package com.alumniconnect.identityservice.domain;

public enum UserRole {
    STUDENT,
    ALUMNI;

    public static UserRole fromApi(String role) {
        if (role == null) {
            throw new IllegalArgumentException("role is required");
        }
        return switch (role.trim()) {
            case "Student" -> STUDENT;
            case "Alumni" -> ALUMNI;
            default -> throw new IllegalArgumentException("role must be Student or Alumni");
        };
    }
}
