package com.pet_project.watchstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentResultEmailListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultEmailListener.class);

    private final EmailService emailService;

    public PaymentResultEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(PaymentResultEmailEvent event) {
        try {
            emailService.sendPaymentResult(event.email(), event.orderId(), event.amount(), event.success());
        } catch (RuntimeException ex) {
            // Thanh toán đã commit; lỗi SMTP không được thay đổi kết quả trả cho VNPay.
            log.error("Không gửi được email kết quả thanh toán cho đơn #{}: {}",
                    event.orderId(), ex.getClass().getSimpleName());
        }
    }
}
