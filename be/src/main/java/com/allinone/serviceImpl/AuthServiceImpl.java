package com.allinone.serviceImpl;

import com.allinone.dto.request.auth.LoginRequest;
import com.allinone.dto.response.auth.LoginResponse;
import com.allinone.entity.RefreshToken;
import com.allinone.repository.RefreshTokenRepository;
import com.allinone.security.CustomUserDetails;
import com.allinone.security.CustomUserDetailsService;
import com.allinone.security.JwtService;
import com.allinone.service.AuthService;
import com.allinone.utils.Hash;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    AuthenticationManager authenticationManager;
    JwtService jwtService;
    JwtDecoder jwtDecoder;
    CustomUserDetailsService userDetailsService;
    RefreshTokenRepository refreshTokenRepository;

    @Override
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

        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(Hash.hashToken(refreshToken));
        assert user != null;
        entity.setEmail(user.getUsername());
        entity.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        entity.setRevoked(false);

        refreshTokenRepository.save(entity);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    public LoginResponse refresh(HttpServletRequest request) {

        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token not found");
        }

        String hash = Hash.hashToken(refreshToken);

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new RuntimeException("Refresh token revoked"));

        Jwt jwt = jwtDecoder.decode(refreshToken);

        if (!"refresh".equals(jwt.getClaim("type"))) {
            throw new RuntimeException("Invalid token type");
        }

        String email = jwt.getClaim("email");

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        CustomUserDetails user =
                (CustomUserDetails) userDetailsService
                        .loadUserByUsername(email);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        RefreshToken newEntity = new RefreshToken();
        newEntity.setTokenHash(Hash.hashToken(newRefreshToken));
        newEntity.setEmail(email);
        newEntity.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        newEntity.setRevoked(false);

        refreshTokenRepository.save(newEntity);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(HttpServletRequest request) {

        String accessToken = extractCookie(request, "access_token");
        if (accessToken == null) return;

        Jwt jwt = jwtDecoder.decode(accessToken);
        String email = jwt.getSubject();

        refreshTokenRepository.deleteByEmail(email);
    }

    private String extractCookie(
            HttpServletRequest request,
            String name
    ) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
