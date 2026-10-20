package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.config.AppSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cookie chứa refresh token:
 * - HttpOnly: JavaScript không đọc được (chống XSS lấy token)
 * - SameSite=Lax: trình duyệt không gửi kèm trong POST cross-site (chống CSRF)
 * - Path=/api/auth: chỉ gửi tới các endpoint auth, không đi kèm mọi request
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookie {

    public static final String NAME = "refresh_token";
    private static final String PATH = "/api/auth";

    private final AppSecurityProperties properties;

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, properties.jwt().refreshTokenTtl());
    }

    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(properties.cookie().secure())
                .sameSite(properties.cookie().sameSite())
                .path(PATH)
                .maxAge(maxAge)
                .build();
    }
}
