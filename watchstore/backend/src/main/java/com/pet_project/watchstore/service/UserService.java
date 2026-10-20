package com.pet_project.watchstore.service;

import com.pet_project.watchstore.dto.PageResponse;
import com.pet_project.watchstore.dto.UpdateProfileRequest;
import com.pet_project.watchstore.dto.UserResponse;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Integer userId) {
        return UserResponse.from(findById(userId));
    }

    @Transactional
    public UserResponse updateProfile(Integer userId, UpdateProfileRequest request) {
        User user = findById(userId);
        user.setFullName(request.fullName().trim());
        user.setPhone(blankToNull(request.phone()));
        user.setAddress(blankToNull(request.address()));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(UserResponse::from));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }
}
