package com.pet_project.watchstore.security;

import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(User.normalizeEmail(email))
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
    }
}
