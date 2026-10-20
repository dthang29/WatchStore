package com.pet_project.watchstore.auth.dto;

import com.pet_project.watchstore.dto.UserResponse;

/** Refresh token KHÔNG nằm trong body — nó được đặt vào cookie HttpOnly. */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse bearer(String accessToken, long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresIn, user);
    }
}
