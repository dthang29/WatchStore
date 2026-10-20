package com.pet_project.watchstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(min = 2, max = 150, message = "Họ tên từ 2 đến 150 ký tự")
        String fullName,

        @Pattern(regexp = "^$|^(0|\\+84)\\d{9}$", message = "Số điện thoại không hợp lệ")
        String phone,

        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String address
) {
}
