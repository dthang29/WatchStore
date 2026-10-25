package com.pet_project.watchstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Mã OTP phải gồm 6 chữ số") String code,
        @NotBlank
        @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$", message = "Mật khẩu cần ít nhất 1 chữ hoa và 1 chữ số")
        String newPassword
) {
}
