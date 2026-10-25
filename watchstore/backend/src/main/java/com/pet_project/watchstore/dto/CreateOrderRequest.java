package com.pet_project.watchstore.dto;

import com.pet_project.watchstore.common.constants.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Size(max = 150) String receiverName,
        @NotBlank @Size(max = 20) String phone,
        @NotBlank @Size(max = 255) String address,
        @Size(max = 500) String note,
        @NotNull PaymentMethod paymentMethod,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(@NotNull Integer productId, @Min(1) int quantity) {
    }
}
