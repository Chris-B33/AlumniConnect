package com.alumniconnect.eventservice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistrationEntity, Long> {

    boolean existsByEventIdAndRegistrantEmail(Long eventId, String registrantEmail);
}
