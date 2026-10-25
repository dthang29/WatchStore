package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.auth.dto.AuthResponse;
import com.pet_project.watchstore.auth.dto.LoginRequest;
import com.pet_project.watchstore.auth.dto.RegisterRequest;
import com.pet_project.watchstore.auth.dto.ResendRegistrationCodeRequest;
import com.pet_project.watchstore.auth.dto.VerifyRegistrationRequest;
import com.pet_project.watchstore.dto.UserResponse;
import com.pet_project.watchstore.entity.PendingRegistration;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.PendingRegistrationRepository;
import com.pet_project.watchstore.repository.UserRepository;
import com.pet_project.watchstore.service.EmailService;
import com.pet_project.watchstore.security.jwt.JwtService;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final EmailService emailService;

    /** Kết quả đăng nhập: body trả về client + refresh token để controller đặt vào cookie. */
    public record AuthResult(AuthResponse response, String refreshToken) {
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = User.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        PendingRegistration pending = pendingRegistrationRepository.findByEmailForUpdate(email).orElse(null);
        if (pending != null && !pending.canResend(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi trước khi gửi mã mới");
        }
        String passwordHash = passwordEncoder.encode(request.password());
        if (pending == null) {
            pending = new PendingRegistration(email, request.fullName().trim(), passwordHash);
        } else {
            pending.updateDetails(request.fullName().trim(), passwordHash);
        }
        String code = newCode();
        pending.issueCode(passwordEncoder.encode(code), now);
        pendingRegistrationRepository.saveAndFlush(pending);
        emailService.sendRegistrationCode(email, code);
    }

    @Transactional(noRollbackFor = InvalidRegistrationCodeException.class)
    public UserResponse verifyRegistration(VerifyRegistrationRequest request) {
        String email = User.normalizeEmail(request.email());
        PendingRegistration pending = pendingRegistrationRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new InvalidRegistrationCodeException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ"));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (pending.isExpired(now)) {
            throw new InvalidRegistrationCodeException(HttpStatus.GONE, "Mã OTP đã hết hạn");
        }
        if (pending.hasTooManyAttempts()) {
            throw new InvalidRegistrationCodeException(HttpStatus.TOO_MANY_REQUESTS, "Đã vượt quá số lần thử OTP");
        }
        if (!pending.matches(request.code(), passwordEncoder)) {
            throw new InvalidRegistrationCodeException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        User user = userRepository.save(User.local(email, pending.getPasswordHash(), pending.getFullName()));
        pendingRegistrationRepository.delete(pending);
        return UserResponse.from(user);
    }

    @Transactional
    public void resendRegistrationCode(ResendRegistrationCodeRequest request) {
        String email = User.normalizeEmail(request.email());
        PendingRegistration pending = pendingRegistrationRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không có đăng ký đang chờ xác thực"));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (!pending.canResend(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi trước khi gửi mã mới");
        }
        String code = newCode();
        pending.issueCode(passwordEncoder.encode(code), now);
        emailService.sendRegistrationCode(email, code);
    }

    private static String newCode() {
        return String.format(Locale.ROOT, "%06d", OTP_RANDOM.nextInt(1_000_000));
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
