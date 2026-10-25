package com.pet_project.watchstore.security.jwt;

import com.pet_project.watchstore.config.AppSecurityProperties;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.security.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    static final String CLAIM_USER_ID = "uid";
    static final String CLAIM_ROLES = "roles";
    static final String CLAIM_FULL_NAME = "fullName";

    private final SecretKey signingKey;
    private final String issuer;
    private final Duration accessTokenTtl;

    public JwtService(AppSecurityProperties properties) {
        byte[] keyBytes = Decoders.BASE64.decode(properties.jwt().secret());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.security.jwt.secret phải dài tối thiểu 256 bit (32 byte)");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = properties.jwt().issuer();
        this.accessTokenTtl = properties.jwt().accessTokenTtl();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_FULL_NAME, user.getFullName())
                .claim(CLAIM_ROLES, user.getAuthorityNames())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Kiểm tra chữ ký, issuer và hạn dùng.
     *
     * @throws JwtException nếu token không hợp lệ hoặc đã hết hạn
     */
    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthUser toAuthUser(Claims claims) {
        Number userId = claims.get(CLAIM_USER_ID, Number.class);
        if (userId == null) throw new JwtException("Thiếu claim uid");
        return new AuthUser(userId.intValue(), claims.getSubject());
    }

    public List<SimpleGrantedAuthority> toAuthorities(Claims claims) {
        List<?> roles = claims.get(CLAIM_ROLES, List.class);
        if (roles == null) return List.of();
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.toString())).toList();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }
}
