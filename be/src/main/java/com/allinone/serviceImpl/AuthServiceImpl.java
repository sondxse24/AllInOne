package com.allinone.serviceImpl;

import com.allinone.constrant.AuthProvider;
import com.allinone.constrant.Role;
import com.allinone.dto.request.auth.LoginRequest;
import com.allinone.dto.response.auth.LoginResponse;
import com.allinone.entity.RefreshToken;
import com.allinone.entity.Users;
import com.allinone.exception.AppException;
import com.allinone.exception.ErrorCode;
import com.allinone.repository.RefreshTokenRepository;
import com.allinone.repository.UsersRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    AuthenticationManager authenticationManager;
    JwtService jwtService;
    JwtDecoder jwtDecoder;
    CustomUserDetailsService userDetailsService;
    RefreshTokenRepository refreshTokenRepository;
    UsersRepository usersRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            return generateAndSaveTokens(user);
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(String email, String name, String avatar) {
        Users user = usersRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new Users();
            user.setEmail(email);
            user.setUsername(name);
            user.setAvatar(avatar);
            user.setProvider(AuthProvider.GOOGLE);
            user.setRole(Role.MEMBER);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setEnabled(true);

            try {
                usersRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                user = usersRepository.findByEmail(email)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            }
        } else {
            user.setAvatar(avatar);
            user.setUsername(name);
            usersRepository.save(user);
        }

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        return generateAndSaveTokens(userDetails);
    }

    @Override
    @Transactional
    public LoginResponse refresh(HttpServletRequest request) {

        String refreshToken = extractCookie(request, "refresh_token");

        if (refreshToken == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        String hash = Hash.hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_REVOKED));

        Jwt jwt = jwtDecoder.decode(refreshToken);
        if (!"refresh".equals(jwt.getClaim("type"))) {
            throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
        }

        String email = jwt.getClaim("email");

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        return generateAndSaveTokens(user);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {

        String accessToken = extractCookie(request, "access_token");
        if (accessToken == null) return;

        Jwt jwt = jwtDecoder.decode(accessToken);
        String email = jwt.getSubject();
        refreshTokenRepository.deleteByEmail(email);
    }

    private LoginResponse generateAndSaveTokens(CustomUserDetails user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(Hash.hashToken(refreshToken));
        entity.setEmail(user.getUsername());
        entity.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)); // 7 ngày
        entity.setRevoked(false);

        refreshTokenRepository.save(entity);

        return new LoginResponse(accessToken, refreshToken);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}