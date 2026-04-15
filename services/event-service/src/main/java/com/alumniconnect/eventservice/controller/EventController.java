package com.alumniconnect.eventservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private Map<Long, Map<String, Object>> events = new HashMap<>();
    private long counter = 1;

    // ✅ POST /api/events
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Map<String, Object> body) {
        long id = counter++;

        Map<String, Object> event = new HashMap<>();
        event.put("id", id);
        event.put("title", body.getOrDefault("title", "Untitled Event"));
        event.put("createdAt", new Date());
        event.put("status", "CREATED");

        events.put(id, event);

        return ResponseEntity.ok(event);
    }

    // ✅ GET /api/events
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getEvents() {
        return ResponseEntity.ok(new ArrayList<>(events.values()));
    }

    // ✅ POST /api/events/{id}/register (already expected)
    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, Object>> register(@PathVariable Long id) {
        if (!events.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("eventId", id);
        response.put("status", "REGISTERED");

        return ResponseEntity.ok(response);
    }
}