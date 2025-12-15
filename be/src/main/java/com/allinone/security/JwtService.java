package com.allinone.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtService {

    final JwtEncoder jwtEncoder;

    @Value("${jwt.access-expiration}")
    long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    long refreshExpiration;

    public String generateAccessToken(UserDetails user) {
        return generateToken(user, accessExpiration, "access");
    }

    public String generateRefreshToken(UserDetails user) {
        return generateToken(user, refreshExpiration, "refresh");
    }

    String generateToken(
            UserDetails user,
            long expirationSeconds,
            String type
    ) {
        Instant now = Instant.now();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(user.getUsername())
                .claim("type", type)
                .claim("email", user.getUsername())
                .claim("roles", user.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims)
        ).getTokenValue();
    }
}