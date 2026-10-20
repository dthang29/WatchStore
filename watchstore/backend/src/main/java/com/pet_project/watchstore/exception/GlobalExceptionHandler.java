package com.pet_project.watchstore.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Trả lỗi dạng ProblemDetail. Frontend đọc field "detail" để hiển thị.
 * ResponseStatusException (vd: 409 email trùng) đã được lớp cha xử lý sẵn.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ex.getBody();
        problem.setDetail(errors.values().stream().findFirst().orElse("Dữ liệu không hợp lệ"));
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials() {
        // Không phân biệt "sai email" hay "sai mật khẩu" để tránh dò tài khoản
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng");
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Tài khoản đã bị khoá");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Xác thực thất bại");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Dữ liệu bị trùng hoặc không hợp lệ");
    }
}
