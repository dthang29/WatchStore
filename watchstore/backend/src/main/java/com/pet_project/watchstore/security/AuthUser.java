package com.pet_project.watchstore.security;

/**
 * Principal của request đã xác thực bằng access token.
 * Dùng trong controller: {@code @AuthenticationPrincipal AuthUser me}.
 */
public record AuthUser(Integer id, String email) {
}
