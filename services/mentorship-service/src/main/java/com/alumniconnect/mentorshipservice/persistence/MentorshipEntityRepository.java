package com.alumniconnect.mentorshipservice.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorshipEntityRepository extends JpaRepository<MentorshipEntity, Long> {

    List<MentorshipEntity> findByMentorEmail(String mentorEmail);

    Optional<MentorshipEntity> findByStudentEmailAndMentorEmail(String studentEmail, String mentorEmail);
}
