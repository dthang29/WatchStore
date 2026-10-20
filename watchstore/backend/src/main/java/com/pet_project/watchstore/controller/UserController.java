package com.pet_project.watchstore.controller;

import com.pet_project.watchstore.dto.UpdateProfileRequest;
import com.pet_project.watchstore.dto.UserResponse;
import com.pet_project.watchstore.security.AuthUser;
import com.pet_project.watchstore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser me) {
        return userService.getProfile(me.id());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal AuthUser me,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(me.id(), request);
    }
}
