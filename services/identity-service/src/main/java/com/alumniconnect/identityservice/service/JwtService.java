package com.alumniconnect.identityservice.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.alumniconnect.identityservice.config.JwtProperties;
import com.alumniconnect.identityservice.domain.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String createAccessToken(String email, UserRole role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.expirationSeconds());
        SecretKey key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }
}
