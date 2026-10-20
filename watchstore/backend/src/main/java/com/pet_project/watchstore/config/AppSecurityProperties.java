package com.pet_project.watchstore.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Cookie cookie,
        @NotBlank String frontendUrl,
        @NotEmpty List<String> allowedOrigins
) {
    public record Jwt(
            @NotBlank String secret,
            @NotBlank String issuer,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl
    ) {
    }

    public record Cookie(boolean secure, @NotBlank String sameSite) {
    }
}
