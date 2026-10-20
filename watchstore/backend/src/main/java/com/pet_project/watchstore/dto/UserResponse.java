package com.pet_project.watchstore.dto;

import com.pet_project.watchstore.entity.User;

import java.util.List;

public record UserResponse(
        Integer id,
        String email,
        String fullName,
        String phone,
        String address,
        String avatarUrl,
        String provider,
        List<String> roles
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatarUrl(),
                user.getProvider().name(),
                user.getAuthorityNames());
    }
}
