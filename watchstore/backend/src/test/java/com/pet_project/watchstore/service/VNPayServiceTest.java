package com.pet_project.watchstore.service;

import com.pet_project.watchstore.config.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VNPayServiceTest {

    private static final String SECRET = "test-hash-secret";
    private final VNPayService service = new VNPayService();
    private String originalReturnUrl;
    private String originalTmnCode;
    private String originalHashSecret;

    @BeforeEach
    void configureVnPay() {
        originalReturnUrl = VNPayConfig.vnp_ReturnUrl;
        originalTmnCode = VNPayConfig.vnp_TmnCode;
        originalHashSecret = VNPayConfig.vnp_HashSecret;
        VNPayConfig.vnp_ReturnUrl = "/api/vnpay/return";
        VNPayConfig.vnp_TmnCode = "ABCDEFGH";
        VNPayConfig.vnp_HashSecret = SECRET;
    }

    @AfterEach
    void restoreVnPayConfig() {
        VNPayConfig.vnp_ReturnUrl = originalReturnUrl;
        VNPayConfig.vnp_TmnCode = originalTmnCode;
        VNPayConfig.vnp_HashSecret = originalHashSecret;
    }

    @Test
    void createOrderBuildsSignedPaymentUrl() {
        String url = service.createOrder(125000, "Thanh toan don hang 12", "http://localhost:8080");
        URI uri = URI.create(url);
        Map<String, String> params = queryParams(uri);

        assertEquals(VNPayConfig.vnp_PayUrl, uri.getScheme() + "://" + uri.getHost() + uri.getPath());
        assertEquals("2.1.0", params.get("vnp_Version"));
        assertEquals("pay", params.get("vnp_Command"));
        assertEquals("ABCDEFGH", params.get("vnp_TmnCode"));
        assertEquals("12500000", params.get("vnp_Amount"));
        assertEquals("VND", params.get("vnp_CurrCode"));
        assertEquals("Thanh toan don hang 12", params.get("vnp_OrderInfo"));
        assertEquals("order-type", params.get("vnp_OrderType"));
        assertEquals("vn", params.get("vnp_Locale"));
        assertEquals("127.0.0.1", params.get("vnp_IpAddr"));
        assertEquals("http://localhost:8080/api/vnpay/return", params.get("vnp_ReturnUrl"));
        assertTrue(params.get("vnp_TxnRef").matches("\\d{8}"));
        assertTrue(params.get("vnp_CreateDate").matches("\\d{14}"));
        assertTrue(params.get("vnp_ExpireDate").matches("\\d{14}"));

        String signature = params.remove("vnp_SecureHash");
        assertEquals(hmac(encodedQuery(params)), signature);
    }

    @Test
    void orderReturnAcceptsValidSuccessfulPayment() {
        Map<String, String> params = signedReturnParams("00");
        params.put("vnp_SecureHashType", "SHA512");

        assertEquals(1, service.orderReturn(requestWith(params)));
    }

    @Test
    void orderReturnReportsValidFailedPayment() {
        Map<String, String> params = signedReturnParams("02");

        assertEquals(0, service.orderReturn(requestWith(params)));
    }

    @Test
    void orderReturnRejectsTamperedPayment() {
        Map<String, String> params = signedReturnParams("00");
        params.put("vnp_Amount", "1");

        assertEquals(-1, service.orderReturn(requestWith(params)));
    }

    private static Map<String, String> signedReturnParams(String transactionStatus) {
        Map<String, String> params = new TreeMap<>(Map.of(
                "vnp_TmnCode", "ABCDEFGH",
                "vnp_TxnRef", "12345678",
                "vnp_Amount", "12500000",
                "vnp_OrderInfo", "Thanh toan don hang 12",
                "vnp_TransactionStatus", transactionStatus));
        params.put("vnp_SecureHash", hmac(encodedQuery(params)));
        return params;
    }

    private static HttpServletRequest requestWith(Map<String, String> params) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterNames()).thenReturn(Collections.enumeration(params.keySet()));
        when(request.getParameter(anyString())).thenAnswer(invocation -> params.get(invocation.getArgument(0)));
        return request;
    }

    private static Map<String, String> queryParams(URI uri) {
        return Arrays.stream(uri.getRawQuery().split("&"))
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(parts -> decode(parts[0]), parts -> decode(parts[1]),
                        (first, second) -> second, TreeMap::new));
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String encodedQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> entry.getKey() + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII))
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
