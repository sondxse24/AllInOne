package com.allinone.serviceImpl;

import com.allinone.constrant.Role;
import com.allinone.dto.request.auth.LoginRequest;
import com.allinone.dto.response.auth.LoginResponse;
import com.allinone.entity.RefreshToken;
import com.allinone.entity.Users;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        return generateAndSaveTokens(user);
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(String email, String name) {
        Users user = usersRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new Users();
            user.setEmail(email);
            user.setUsername(name);
            user.setRole(Role.MEMBER);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            try {
                usersRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                user = usersRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found after race condition"));
            }
        } else {
            user.setUsername(name);
        }
        usersRepository.save(user);

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        return generateAndSaveTokens(userDetails);
    }

    @Override
    @Transactional
    public LoginResponse refresh(HttpServletRequest request) {
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null) throw new RuntimeException("Refresh token not found");

        String hash = Hash.hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token has been revoked"));

        Jwt jwt = jwtDecoder.decode(refreshToken);
        if (!"refresh".equals(jwt.getClaim("type"))) throw new RuntimeException("Invalid token type");

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
        System.out.println("   -> Service: Found Access Token string? " + (accessToken != null));

        if (accessToken == null) {
            System.out.println("   -> Service: Token is NULL, nothing to delete.");
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            String email = jwt.getSubject();
            System.out.println("   -> Service: Decoded Email: " + email);

            refreshTokenRepository.deleteByEmail(email);
            System.out.println("   -> Service: Deleted from DB successfully.");

        } catch (Exception e) {
            System.err.println("   -> Service: FAILED to decode/delete. Reason: " + e.getMessage());
            throw e; // Ném lỗi ra để Controller bắt được
        }
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