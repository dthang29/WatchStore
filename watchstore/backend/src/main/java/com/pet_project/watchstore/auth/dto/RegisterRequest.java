package com.pet_project.watchstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Quy tắc mật khẩu khớp với registerSchema (zod) ở frontend
public record RegisterRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(min = 2, max = 150, message = "Họ tên từ 2 đến 150 ký tự")
        String fullName,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$", message = "Mật khẩu cần ít nhất 1 chữ hoa và 1 chữ số")
        String password
) {
}
