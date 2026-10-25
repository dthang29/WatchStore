package com.pet_project.watchstore.auth;

import com.pet_project.watchstore.auth.dto.AuthResponse;
import com.pet_project.watchstore.auth.dto.ForgotPasswordRequest;
import com.pet_project.watchstore.auth.dto.LoginRequest;
import com.pet_project.watchstore.auth.dto.RegisterRequest;
import com.pet_project.watchstore.auth.dto.ResendRegistrationCodeRequest;
import com.pet_project.watchstore.auth.dto.ResetPasswordRequest;
import com.pet_project.watchstore.auth.dto.VerifyRegistrationRequest;
import com.pet_project.watchstore.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenCookie refreshTokenCookie;

    public AuthController(AuthService authService, PasswordResetService passwordResetService,
                          RefreshTokenCookie refreshTokenCookie) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/register/verify")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse verifyRegistration(@Valid @RequestBody VerifyRegistrationRequest request) {
        return authService.verifyRegistration(request);
    }

    @PostMapping("/register/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendRegistrationCode(@Valid @RequestBody ResendRegistrationCodeRequest request) {
        authService.resendRegistrationCode(request);
    }

    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestCode(request);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return withRefreshCookie(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
        return withRefreshCookie(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString())
                .build();
    }

    /** Refresh thất bại: trả 401 và xoá cookie để trình duyệt không gửi lại token hỏng. */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString())
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(AuthService.AuthResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(result.refreshToken()).toString())
                .body(result.response());
    }
}
