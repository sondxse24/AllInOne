package com.allinone.service;

import com.allinone.dto.request.auth.LoginRequest;
import com.allinone.dto.request.auth.RefreshTokenRequest;
import com.allinone.dto.response.auth.LoginResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
    LoginResponse refresh(RefreshTokenRequest refreshRequest);
}
