package com.pet_project.watchstore.service;

import com.pet_project.watchstore.common.constants.OrderStatus;
import com.pet_project.watchstore.common.constants.PaymentMethod;
import com.pet_project.watchstore.common.constants.PaymentStatus;
import com.pet_project.watchstore.config.VNPayConfig;
import com.pet_project.watchstore.entity.Order;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VNPayServiceTest {

    private static final String SECRET = "test-hash-secret";
    private final OrderRepository repository = mock(OrderRepository.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final VNPayService service = new VNPayService(
            new VNPayConfig("http://localhost:8080/api/vnpay/return", "ABCDEFGH", SECRET), repository, events, false);

    @Test
    void paymentUrlUsesPersistedOrderAmountAndValidSignature() {
        Order order = mock(Order.class);
        User user = mock(User.class);
        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(7);
        when(order.getPaymentMethod()).thenReturn(PaymentMethod.VNPAY);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);
        when(order.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(order.getTotalAmount()).thenReturn(new BigDecimal("125000.00"));
        when(repository.findByIdForUpdate(12)).thenReturn(Optional.of(order));

        String url = service.createPaymentUrl(12, 7, "127.0.0.1");
        Map<String, String> params = queryParams(url);
        assertEquals("12500000", params.get("vnp_Amount"));
        assertTrue(params.get("vnp_TxnRef").startsWith("12"));
        String signature = params.remove("vnp_SecureHash");
        assertEquals(hmac(query(params)), signature);
        verify(order).beginPayment(params.get("vnp_TxnRef"));
    }

    @Test
    void ipnChecksSignatureAmountAndOnlyConfirmsMatchingOrder() {
        Order order = mock(Order.class);
        User user = mock(User.class);
        when(order.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("customer@example.com");
        when(order.getPaymentMethod()).thenReturn(PaymentMethod.VNPAY);
        when(order.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);
        when(order.getTotalAmount()).thenReturn(new BigDecimal("125000.00"));
        when(repository.findByPaymentReferenceForUpdate("12abc"))
                .thenReturn(Optional.of(order));

        Map<String, String> params = new TreeMap<>(Map.of(
                "vnp_TmnCode", "ABCDEFGH", "vnp_TxnRef", "12abc",
                "vnp_Amount", "12500000", "vnp_ResponseCode", "00",
                "vnp_TransactionStatus", "00"));
        params.put("vnp_SecureHash", hmac(query(params)));

        assertEquals("00", service.handleIpn(params).RspCode());
        verify(order).markPaid();
        verify(events).publishEvent(org.mockito.ArgumentMatchers.any(PaymentResultEmailEvent.class));

        params.put("vnp_Amount", "1");
        assertEquals("97", service.handleIpn(params).RspCode());
        params.put("vnp_SecureHash", hmac(query(new TreeMap<>(Map.of(
                "vnp_TmnCode", "ABCDEFGH", "vnp_TxnRef", "12abc",
                "vnp_Amount", "1", "vnp_ResponseCode", "00",
                "vnp_TransactionStatus", "00")))));
        assertEquals("04", service.handleIpn(params).RspCode());
        verify(order, never()).markPaymentFailed();
    }

    @Test
    void signedReturnCanConfirmSandboxPaymentWhenEnabled() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("customer@example.com");
        Order order = Order.create(user, "Customer", "0900000000", "123 Street",
                null, new BigDecimal("125000.00"), PaymentMethod.VNPAY);
        order.beginPayment("12abc");
        when(repository.findByPaymentReference("12abc")).thenReturn(Optional.of(order));
        when(repository.findByPaymentReferenceForUpdate("12abc")).thenReturn(Optional.of(order));
        Map<String, String> params = new TreeMap<>(Map.of(
                "vnp_TmnCode", "ABCDEFGH", "vnp_TxnRef", "12abc",
                "vnp_Amount", "12500000", "vnp_ResponseCode", "00",
                "vnp_TransactionStatus", "00"));
        params.put("vnp_SecureHash", hmac(query(params)));

        assertEquals("pending", service.returnStatus(params));
        VNPayService sandboxService = new VNPayService(
                new VNPayConfig("http://localhost:8080/api/vnpay/return", "ABCDEFGH", SECRET), repository, events, true);
        assertEquals("success", sandboxService.returnStatus(params));
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals("02", sandboxService.handleIpn(params).RspCode());

        params.put("vnp_Amount", "1");
        assertEquals("invalid", sandboxService.returnStatus(params));
    }

    @Test
    void failedPaymentPublishesOneFailureEmailEvent() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("customer@example.com");
        Order order = Order.create(user, "Customer", "0900000000", "123 Street",
                null, new BigDecimal("125000.00"), PaymentMethod.VNPAY);
        order.beginPayment("12failed");
        when(repository.findByPaymentReferenceForUpdate("12failed")).thenReturn(Optional.of(order));
        Map<String, String> params = new TreeMap<>(Map.of(
                "vnp_TmnCode", "ABCDEFGH", "vnp_TxnRef", "12failed",
                "vnp_Amount", "12500000", "vnp_ResponseCode", "24",
                "vnp_TransactionStatus", "02"));
        params.put("vnp_SecureHash", hmac(query(params)));

        assertEquals("00", service.handleIpn(params).RspCode());
        assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());
        assertEquals("02", service.handleIpn(params).RspCode());
        ArgumentCaptor<PaymentResultEmailEvent> event = ArgumentCaptor.forClass(PaymentResultEmailEvent.class);
        verify(events, times(1)).publishEvent(event.capture());
        assertEquals(false, event.getValue().success());
        assertEquals("customer@example.com", event.getValue().email());
    }

    private static Map<String, String> queryParams(String url) {
        return Arrays.stream(URI.create(url).getRawQuery().split("&"))
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(parts -> decode(parts[0]), parts -> decode(parts[1]),
                        (a, b) -> b, TreeMap::new));
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String query(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
