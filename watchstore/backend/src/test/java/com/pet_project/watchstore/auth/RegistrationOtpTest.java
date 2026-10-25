package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.auth.dto.RegisterRequest;
import com.pet_project.watchstore.auth.dto.VerifyRegistrationRequest;
import com.pet_project.watchstore.entity.PendingRegistration;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.PendingRegistrationRepository;
import com.pet_project.watchstore.repository.UserRepository;
import com.pet_project.watchstore.security.jwt.JwtService;
import com.pet_project.watchstore.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationOtpTest {

    @Test
    void createsLocalUserOnlyAfterValidOneTimeCode() {
        UserRepository users = mock(UserRepository.class);
        PendingRegistrationRepository pendingRepository = mock(PendingRegistrationRepository.class);
        EmailService email = mock(EmailService.class);
        AtomicReference<PendingRegistration> stored = new AtomicReference<>();
        when(pendingRepository.findByEmailForUpdate("customer@example.com"))
                .thenAnswer(invocation -> Optional.ofNullable(stored.get()));
        when(pendingRepository.saveAndFlush(any(PendingRegistration.class)))
                .thenAnswer(invocation -> {
                    PendingRegistration pending = invocation.getArgument(0);
                    stored.set(pending);
                    return pending;
                });
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            stored.set(null);
            return null;
        }).when(pendingRepository).delete(any(PendingRegistration.class));
        AuthService service = new AuthService(mock(AuthenticationManager.class), users,
                new BCryptPasswordEncoder(4), mock(JwtService.class), mock(RefreshTokenService.class),
                pendingRepository, email);

        service.register(new RegisterRequest("Customer", "CUSTOMER@example.com", "Password1"));
        verify(users, never()).save(any(User.class));
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(email).sendRegistrationCode(eq("customer@example.com"), code.capture());
        assertTrue(code.getValue().matches("\\d{6}"));

        assertThrows(InvalidRegistrationCodeException.class,
                () -> service.verifyRegistration(new VerifyRegistrationRequest("customer@example.com", "999999".equals(code.getValue()) ? "000000" : "999999")));
        var response = service.verifyRegistration(new VerifyRegistrationRequest("customer@example.com", code.getValue()));
        assertEquals("customer@example.com", response.email());
        verify(pendingRepository).delete(any(PendingRegistration.class));
        assertThrows(InvalidRegistrationCodeException.class,
                () -> service.verifyRegistration(new VerifyRegistrationRequest("customer@example.com", code.getValue())));
    }
}
