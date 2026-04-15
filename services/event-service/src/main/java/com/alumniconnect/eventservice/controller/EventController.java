package com.alumniconnect.eventservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alumniconnect.eventservice.domain.EventRecord;
import com.alumniconnect.eventservice.service.EventAppService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventAppService events;

    public EventController(EventAppService events) {
        this.events = events;
    }

    @GetMapping
    public ResponseEntity<List<EventRecord>> listEvents(@RequestParam(value = "q", defaultValue = "") String q) {
        return ResponseEntity.ok(events.list(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventRecord> getEvent(@PathVariable Long id) {
        EventRecord event = events.get(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<EventRecord> createEvent(@RequestBody Map<String, String> body) {
        EventRecord event = events.create(
                body.get("title"),
                body.get("date"),
                body.get("description"));
        return ResponseEntity.status(201).body(event);
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, Object>> registerForEvent(
            @PathVariable Long id,
            @RequestParam(value = "email", required = false) String email) {
        Map<String, Object> response = events.register(id, email);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
