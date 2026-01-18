package com.allinone.service;

import com.allinone.dto.response.auth.LoginGoogleResponse;

public interface AuthGoogleService {
    LoginGoogleResponse authenticate(String code);
}
