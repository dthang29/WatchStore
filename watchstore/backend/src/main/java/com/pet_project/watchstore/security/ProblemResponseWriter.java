package com.pet_project.watchstore.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Ghi lỗi dạng ProblemDetail (RFC 9457) cho các lỗi xảy ra trong security filter chain. */
final class ProblemResponseWriter {

    private ProblemResponseWriter() {
    }

    // detail chỉ là chuỗi cố định trong code, không chứa input người dùng nên không cần escape JSON
    static void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String detail) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"about:blank","title":"%s","status":%d,"detail":"%s","instance":"%s"}"""
                .formatted(status.getReasonPhrase(), status.value(), detail, request.getRequestURI()));
    }
}
