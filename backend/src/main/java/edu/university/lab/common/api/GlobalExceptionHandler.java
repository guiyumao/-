package edu.university.lab.common.api;

import edu.university.lab.common.constant.Messages;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse(Messages.VALIDATION_FAILED);
        return ApiResponse.fail(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        return ApiResponse.fail(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ApiResponse.fail(exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Void> handleIllegalStateException(IllegalStateException exception) {
        return ApiResponse.fail(exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException exception) {
        return ApiResponse.fail(Messages.USERNAME_PASSWORD_ERROR);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ApiResponse<Void> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        return ApiResponse.fail(Messages.USER_NOT_FOUND);
    }

    @ExceptionHandler(DisabledException.class)
    public ApiResponse<Void> handleDisabledException(DisabledException exception) {
        return ApiResponse.fail("账号已被停用");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("系统异常", exception);
        return ApiResponse.fail(Messages.SYSTEM_ERROR);
    }
}
