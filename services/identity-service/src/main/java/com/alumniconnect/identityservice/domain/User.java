package com.alumniconnect.identityservice.domain;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final Instant createdAt;
    private final String firstName;
    private final String lastName;
    private final String bio;
    private final String avatarUrl;

    public User(String id, String email, String passwordHash, UserRole role, Instant createdAt,
            String firstName, String lastName, String bio, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public static User newUser(String email, String passwordHash, UserRole role) {
        return new User(UUID.randomUUID().toString(), email, passwordHash, role, Instant.now(),
                null, null, null, null);
    }

    public User withProfile(String firstName, String lastName, String bio) {
        return new User(id, email, passwordHash, role, createdAt, firstName, lastName, bio, avatarUrl);
    }

    public User withAvatarUrl(String avatarUrl) {
        return new User(id, email, passwordHash, role, createdAt, firstName, lastName, bio, avatarUrl);
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
}
