package com.pet_project.watchstore.service;

import java.math.BigDecimal;

public record PaymentResultEmailEvent(Integer orderId, String email, BigDecimal amount, boolean success) {
}
