package com.pet_project.watchstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Đọc "Authorization: Bearer <jwt>", xác thực stateless từ claims (không truy vấn DB).
 * Token sai/hết hạn thì bỏ qua: request đi tiếp dưới dạng anonymous và bị chặn ở
 * authorizeHttpRequests nếu endpoint yêu cầu đăng nhập -> 401 -> frontend gọi /auth/refresh.
 *
 * Không đánh dấu @Component để Spring Boot không tự đăng ký thêm 1 lần ngoài security chain.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            try {
                Claims claims = jwtService.parseAccessToken(header.substring(BEARER_PREFIX.length()));
                var authentication = UsernamePasswordAuthenticationToken.authenticated(
                        jwtService.toAuthUser(claims), null, jwtService.toAuthorities(claims));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("Access token không hợp lệ: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
