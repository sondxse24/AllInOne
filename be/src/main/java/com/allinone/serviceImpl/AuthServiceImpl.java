package com.allinone.serviceImpl;

import com.allinone.dto.request.auth.LoginRequest;
import com.allinone.dto.request.auth.RefreshTokenRequest;
import com.allinone.dto.response.auth.LoginResponse;
import com.allinone.security.CustomUserDetails;
import com.allinone.security.CustomUserDetailsService;
import com.allinone.security.JwtService;
import com.allinone.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    final AuthenticationManager authenticationManager;
    final JwtService jwtService;
    final JwtDecoder jwtDecoder;
    final CustomUserDetailsService userDetailsService;

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(RefreshTokenRequest request) {

        Jwt jwt = jwtDecoder.decode(request.refreshToken());

        if (!"refresh".equals(jwt.getClaim("type"))) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwt.getClaim("email");

        CustomUserDetails user =
                (CustomUserDetails) userDetailsService
                        .loadUserByUsername(email);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }
}
