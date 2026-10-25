package com.pet_project.watchstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.pet_project.watchstore.common.constants.PaymentMethod;
import com.pet_project.watchstore.common.constants.PaymentStatus;
import com.pet_project.watchstore.common.constants.OrderStatus;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "receiver_name", nullable = false, length = 150)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(length = 500)
    private String note;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false,
            columnDefinition = "enum('COD','BANK_TRANSFER','VNPAY')")
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false,
            columnDefinition = "enum('PENDING','PAID','FAILED')")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_reference", unique = true, length = 100)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            columnDefinition = "enum('PENDING','CONFIRMED','SHIPPING','DELIVERED','CANCELLED')")
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public static Order create(User user, String receiverName, String receiverPhone,
                               String shippingAddress, String note, BigDecimal totalAmount,
                               PaymentMethod paymentMethod) {
        Order order = new Order();
        order.user = user;
        order.receiverName = receiverName.trim();
        order.receiverPhone = receiverPhone.trim();
        order.shippingAddress = shippingAddress.trim();
        order.note = note == null || note.isBlank() ? null : note.trim();
        order.totalAmount = totalAmount;
        order.paymentMethod = paymentMethod;
        return order;
    }

    public void beginPayment(String reference) {
        paymentReference = reference;
        paymentStatus = PaymentStatus.PENDING;
    }

    public void markPaid() {
        paymentStatus = PaymentStatus.PAID;
        status = OrderStatus.CONFIRMED;
    }

    public void markPaymentFailed() {
        paymentStatus = PaymentStatus.FAILED;
    }
}
