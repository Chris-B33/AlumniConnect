package com.alumniconnect.eventservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @PostMapping("/{id}/register")
    public ResponseEntity<Map<String, Object>> registerForEvent(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        response.put("eventId", id);
        response.put("status", "REGISTERED");

        return ResponseEntity.ok(response);
    }
}