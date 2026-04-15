package com.alumniconnect.mentorshipservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.*;

@RestController
@RequestMapping("/api")
public class MentorshipController {

    private final RestTemplate restTemplate;

    private Map<Long, Map<String, Object>> mentorships = new HashMap<>();
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

        Map<String, Object> mentorship = new HashMap<>();
        mentorship.put("id", id);
        mentorship.put("status", "REQUESTED");
        mentorship.put("createdAt", new Date());

        mentorships.put(id, mentorship);

        return ResponseEntity.ok(mentorship);
    }

    @PatchMapping("/mentorships/{id}/accept")
    public ResponseEntity<Map<String, Object>> acceptMentorship(@PathVariable Long id) {
        if (!mentorships.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        mentorships.get(id).put("status", "ACCEPTED");

        return ResponseEntity.ok(mentorships.get(id));
    }

    @GetMapping("/mentorships")
    public ResponseEntity<List<Map<String, Object>>> getMentorships() {
        return ResponseEntity.ok(new ArrayList<>(mentorships.values()));
    }
}