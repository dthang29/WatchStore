package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.auth.dto.ForgotPasswordRequest;
import com.pet_project.watchstore.auth.dto.ResetPasswordRequest;
import com.pet_project.watchstore.entity.PasswordResetRequest;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.PasswordResetRequestRepository;
import com.pet_project.watchstore.repository.UserRepository;
import com.pet_project.watchstore.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Service
public class PasswordResetService {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetRequestRepository requestRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void requestCode(ForgotPasswordRequest request) {
        String email = User.normalizeEmail(request.email());
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getPasswordHash() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        PasswordResetRequest pending = requestRepository.findByEmailForUpdate(email).orElse(null);
        if (pending != null && !pending.canResend(now)) {
            return;
        }

        if (pending == null) {
            pending = new PasswordResetRequest(email);
        }
        String code = String.format(Locale.ROOT, "%06d", OTP_RANDOM.nextInt(1_000_000));
        pending.issueCode(passwordEncoder.encode(code), now);
        requestRepository.saveAndFlush(pending);
        emailService.sendPasswordResetCode(email, code);
    }

    @Transactional(noRollbackFor = InvalidPasswordResetCodeException.class)
    public void resetPassword(ResetPasswordRequest request) {
        String email = User.normalizeEmail(request.email());
        PasswordResetRequest pending = requestRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> invalidCode(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ"));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (pending.isExpired(now)) {
            throw invalidCode(HttpStatus.GONE, "Mã OTP đã hết hạn");
        }
        if (pending.hasTooManyAttempts()) {
            throw invalidCode(HttpStatus.TOO_MANY_REQUESTS, "Đã vượt quá số lần thử OTP");
        }
        if (!pending.matches(request.code(), passwordEncoder)) {
            throw invalidCode(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ");
        }

        User user = userRepository.findByEmail(email)
                .filter(found -> found.getPasswordHash() != null)
                .orElseThrow(() -> invalidCode(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        requestRepository.delete(pending);
        refreshTokenService.revokeAllForUser(user.getId());
    }

    private static InvalidPasswordResetCodeException invalidCode(HttpStatus status, String message) {
        return new InvalidPasswordResetCodeException(status, message);
    }
}
