package com.alumniconnect.mentorshipservice.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "mentorship", name = "mentor_availability")
public class MentorAvailabilityEntity {

    @Id
    @Column(name = "mentor_email", length = 320)
    private String mentorEmail;

    @Column(nullable = false)
    private boolean available;

    public String getMentorEmail() {
        return mentorEmail;
    }

    public void setMentorEmail(String mentorEmail) {
        this.mentorEmail = mentorEmail;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
