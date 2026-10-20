package com.pet_project.watchstore.security;

import com.pet_project.watchstore.config.AppSecurityProperties;
import com.pet_project.watchstore.entity.Role;
import com.pet_project.watchstore.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "x5n6K8R6QvK02wpVxC6KICncyr/7bIzlP3XXlo3tPTuOOkE1InMFA+hL45njuMDh";
    private static final String OTHER_SECRET = "c2VjcmV0LWtleS1raGFjLWRlLWtpZW0tdHJhLWNodS1reS1zYWktMTIzNDU2Nzg=";

    private static JwtService jwtService(String secret, Duration ttl) {
        var properties = new AppSecurityProperties(
                new AppSecurityProperties.Jwt(secret, "watchstore", ttl, Duration.ofDays(7)),
                new AppSecurityProperties.Cookie(false, "Lax"),
                "http://localhost:5173",
                List.of("http://localhost:5173"));
        return new JwtService(properties);
    }

    private static User adminUser() {
        User user = User.local("Admin@Watchstore.local", "hash", "Quản trị viên");
        user.setRole(Role.ADMIN);
        ReflectionTestUtils.setField(user, "id", 42);
        return user;
    }

    @Test
    void generatedTokenContainsUserIdEmailAndRoles() {
        JwtService service = jwtService(SECRET, Duration.ofMinutes(15));

        Claims claims = service.parseAccessToken(service.generateAccessToken(adminUser()));

        assertThat(service.toAuthUser(claims)).isEqualTo(new AuthUser(42, "admin@watchstore.local"));
        assertThat(service.toAuthorities(claims))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
        assertThat(claims.get("fullName", String.class)).isEqualTo("Quản trị viên");
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String forged = jwtService(OTHER_SECRET, Duration.ofMinutes(15)).generateAccessToken(adminUser());

        assertThatThrownBy(() -> jwtService(SECRET, Duration.ofMinutes(15)).parseAccessToken(forged))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService service = jwtService(SECRET, Duration.ofSeconds(-1));
        String expired = service.generateAccessToken(adminUser());

        assertThatThrownBy(() -> service.parseAccessToken(expired)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsTamperedPayload() {
        JwtService service = jwtService(SECRET, Duration.ofMinutes(15));
        String[] parts = service.generateAccessToken(adminUser()).split("\\.");
        String tampered = parts[0] + "." + parts[1].substring(0, parts[1].length() - 2) + "AA." + parts[2];

        assertThatThrownBy(() -> service.parseAccessToken(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void refusesSecretShorterThan256Bits() {
        assertThatThrownBy(() -> jwtService("c2hvcnQ=", Duration.ofMinutes(15)))
                .isInstanceOf(IllegalStateException.class);
    }
}
