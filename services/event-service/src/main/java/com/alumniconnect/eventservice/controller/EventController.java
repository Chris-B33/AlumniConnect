package com.alumniconnect.eventservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    record Event(long id, String title, String date, String description) {}

    private final Map<Long, Event> events = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public EventController() {
        seed(new Event(counter.getAndIncrement(), "Alumni Networking Night", "2025-06-10", "Meet fellow alumni and expand your professional network."));
        seed(new Event(counter.getAndIncrement(), "UL Career Fair 2025", "2025-07-15", "Connect with employers and explore career opportunities."));
        seed(new Event(counter.getAndIncrement(), "Graduation Ceremony 2025", "2025-08-01", "Celebrate the class of 2025 at the annual graduation ceremony."));
    }

    private void seed(Event e) {
        events.put(e.id(), e);
    }

    @GetMapping
    public ResponseEntity<List<Event>> listEvents(@RequestParam(value = "q", defaultValue = "") String q) {
        List<Event> result = events.values().stream()
                .filter(e -> q.isBlank() || e.title().toLowerCase().contains(q.toLowerCase()))
                .sorted(Comparator.comparingLong(Event::id))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        Event event = events.get(id);
        if (event == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Map<String, String> body) {
        long id = counter.getAndIncrement();
        Event event = new Event(id,
                body.getOrDefault("title", "Untitled Event"),
                body.get("date"),
                body.get("description"));
        events.put(id, event);
        return ResponseEntity.status(201).body(event);
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, Object>> registerForEvent(@PathVariable Long id) {
        if (!events.containsKey(id)) return ResponseEntity.notFound().build();
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", id);
        response.put("status", "REGISTERED");
        return ResponseEntity.ok(response);
    }
}
