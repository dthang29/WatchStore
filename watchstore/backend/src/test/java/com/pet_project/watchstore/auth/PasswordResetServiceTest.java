package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.auth.dto.ForgotPasswordRequest;
import com.pet_project.watchstore.auth.dto.ResetPasswordRequest;
import com.pet_project.watchstore.entity.PasswordResetRequest;
import com.pet_project.watchstore.entity.User;
import com.pet_project.watchstore.repository.PasswordResetRequestRepository;
import com.pet_project.watchstore.repository.UserRepository;
import com.pet_project.watchstore.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    @Test
    void resetsPasswordOnlyWithValidOneTimeCodeAndRevokesSessions() {
        UserRepository users = mock(UserRepository.class);
        PasswordResetRequestRepository requests = mock(PasswordResetRequestRepository.class);
        EmailService email = mock(EmailService.class);
        RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        User user = User.local("customer@example.com", encoder.encode("OldPassword1"), "Customer");
        user.setId(42);
        when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));

        AtomicReference<PasswordResetRequest> stored = new AtomicReference<>();
        when(requests.findByEmailForUpdate("customer@example.com"))
                .thenAnswer(invocation -> Optional.ofNullable(stored.get()));
        when(requests.saveAndFlush(any(PasswordResetRequest.class))).thenAnswer(invocation -> {
            PasswordResetRequest pending = invocation.getArgument(0);
            stored.set(pending);
            return pending;
        });

        PasswordResetService service = new PasswordResetService(users, requests, encoder, email, refreshTokens);
        service.requestCode(new ForgotPasswordRequest(" CUSTOMER@example.com "));
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(email).sendPasswordResetCode(eq("customer@example.com"), code.capture());
        assertTrue(code.getValue().matches("\\d{6}"));

        String wrongCode = code.getValue().equals("999999") ? "000000" : "999999";
        assertThrows(InvalidPasswordResetCodeException.class,
                () -> service.resetPassword(new ResetPasswordRequest("customer@example.com", wrongCode, "NewPassword1")));
        assertTrue(encoder.matches("OldPassword1", user.getPasswordHash()));
        service.resetPassword(new ResetPasswordRequest("customer@example.com", code.getValue(), "NewPassword1"));
        assertTrue(encoder.matches("NewPassword1", user.getPasswordHash()));
        assertNotEquals("NewPassword1", user.getPasswordHash());
        verify(requests).delete(stored.get());
        verify(refreshTokens).revokeAllForUser(42);
    }

    @Test
    void doesNotRevealUnknownOrGoogleOnlyAccounts() {
        UserRepository users = mock(UserRepository.class);
        PasswordResetRequestRepository requests = mock(PasswordResetRequestRepository.class);
        EmailService email = mock(EmailService.class);
        PasswordResetService service = new PasswordResetService(users, requests,
                new BCryptPasswordEncoder(4), email, mock(RefreshTokenService.class));
        User googleOnly = User.google("google-id", "google@example.com", "Google User", null);
        when(users.findByEmail("google@example.com")).thenReturn(Optional.of(googleOnly));

        service.requestCode(new ForgotPasswordRequest("unknown@example.com"));
        service.requestCode(new ForgotPasswordRequest("google@example.com"));

        verify(requests, never()).saveAndFlush(any());
        verify(email, never()).sendPasswordResetCode(any(), any());
    }
}
