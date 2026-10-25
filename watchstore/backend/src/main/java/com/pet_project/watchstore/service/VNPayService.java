package com.pet_project.watchstore.service;

import com.pet_project.watchstore.common.constants.OrderStatus;
import com.pet_project.watchstore.common.constants.PaymentMethod;
import com.pet_project.watchstore.common.constants.PaymentStatus;
import com.pet_project.watchstore.config.VNPayConfig;
import com.pet_project.watchstore.entity.Order;
import com.pet_project.watchstore.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VNPayService {

    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VNPayConfig config;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final boolean confirmOnReturn;

    public VNPayService(VNPayConfig config, OrderRepository orderRepository,
                        ApplicationEventPublisher eventPublisher,
                        @Value("${app.vnpay.confirm-on-return:false}") boolean confirmOnReturn) {
        this.config = config;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.confirmOnReturn = confirmOnReturn;
    }

    @Transactional
    public String createPaymentUrl(Integer orderId, Integer userId, String ipAddress) {
        if (!config.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "VNPay chưa được cấu hình");
        }
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }
        if (order.getPaymentMethod() != PaymentMethod.VNPAY || order.getStatus() != OrderStatus.PENDING
                || order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đơn hàng không thể thanh toán qua VNPay");
        }

        if (order.getPaymentStatus() == PaymentStatus.PENDING && order.getPaymentReference() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Giao dịch VNPay đang chờ xử lý");
        }
        String amount = amountInCents(order.getTotalAmount());
        if (amount.length() > 12 || order.getTotalAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền VNPay không hợp lệ");
        }
        String reference = orderId + UUID.randomUUID().toString().replace("-", "");
        order.beginPayment(reference);
        ZonedDateTime now = ZonedDateTime.now(VIETNAM);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", config.tmnCode());
        params.put("vnp_Amount", amount);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", reference);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", config.returnUrl());
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", now.format(DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMAT));

        String query = encodedQuery(params);
        return config.payUrl() + "?" + query + "&vnp_SecureHash=" + sign(query);
    }

    @Transactional
    public IpnResponse handleIpn(Map<String, String> params) {
        if (!validSignature(params)) {
            return new IpnResponse("97", "Invalid signature");
        }
        if (!config.tmnCode().equals(params.get("vnp_TmnCode"))) {
            return new IpnResponse("02", "Invalid merchant");
        }
        if (params.get("vnp_ResponseCode") == null || params.get("vnp_TransactionStatus") == null) {
            return new IpnResponse("99", "Invalid request");
        }
        String reference = params.get("vnp_TxnRef");
        if (reference == null || reference.isBlank()) {
            return new IpnResponse("01", "Order not found");
        }
        Order order = orderRepository.findByPaymentReferenceForUpdate(reference).orElse(null);
        if (order == null || order.getPaymentMethod() != PaymentMethod.VNPAY) {
            return new IpnResponse("01", "Order not found");
        }
        if (!amountInCents(order.getTotalAmount()).equals(params.get("vnp_Amount"))) {
            return new IpnResponse("04", "Invalid amount");
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING || order.getStatus() != OrderStatus.PENDING) {
            return new IpnResponse("02", "Order already confirmed");
        }
        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
        if (success) {
            order.markPaid();
        } else {
            order.markPaymentFailed();
        }
        eventPublisher.publishEvent(new PaymentResultEmailEvent(order.getId(), order.getUser().getEmail(),
                order.getTotalAmount(), success));
        return new IpnResponse("00", "Confirm success");
    }

    @Transactional
    public String returnStatus(Map<String, String> params) {
        if (!validSignature(params) || !config.tmnCode().equals(params.get("vnp_TmnCode"))
                || params.get("vnp_ResponseCode") == null || params.get("vnp_TransactionStatus") == null) {
            return "invalid";
        }
        String reference = params.get("vnp_TxnRef");
        if (reference == null) {
            return "invalid";
        }
        // Chỉ dùng trong sandbox local khi VNPay không gọi được IPN tới localhost.
        // Callback phải có chữ ký hợp lệ; IPN vẫn là nguồn xác nhận chính khi triển khai công khai.
        if (confirmOnReturn) {
            IpnResponse response = handleIpn(params);
            if (!"00".equals(response.RspCode()) && !"02".equals(response.RspCode())) {
                return "invalid";
            }
        }
        Order order = orderRepository.findByPaymentReference(reference).orElse(null);
        if (order == null || order.getPaymentMethod() != PaymentMethod.VNPAY
                || !amountInCents(order.getTotalAmount()).equals(params.get("vnp_Amount"))) {
            return "invalid";
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return "success";
        }
        if (!"00".equals(params.get("vnp_ResponseCode"))
                || !"00".equals(params.get("vnp_TransactionStatus"))) {
            return "failed";
        }
        return "pending";
    }

    private boolean validSignature(Map<String, String> params) {
        if (!config.isConfigured()) {
            return false;
        }
        String supplied = params.get("vnp_SecureHash");
        if (supplied == null || !supplied.matches("(?i)[0-9a-f]{128}")) {
            return false;
        }
        Map<String, String> signedFields = params.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vnp_"))
                .filter(entry -> !entry.getKey().equals("vnp_SecureHash")
                        && !entry.getKey().equals("vnp_SecureHashType"))
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, TreeMap::new));
        String expected = sign(encodedQuery(signedFields));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                supplied.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(config.hashSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký yêu cầu VNPay", ex);
        }
    }

    private static String encodedQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String amountInCents(BigDecimal amount) {
        return amount.movePointRight(2).toBigIntegerExact().toString();
    }

    public record IpnResponse(String RspCode, String Message) {
    }
}
