package edu.university.lab.auth.controller;

import edu.university.lab.auth.dto.AuthContextResponse;
import edu.university.lab.auth.dto.LoginRequest;
import edu.university.lab.auth.dto.LoginResponse;
import edu.university.lab.auth.dto.RegisterRequest;
import edu.university.lab.auth.dto.RegisterResponse;
import edu.university.lab.auth.dto.UserProfile;
import edu.university.lab.auth.service.AuthService;
import edu.university.lab.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ApiResponse<UserProfile> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }

    @Operation(summary = "获取当前登录上下文")
    @GetMapping("/context")
    public ApiResponse<AuthContextResponse> currentContext() {
        return ApiResponse.success(authService.currentContext());
    }
}
