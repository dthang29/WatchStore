package com.pet_project.watchstore.security;

import com.pet_project.watchstore.config.AppSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final AppSecurityProperties properties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("Đăng nhập OAuth2 thất bại: {}", exception.getMessage());

        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        // Không đưa message lỗi chi tiết lên URL
        response.sendRedirect(properties.frontendUrl() + "/login?error=oauth2");
    }
}
