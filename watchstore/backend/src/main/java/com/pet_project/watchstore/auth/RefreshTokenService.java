package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.config.AppSecurityProperties;
import com.pet_project.watchstore.entity.RefreshToken;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh token là chuỗi ngẫu nhiên 256 bit (không phải JWT), DB chỉ lưu SHA-256 của nó.
 * Mỗi lần refresh sẽ xoay vòng (rotation): token cũ bị thu hồi, cấp token mới.
 * Nếu 1 token đã thu hồi bị dùng lại -> nghi bị đánh cắp -> thu hồi toàn bộ phiên của user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    /**
     * Khoảng ân hạn cho trường hợp hợp lệ: 2 tab cùng refresh 1 lúc bằng cùng 1 cookie.
     * Trong khoảng này token cũ bị từ chối nhưng không coi là tấn công.
     */
    private static final Duration REUSE_GRACE_PERIOD = Duration.ofSeconds(30);

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppSecurityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public record Rotation(User user, String refreshToken) {
    }

    /** Tạo refresh token mới cho user, trả về token gốc để đặt vào cookie. */
    @Transactional
    public String create(User user) {
        String rawToken = generateRawToken();
        LocalDateTime expiresAt = now().plus(properties.jwt().refreshTokenTtl());
        refreshTokenRepository.save(RefreshToken.of(user, hash(rawToken), expiresAt));
        return rawToken;
    }

    // noRollbackFor: lệnh thu hồi toàn bộ phiên khi phát hiện reuse phải được commit dù có ném lỗi
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Rotation rotate(String rawToken) {
        RefreshToken current = refreshTokenRepository.findForUpdate(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Phiên đăng nhập không hợp lệ"));
        LocalDateTime now = now();
        User user = current.getUser();

        if (current.isRevoked()) {
            if (current.getRevokedAt().isBefore(now.minus(REUSE_GRACE_PERIOD))) {
                int revoked = refreshTokenRepository.revokeAllActiveByUserId(user.getId(), now);
                log.warn("Phát hiện refresh token bị dùng lại, userId={}, đã thu hồi {} phiên", user.getId(), revoked);
            }
            throw new InvalidRefreshTokenException("Phiên đăng nhập đã bị thu hồi");
        }
        if (current.isExpired(now)) {
            throw new InvalidRefreshTokenException("Phiên đăng nhập đã hết hạn");
        }

        current.revoke(now);
        return new Rotation(user, create(user));
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> token.revoke(now()));
    }

    @Transactional
    public void revokeAllForUser(Integer userId) {
        refreshTokenRepository.revokeAllActiveByUserId(userId, now());
    }

    /** Dọn token hết hạn lúc 3h sáng mỗi ngày. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        int deleted = refreshTokenRepository.deleteExpired(now());
        log.info("Đã xoá {} refresh token hết hạn", deleted);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không khả dụng", e);
        }
    }

    private static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
