package com.alumniconnect.mentorshipservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MentorshipController {

    private final RestTemplate restTemplate;

    private Map<Long, String> mentorships = new HashMap<>();
    private long counter = 1;

    public MentorshipController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/mentorship/check")
    @Retry(name = "identityRetry")
    @CircuitBreaker(name = "identityService", fallbackMethod = "fallback")
    public String checkIdentity() {
        return restTemplate.getForObject(
                "http://identity-service/api/identity/status",
                String.class);
    }

    public String fallback(Throwable t) {
        return "Fallback: Identity service unavailable (" + t.getClass().getSimpleName() + ")";
    }

    @PostMapping("/mentorships")
    public ResponseEntity<Map<String, Object>> createMentorship() {
        long id = counter++;
        mentorships.put(id, "REQUESTED");

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("status", "REQUESTED");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/mentorships/{id}/accept")
    public ResponseEntity<Map<String, Object>> acceptMentorship(@PathVariable Long id) {
        if (!mentorships.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        mentorships.put(id, "ACCEPTED");

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("status", "ACCEPTED");

        return ResponseEntity.ok(response);
    }
}