package com.alumniconnect.identityservice.domain;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final Instant createdAt;

    public User(String id, String email, String passwordHash, UserRole role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User newUser(String email, String passwordHash, UserRole role) {
        return new User(UUID.randomUUID().toString(), email, passwordHash, role, Instant.now());
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
