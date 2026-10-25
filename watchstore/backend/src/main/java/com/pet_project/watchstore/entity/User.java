package com.pet_project.watchstore.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import com.pet_project.watchstore.common.constants.Role;
import com.pet_project.watchstore.common.constants.AuthProvider;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt hash; null với tài khoản chỉ đăng nhập bằng Google. */
    @Column(name = "password_hash")
    private String passwordHash;

    /** Claim "sub" của Google. */
    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    private String address;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // columnDefinition khớp cột ENUM của MySQL để Hibernate validate không báo lệch kiểu
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "enum('CUSTOMER','ADMIN')")
    private Role role = Role.CUSTOMER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static User local(String email, String passwordHash, String fullName) {
        User user = new User();
        user.email = normalizeEmail(email);
        user.passwordHash = passwordHash;
        user.fullName = fullName.trim();
        return user;
    }

    public static User google(String googleId, String email, String fullName, String avatarUrl) {
        User user = new User();
        user.email = normalizeEmail(email);
        user.googleId = googleId;
        user.fullName = fullName;
        user.avatarUrl = avatarUrl;
        return user;
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public AuthProvider getProvider() {
        return passwordHash == null && googleId != null ? AuthProvider.GOOGLE : AuthProvider.LOCAL;
    }

    /** Danh sách authority dạng "ROLE_CUSTOMER" / "ROLE_ADMIN" — dùng cho JWT và response. */
    public List<String> getAuthorityNames() {
        return List.of(role.authority());
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
