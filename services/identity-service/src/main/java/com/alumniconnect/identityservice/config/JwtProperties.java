package com.alumniconnect.identityservice.config;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expirationSeconds) {

    public JwtProperties {
        Objects.requireNonNull(secret, "jwt.secret");
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("jwt.secret must be at least 32 bytes for HS256");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("jwt.expiration-seconds must be positive");
        }
    }
}
