package com.alumniconnect.mentorshipservice.dto;

public record MentorshipView(
        String id,
        String mentorEmail,
        String mentorName,
        String mentorBio,
        String mentorAvatarUrl,
        String studentEmail,
        String studentName,
        String areaOfExpertise,
        String status) {
}
