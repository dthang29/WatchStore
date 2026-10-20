package com.pet_project.watchstore.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Đã đăng nhập nhưng không đủ quyền (vd: USER gọi /api/admin/**) -> 403 JSON. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ProblemResponseWriter.write(request, response, HttpStatus.FORBIDDEN,
                "Bạn không có quyền truy cập tài nguyên này");
    }
}
