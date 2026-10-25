package com.pet_project.watchstore.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String host;
    private final String from;
    private final String password;
    private final String frontendUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${spring.mail.host:}") String host,
                        @Value("${spring.mail.username:}") String from,
                        @Value("${spring.mail.password:}") String password,
                        @Value("${app.security.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.mailSender = mailSender;
        this.host = host;
        this.from = from;
        this.password = password;
        this.frontendUrl = frontendUrl;
    }

    public void sendRegistrationCode(String to, String code) {
        send(to, "Mã xác thực đăng ký Watchstore",
                "Mã OTP đăng ký tài khoản của bạn là: " + code + "\n"
                        + "Mã có hiệu lực trong 10 phút và chỉ dùng một lần.\n"
                        + "Nếu bạn không yêu cầu đăng ký, hãy bỏ qua email này.");
    }

    public void sendPasswordResetCode(String to, String code) {
        send(to, "Mã đặt lại mật khẩu Watchstore",
                "Mã OTP đặt lại mật khẩu của bạn là: " + code + "\n"
                        + "Mã có hiệu lực trong 10 phút và chỉ dùng một lần.\n"
                        + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.");
    }

    public void sendPaymentResult(String to, Integer orderId, BigDecimal amount, boolean success) {
        String total = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " VND";
        String subject = success ? "Thanh toán đơn hàng #" + orderId + " thành công"
                : "Thanh toán đơn hàng #" + orderId + " thất bại";
        String detail = success
                ? "Thanh toán đã hoàn tất. Đơn hàng của bạn đang được xử lý."
                : "Thanh toán chưa thành công. Bạn có thể thử lại trong mục Đơn hàng của tôi.";
        send(to, subject, "Đơn hàng #" + orderId + "\nTổng tiền: " + total + "\n"
                + detail + "\nXem đơn hàng: " + frontendUrl.replaceAll("/+$", "") + "/account/orders");
    }

    private void send(String to, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (host.isBlank() || from.isBlank() || password.isBlank() || sender == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ email chưa được cấu hình");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            sender.send(message);
        } catch (MailException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không gửi được email, vui lòng thử lại");
        }
    }
}
