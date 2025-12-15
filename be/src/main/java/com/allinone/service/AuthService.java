package com.allinone.service;

import com.allinone.dto.request.auth.LoginRequest;
import com.allinone.dto.response.auth.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refresh(HttpServletRequest refreshRequest);

    void logout(HttpServletRequest request);
}
