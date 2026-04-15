package com.alumniconnect.mentorshipservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MentorshipController {

    record Mentorship(
            String id,
            String mentorEmail, String mentorName, String mentorBio, String mentorAvatarUrl,
            String studentEmail, String studentName,
            String areaOfExpertise, String status) {}

    private final RestTemplate restTemplate;
    private final Map<String, Mentorship> mentorships = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public MentorshipController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

    // Student view: GET /api/mentorships?student=<email>&q=<term>
    //   Shows all alumni; status reflects THIS student's request only.
    // Alumni view:  GET /api/mentorships?mentor=<email>&q=<term>
    //   Shows only incoming requests directed at this mentor.
    @GetMapping("/mentorships")
    public ResponseEntity<List<Mentorship>> listMentorships(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "student", defaultValue = "") String student,
            @RequestParam(value = "mentor", defaultValue = "") String mentor) {

        if (!mentor.isBlank()) {
            // Alumni view
            List<Mentorship> requests = mentorships.values().stream()
                    .filter(m -> mentor.equalsIgnoreCase(m.mentorEmail()))
                    .filter(m -> q.isBlank() ||
                            (m.studentName() != null && m.studentName().toLowerCase().contains(q.toLowerCase())) ||
                            (m.studentEmail() != null && m.studentEmail().toLowerCase().contains(q.toLowerCase())))
                    .map(m -> enrich(m))
                    .sorted(Comparator.comparing(Mentorship::id))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(requests);
        }

        // Student view — all alumni, merged with THIS student's request if one exists
        List<Map<String, String>> alumni = fetchAlumni();
        List<Mentorship> result = new ArrayList<>();
        for (Map<String, String> a : alumni) {
            String mentorEmail = a.get("email");
            // Only match a request that belongs to this specific student
            Optional<Mentorship> existing = student.isBlank() ? Optional.empty() :
                    mentorships.values().stream()
                            .filter(m -> mentorEmail.equalsIgnoreCase(m.mentorEmail())
                                    && student.equalsIgnoreCase(m.studentEmail()))
                            .findFirst();

            Mentorship raw = existing.orElse(
                    new Mentorship(mentorEmail, mentorEmail, null, null, null, student, null, null, "AVAILABLE"));
            result.add(enrich(raw));
        }

        if (!q.isBlank()) {
            String lq = q.toLowerCase();
            result = result.stream()
                    .filter(m -> m.mentorName().toLowerCase().contains(lq) ||
                            (m.mentorBio() != null && m.mentorBio().toLowerCase().contains(lq)))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/mentorships/{id}")
    public ResponseEntity<Mentorship> getMentorship(@PathVariable String id) {
        Mentorship m = mentorships.get(id);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(enrich(m));
    }

    @PostMapping("/mentorships")
    public ResponseEntity<?> createMentorship(@RequestBody Map<String, String> body) {
        String mentorEmail = body.get("mentorEmail");
        if (mentorEmail == null || mentorEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "mentorEmail is required"));
        }

        List<Map<String, String>> alumni = fetchAlumni();
        boolean isAlumni = alumni.stream().anyMatch(a -> mentorEmail.equalsIgnoreCase(a.get("email")));
        if (!isAlumni) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mentor must be a registered alumni account"));
        }

        String id = String.valueOf(counter.getAndIncrement());
        Mentorship m = new Mentorship(id, mentorEmail, null, null, null,
                body.get("studentEmail"), null, body.get("areaOfExpertise"), "REQUESTED");
        mentorships.put(id, m);
        return ResponseEntity.status(201).body(enrich(m));
    }

    @PatchMapping("/mentorships/{id}/accept")
    public ResponseEntity<Mentorship> acceptMentorship(@PathVariable String id) {
        Mentorship m = mentorships.get(id);
        if (m == null) return ResponseEntity.notFound().build();
        Mentorship updated = new Mentorship(m.id(), m.mentorEmail(), m.mentorName(), m.mentorBio(), m.mentorAvatarUrl(),
                m.studentEmail(), m.studentName(), m.areaOfExpertise(), "ACCEPTED");
        mentorships.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/mentorships/{id}/decline")
    public ResponseEntity<Mentorship> declineMentorship(@PathVariable String id) {
        Mentorship m = mentorships.get(id);
        if (m == null) return ResponseEntity.notFound().build();
        Mentorship updated = new Mentorship(m.id(), m.mentorEmail(), m.mentorName(), m.mentorBio(), m.mentorAvatarUrl(),
                m.studentEmail(), m.studentName(), m.areaOfExpertise(), "DECLINED");
        mentorships.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    // Populate mentorName/mentorBio and studentName from the identity-service profile endpoint.
    // Falls back to email as display name if the endpoint is unavailable or returns no name.
    private Mentorship enrich(Mentorship m) {
        Map<String, Object> mentorProfile = fetchProfile(m.mentorEmail());
        String mentorName = resolveDisplayName(m.mentorEmail(), m.mentorName(), mentorProfile);
        String mentorBio = m.mentorBio() != null ? m.mentorBio()
                : (mentorProfile != null ? (String) mentorProfile.get("bio") : null);
        String mentorAvatarUrl = m.mentorAvatarUrl() != null ? m.mentorAvatarUrl()
                : (mentorProfile != null ? (String) mentorProfile.get("avatarUrl") : null);
        String studentName = resolveDisplayName(m.studentEmail(), m.studentName(), fetchProfile(m.studentEmail()));
        return new Mentorship(m.id(), m.mentorEmail(), mentorName, mentorBio, mentorAvatarUrl,
                m.studentEmail(), studentName, m.areaOfExpertise(), m.status());
    }

    private String resolveDisplayName(String email, String cached, Map<String, Object> profile) {
        if (email == null || email.isBlank()) return cached;
        if (cached != null && !cached.isBlank() && !cached.equals(email)) return cached;
        if (profile == null) return email;
        String first = (String) profile.get("firstName");
        String last  = (String) profile.get("lastName");
        if (first != null && !first.isBlank()) {
            return last != null && !last.isBlank() ? first + " " + last : first;
        }
        return email;
    }

    private Map<String, Object> fetchProfile(String email) {
        if (email == null || email.isBlank()) return null;
        try {
            String url = UriComponentsBuilder
                    .fromUriString("http://identity-service/api/users/profile/{email}")
                    .buildAndExpand(email)
                    .toUriString();
            return restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, String>> fetchAlumni() {
        try {
            List<Map<String, String>> result = restTemplate.exchange(
                    "http://identity-service/api/users/alumni",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, String>>>() {}).getBody();
            return result != null ? result : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
}
