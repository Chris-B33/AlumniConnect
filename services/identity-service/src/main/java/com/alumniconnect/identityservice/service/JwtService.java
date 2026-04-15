package com.alumniconnect.identityservice.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.alumniconnect.identityservice.config.JwtProperties;
import com.alumniconnect.identityservice.domain.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    public record ParsedToken(String userId, String email, String role) {}

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String createAccessToken(String userId, String email, UserRole role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.expirationSeconds());
        SecretKey key = signingKey();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public ParsedToken parseToken(String jwt) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
        return new ParsedToken(
                claims.get("userId", String.class),
                claims.getSubject(),
                claims.get("role", String.class));
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
