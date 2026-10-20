package com.pet_project.watchstore.security;

import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Lấy thông tin user từ Google rồi tìm hoặc tạo User tương ứng trong DB.
 * Dùng scope "profile,email" (không có openid) nên Spring gọi service này thay vì OidcUserService.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equals(registrationId)) {
            throw error("unsupported_provider", "Chưa hỗ trợ đăng nhập bằng " + registrationId);
        }

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = User.normalizeEmail((String) attributes.get("email"));
        // Chỉ tin email đã được Google xác minh, tránh chiếm tài khoản qua email giả
        if (email == null || !Boolean.TRUE.equals(attributes.get("email_verified"))) {
            throw error("email_not_verified", "Email Google chưa được xác minh");
        }

        String googleId = (String) attributes.get("sub");
        String name = (String) attributes.getOrDefault("name", email);
        String picture = (String) attributes.get("picture");

        User user = userRepository.findByEmail(email)
                .map(existing -> linkGoogleAccount(existing, googleId, picture))
                .orElseGet(() -> userRepository.save(User.google(googleId, email, name, picture)));

        return new UserPrincipal(user, attributes);
    }

    private User linkGoogleAccount(User user, String googleId, String picture) {
        if (user.getGoogleId() == null) user.setGoogleId(googleId);
        if (user.getAvatarUrl() == null) user.setAvatarUrl(picture);
        return user;
    }

    private OAuth2AuthenticationException error(String code, String message) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), message);
    }
}
