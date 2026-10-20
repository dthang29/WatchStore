package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.auth.dto.AuthResponse;
import com.pet_project.watchstore.auth.dto.LoginRequest;
import com.pet_project.watchstore.auth.dto.RegisterRequest;
import com.pet_project.watchstore.dto.UserResponse;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.UserRepository;
import com.pet_project.watchstore.security.JwtService;
import com.pet_project.watchstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /** Kết quả đăng nhập: body trả về client + refresh token để controller đặt vào cookie. */
    public record AuthResult(AuthResponse response, String refreshToken) {
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = User.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        User user = User.local(email, passwordEncoder.encode(request.password()), request.fullName());
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Xác thực email/password qua AuthenticationManager -> DaoAuthenticationProvider
     * -> CustomUserDetailsService + BCrypt. Sai thông tin sẽ ném BadCredentialsException.
     */
    @Transactional
    public AuthResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        User.normalizeEmail(request.email()), request.password()));
        User user = ((UserPrincipal) authentication.getPrincipal()).getUser();
        return issueTokens(user, refreshTokenService.create(user));
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Không tìm thấy phiên đăng nhập");
        }
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawRefreshToken);
        return issueTokens(rotation.user(), rotation.refreshToken());
    }

    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revoke(rawRefreshToken);
        }
    }

    private AuthResult issueTokens(User user, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(user);
        AuthResponse response = AuthResponse.bearer(
                accessToken, jwtService.getAccessTokenTtlSeconds(), UserResponse.from(user));
        return new AuthResult(response, refreshToken);
    }
}
