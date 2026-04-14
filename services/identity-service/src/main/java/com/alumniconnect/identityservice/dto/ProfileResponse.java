package com.alumniconnect.identityservice.dto;

import com.alumniconnect.identityservice.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileResponse(
        String email,
        String role,
        String firstName,
        String lastName,
        String bio,
        String avatarUrl) {

    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getEmail(),
                user.getRole().name(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getAvatarUrl());
    }
}
