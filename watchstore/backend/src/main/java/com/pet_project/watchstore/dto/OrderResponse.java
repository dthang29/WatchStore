package com.pet_project.watchstore.dto;

import com.pet_project.watchstore.common.constants.OrderStatus;
import com.pet_project.watchstore.common.constants.PaymentMethod;
import com.pet_project.watchstore.common.constants.PaymentStatus;
import com.pet_project.watchstore.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(Integer id, BigDecimal totalAmount, OrderStatus status,
                            PaymentMethod paymentMethod, PaymentStatus paymentStatus,
                            boolean paymentInProgress, LocalDateTime createdAt) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getTotalAmount(), order.getStatus(),
                order.getPaymentMethod(), order.getPaymentStatus(),
                order.getPaymentMethod() == PaymentMethod.VNPAY
                        && order.getPaymentStatus() == PaymentStatus.PENDING
                        && order.getPaymentReference() != null,
                order.getCreatedAt());
    }
}
