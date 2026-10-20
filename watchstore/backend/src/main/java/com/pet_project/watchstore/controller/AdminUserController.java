package com.pet_project.watchstore.controller;

import com.pet_project.watchstore.dto.PageResponse;
import com.pet_project.watchstore.dto.UserResponse;
import com.pet_project.watchstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// /api/admin/** đã yêu cầu ROLE_ADMIN trong SecurityConfig; @PreAuthorize là lớp bảo vệ thứ 2
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public PageResponse<UserResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.findAll(pageable);
    }
}
