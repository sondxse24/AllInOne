package com.allinone.controller;

import com.allinone.dto.base.ApiResponse;
import com.allinone.dto.request.auth.LoginRequest;
import com.allinone.dto.request.auth.RefreshTokenRequest;
import com.allinone.dto.response.auth.LoginResponse;
import com.allinone.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .code(200)
                        .message("Login successfully")
                        .result(authService.login(request))
                        .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .code(200)
                        .message("Refresh successfully")
                        .result(authService.refresh(request))
                        .build());
    }
}
