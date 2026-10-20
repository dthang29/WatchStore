package com.pet_project.watchstore.security;

import com.pet_project.watchstore.auth.RefreshTokenCookie;
import com.pet_project.watchstore.auth.RefreshTokenService;
import com.pet_project.watchstore.config.AppSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Đăng nhập Google thành công: cấp refresh token (cookie HttpOnly) rồi redirect về frontend.
 * Không đưa token lên URL (tránh lộ qua history, log, header Referer); frontend sẽ tự gọi
 * POST /api/auth/refresh để đổi cookie lấy access token.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookie refreshTokenCookie;
    private final AppSecurityProperties properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String refreshToken = refreshTokenService.create(principal.getUser());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(refreshToken).toString());

        // Session chỉ dùng tạm để lưu state của OAuth2, xong thì huỷ
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        response.sendRedirect(properties.frontendUrl() + "/oauth2/callback");
    }
}
