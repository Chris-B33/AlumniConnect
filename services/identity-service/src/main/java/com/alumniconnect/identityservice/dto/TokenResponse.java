package com.alumniconnect.identityservice.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
