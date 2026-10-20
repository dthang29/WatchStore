package com.pet_project.watchstore.config;

import com.pet_project.watchstore.entity.Role;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Tạo tài khoản admin từ ADMIN_EMAIL / ADMIN_PASSWORD nếu chưa tồn tại. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) return;

        String email = User.normalizeEmail(adminEmail);
        if (userRepository.existsByEmail(email)) return;

        User admin = User.local(email, passwordEncoder.encode(adminPassword), "Administrator");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Đã tạo tài khoản admin {}", email);
    }
}
