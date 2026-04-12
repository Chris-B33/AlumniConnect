package com.alumniconnect.mentorshipservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@RestController
public class MentorshipController {

    private final RestTemplate restTemplate;

    public MentorshipController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/api/mentorship/check")
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
}
