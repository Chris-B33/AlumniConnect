package com.alumniconnect.eventservice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventEntityRepository extends JpaRepository<EventEntity, Long> {
}
