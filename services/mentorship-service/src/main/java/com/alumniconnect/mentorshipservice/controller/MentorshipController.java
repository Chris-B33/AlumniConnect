package com.alumniconnect.mentorshipservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.alumniconnect.mentorshipservice.dto.MentorshipView;
import com.alumniconnect.mentorshipservice.service.MentorshipAppService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@RestController
@RequestMapping("/api")
public class MentorshipController {

    private final RestTemplate restTemplate;
    private final MentorshipAppService mentorship;

    public MentorshipController(RestTemplate restTemplate, MentorshipAppService mentorship) {
        this.restTemplate = restTemplate;
        this.mentorship = mentorship;
    }

    @GetMapping("/mentorship/check")
    @Retry(name = "identityRetry")
    @CircuitBreaker(name = "identityService", fallbackMethod = "fallback")
    public String checkIdentity() {
        return restTemplate.getForObject("http://identity-service/api/identity/status", String.class);
    }

    public String fallback(Throwable t) {
        return "Fallback: Identity service unavailable (" + t.getClass().getSimpleName() + ")";
    }

    @GetMapping("/mentorships")
    public ResponseEntity<List<MentorshipView>> listMentorships(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "student", defaultValue = "") String student,
            @RequestParam(value = "mentor", defaultValue = "") String mentor) {

        if (!mentor.isBlank()) {
            return ResponseEntity.ok(mentorship.listForMentor(mentor, q));
        }
        return ResponseEntity.ok(mentorship.listForStudent(student, q));
    }

    @GetMapping("/mentorships/{id}")
    public ResponseEntity<MentorshipView> getMentorship(@PathVariable String id) {
        MentorshipView m = mentorship.getById(id);
        if (m == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(m);
    }

    @PostMapping("/mentorships")
    public ResponseEntity<?> createMentorship(@RequestBody Map<String, String> body) {
        try {
            MentorshipView created = mentorship.create(
                    body.get("mentorEmail"),
                    body.get("studentEmail"),
                    body.get("areaOfExpertise"));
            return ResponseEntity.status(201).body(created);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.badRequest().body(Map.of("message", ex.getReason() != null ? ex.getReason() : "Bad request"));
            }
            throw ex;
        }
    }

    @PatchMapping("/mentorships/{id}/accept")
    public ResponseEntity<MentorshipView> acceptMentorship(@PathVariable String id) {
        try {
            return ResponseEntity.ok(mentorship.accept(id));
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            throw ex;
        }
    }

    @PatchMapping("/mentorships/{id}/decline")
    public ResponseEntity<MentorshipView> declineMentorship(@PathVariable String id) {
        try {
            return ResponseEntity.ok(mentorship.decline(id));
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            throw ex;
        }
    }

    @GetMapping("/mentors/me/availability")
    public ResponseEntity<?> getMyAvailability(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        try {
            return ResponseEntity.ok(mentorship.getAvailability(authorization));
        } catch (ResponseStatusException ex) {
            return handleAuth(ex);
        }
    }

    @PutMapping("/mentors/me/availability")
    public ResponseEntity<?> putMyAvailability(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody Map<String, Object> body) {
        try {
            Object av = body != null ? body.get("available") : null;
            boolean available = Boolean.TRUE.equals(av) || "true".equalsIgnoreCase(String.valueOf(av));
            return ResponseEntity.ok(mentorship.setAvailability(authorization, available));
        } catch (ResponseStatusException ex) {
            return handleAuth(ex);
        }
    }

    private static ResponseEntity<Map<String, String>> handleAuth(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(code);
        if (status == null) {
            throw ex;
        }
        String msg = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return switch (status) {
            case UNAUTHORIZED, FORBIDDEN, BAD_GATEWAY, GATEWAY_TIMEOUT ->
                    ResponseEntity.status(status).body(Map.of("message", msg));
            default -> throw ex;
        };
    }
}
