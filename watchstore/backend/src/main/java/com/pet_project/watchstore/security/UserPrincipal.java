package com.pet_project.watchstore.security;

import com.pet_project.watchstore.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Principal dùng lúc ĐĂNG NHẬP (form email/password hoặc OAuth2).
 * Sau khi đăng nhập, các request tiếp theo dùng {@link AuthUser} lấy từ JWT.
 */
public class UserPrincipal implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public UserPrincipal(User user) {
        this(user, Map.of());
    }

    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorityNames().stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }
}
