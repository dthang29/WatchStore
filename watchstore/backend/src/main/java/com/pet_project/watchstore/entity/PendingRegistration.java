package com.pet_project.watchstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "pending_registrations")
public class PendingRegistration {

    @Id
    @Column(length = 255)
    private String email;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "resend_after", nullable = false)
    private LocalDateTime resendAfter;

    @Column(nullable = false)
    private int attempts;

    protected PendingRegistration() {
    }

    public PendingRegistration(String email, String fullName, String passwordHash) {
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
    }

    public void updateDetails(String fullName, String passwordHash) {
        this.fullName = fullName;
        this.passwordHash = passwordHash;
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

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
