package com.pet_project.watchstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_requests")
public class PasswordResetRequest {

    @Id
    @Column(length = 255)
    private String email;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "resend_after", nullable = false)
    private LocalDateTime resendAfter;

    @Column(nullable = false)
    private int attempts;

    protected PasswordResetRequest() {
    }

    public PasswordResetRequest(String email) {
        this.email = email;
    }

    public void issueCode(String codeHash, LocalDateTime now) {
        otpHash = codeHash;
        expiresAt = now.plusMinutes(10);
        resendAfter = now.plusMinutes(1);
        attempts = 0;
    }

    public boolean canResend(LocalDateTime now) {
        return !now.isBefore(resendAfter);
    }

    public boolean isExpired(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }

    public boolean hasTooManyAttempts() {
        return attempts >= 5;
    }

    public boolean matches(String code, PasswordEncoder encoder) {
        attempts++;
        return encoder.matches(code, otpHash);
    }
}
