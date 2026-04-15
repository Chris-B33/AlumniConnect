package com.alumniconnect.mentorshipservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MeDto(String id, String email, String role) {
}
