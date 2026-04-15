package com.alumniconnect.eventservice.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alumniconnect.eventservice.domain.EventRecord;
import com.alumniconnect.eventservice.persistence.EventEntity;
import com.alumniconnect.eventservice.persistence.EventEntityRepository;
import com.alumniconnect.eventservice.persistence.EventRegistrationEntity;
import com.alumniconnect.eventservice.persistence.EventRegistrationRepository;

@Service
public class EventAppService {

    private final EventEntityRepository repo;
    private final EventRegistrationRepository registrations;

    public EventAppService(EventEntityRepository repo, EventRegistrationRepository registrations) {
        this.repo = repo;
        this.registrations = registrations;
    }

    @Transactional(readOnly = true)
    public List<EventRecord> list(String q) {
        String term = q == null ? "" : q.trim().toLowerCase();
        return repo.findAll().stream()
                .filter(e -> term.isEmpty() || e.getTitle().toLowerCase().contains(term))
                .sorted(Comparator.comparingLong(EventEntity::getId))
                .map(EventAppService::toRecord)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventRecord get(long id) {
        return repo.findById(id).map(EventAppService::toRecord).orElse(null);
    }

    @Transactional
    public EventRecord create(String title, String date, String description) {
        EventEntity e = new EventEntity();
        e.setTitle(title != null && !title.isBlank() ? title : "Untitled Event");
        e.setEventDate(date);
        e.setDescription(description);
        EventEntity saved = repo.save(e);
        return toRecord(saved);
    }

    public boolean exists(long id) {
        return repo.existsById(id);
    }

    /**
     * Records registration in {@code event.event_registrations} when {@code email} is provided;
     * otherwise returns the same success payload without persisting (backwards compatible).
     */
    @Transactional
    public Map<String, Object> register(long eventId, String emailOptional) {
        if (!exists(eventId)) {
            return null;
        }
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("status", "REGISTERED");
        if (emailOptional != null && !emailOptional.isBlank()) {
            String em = emailOptional.trim().toLowerCase(Locale.ROOT);
            if (!registrations.existsByEventIdAndRegistrantEmail(eventId, em)) {
                EventRegistrationEntity row = new EventRegistrationEntity();
                row.setEventId(eventId);
                row.setRegistrantEmail(em);
                row.setRegisteredAt(Instant.now());
                registrations.save(row);
            }
        }
        return response;
    }

    private static EventRecord toRecord(EventEntity e) {
        return new EventRecord(e.getId(), e.getTitle(), e.getEventDate(), e.getDescription());
    }
}
