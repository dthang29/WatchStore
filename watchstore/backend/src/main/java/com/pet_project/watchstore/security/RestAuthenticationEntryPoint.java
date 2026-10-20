package com.pet_project.watchstore.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Chưa đăng nhập hoặc access token sai/hết hạn -> 401 JSON (không redirect sang trang login). */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ProblemResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                "Bạn chưa đăng nhập hoặc phiên đã hết hạn");
    }
}
