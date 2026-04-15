package com.alumniconnect.mentorshipservice.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alumniconnect.mentorshipservice.dto.MeDto;
import com.alumniconnect.mentorshipservice.dto.MentorshipView;
import com.alumniconnect.mentorshipservice.persistence.MentorAvailabilityEntity;
import com.alumniconnect.mentorshipservice.persistence.MentorAvailabilityRepository;
import com.alumniconnect.mentorshipservice.persistence.MentorshipEntity;
import com.alumniconnect.mentorshipservice.persistence.MentorshipEntityRepository;

@Service
public class MentorshipAppService {

    private final RestTemplate restTemplate;
    private final MentorshipEntityRepository mentorships;
    private final MentorAvailabilityRepository availability;
    private final ObjectMapper objectMapper;

    public MentorshipAppService(
            RestTemplate restTemplate,
            MentorshipEntityRepository mentorships,
            MentorAvailabilityRepository availability,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.mentorships = mentorships;
        this.availability = availability;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<MentorshipView> listForMentor(String mentor, String q) {
        String mentorNorm = mentor.trim().toLowerCase(Locale.ROOT);
        String term = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return mentorships.findByMentorEmail(mentorNorm).stream()
                .map(this::toView)
                .map(this::enrich)
                .filter(m -> term.isEmpty()
                        || (m.studentName() != null && m.studentName().toLowerCase(Locale.ROOT).contains(term))
                        || (m.studentEmail() != null && m.studentEmail().toLowerCase(Locale.ROOT).contains(term)))
                .sorted(Comparator.comparingLong(m -> parseIdOrMax(m.id())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MentorshipView> listForStudent(String student, String q) {
        List<Map<String, String>> alumni = fetchAlumni();
        List<MentorshipView> result = new ArrayList<>();
        String studentNorm = student == null ? "" : student.trim().toLowerCase(Locale.ROOT);

        for (Map<String, String> a : alumni) {
            String mentorEmail = a.get("email");
            if (mentorEmail == null || mentorEmail.isBlank()) {
                continue;
            }
            String mentorNorm = mentorEmail.trim().toLowerCase(Locale.ROOT);

            Optional<MentorshipEntity> existing = studentNorm.isEmpty()
                    ? Optional.empty()
                    : mentorships.findByStudentEmailAndMentorEmail(studentNorm, mentorNorm);

            boolean hasRelationship = existing.isPresent();
            boolean optedIn = availability.findById(mentorNorm).map(MentorAvailabilityEntity::isAvailable).orElse(false);
            if (!hasRelationship && !optedIn) {
                continue;
            }

            MentorshipView raw = existing.map(this::toView).orElseGet(() -> new MentorshipView(
                    "",
                    mentorNorm,
                    null,
                    null,
                    null,
                    studentNorm.isEmpty() ? null : studentNorm,
                    null,
                    null,
                    "AVAILABLE"));
            result.add(enrich(raw));
        }

        if (q != null && !q.isBlank()) {
            String lq = q.toLowerCase(Locale.ROOT);
            result = result.stream()
                    .filter(m -> (m.mentorName() != null && m.mentorName().toLowerCase(Locale.ROOT).contains(lq))
                            || (m.mentorBio() != null && m.mentorBio().toLowerCase(Locale.ROOT).contains(lq)))
                    .collect(Collectors.toList());
        }

        result.sort(Comparator.comparingLong(m -> parseIdOrMax(m.id())));
        return result;
    }

    private static long parseIdOrMax(String id) {
        if (id == null || id.isBlank()) {
            return Long.MAX_VALUE;
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    @Transactional(readOnly = true)
    public MentorshipView getById(String id) {
        long pk;
        try {
            pk = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
        return mentorships.findById(pk).map(this::toView).map(this::enrich).orElse(null);
    }

    @Transactional
    public MentorshipView create(String mentorEmail, String studentEmail, String areaOfExpertise) {
        String mentorNorm = mentorEmail.trim().toLowerCase(Locale.ROOT);
        List<Map<String, String>> alumni = fetchAlumni();
        boolean isAlumni = alumni.stream().anyMatch(a -> mentorNorm.equalsIgnoreCase(a.get("email")));
        if (!isAlumni) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentor must be a registered alumni account");
        }
        String studentNorm = studentEmail == null ? null : studentEmail.trim().toLowerCase(Locale.ROOT);
        MentorshipEntity e = new MentorshipEntity();
        e.setMentorEmail(mentorNorm);
        e.setStudentEmail(studentNorm);
        e.setAreaOfExpertise(areaOfExpertise);
        e.setStatus("REQUESTED");
        MentorshipEntity saved = mentorships.save(e);
        return enrich(toView(saved));
    }

    @Transactional
    public MentorshipView accept(String id) {
        return updateStatus(id, "ACCEPTED");
    }

    @Transactional
    public MentorshipView decline(String id) {
        return updateStatus(id, "DECLINED");
    }

    private MentorshipView updateStatus(String id, String status) {
        long pk;
        try {
            pk = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MentorshipEntity m = mentorships.findById(pk)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        m.setStatus(status);
        mentorships.save(m);
        return enrich(toView(m));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAvailability(String authorizationHeader) {
        MeDto me = fetchMe(authorizationHeader);
        if (!"ALUMNI".equals(me.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only alumni can manage availability");
        }
        String email = me.email().trim().toLowerCase(Locale.ROOT);
        boolean av = availability.findById(email).map(MentorAvailabilityEntity::isAvailable).orElse(false);
        return Map.of("email", email, "available", av);
    }

    @Transactional
    public Map<String, Object> setAvailability(String authorizationHeader, boolean available) {
        MeDto me = fetchMe(authorizationHeader);
        if (!"ALUMNI".equals(me.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only alumni can manage availability");
        }
        String email = me.email().trim().toLowerCase(Locale.ROOT);
        MentorAvailabilityEntity row = availability.findById(email).orElseGet(() -> {
            MentorAvailabilityEntity n = new MentorAvailabilityEntity();
            return n;
        });
        row.setMentorEmail(email);
        row.setAvailable(available);
        availability.save(row);
        return Map.of("email", email, "available", available);
    }

    private MeDto fetchMe(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        try {
            MeDto body = restTemplate.exchange(
                    "http://identity-service/api/identity/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    MeDto.class).getBody();
            if (body == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }
            return body;
        } catch (HttpStatusCodeException e) {
            HttpStatus st = HttpStatus.valueOf(e.getStatusCode().value());
            String fromIdentity = parseIdentityErrorBody(e);
            if (st == HttpStatus.UNAUTHORIZED) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        fromIdentity != null ? fromIdentity : "Invalid or expired token");
            }
            throw new ResponseStatusException(
                    st,
                    fromIdentity != null ? fromIdentity : "Identity service error: " + st.value());
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Identity service unreachable (timeout or network). Is identity-service registered in Eureka?");
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Identity service error: " + e.getMessage());
        }
    }

    private String parseIdentityErrorBody(HttpStatusCodeException e) {
        try {
            String raw = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            JsonNode n = objectMapper.readTree(raw);
            if (n.has("message") && !n.get("message").isNull()) {
                return n.get("message").asText();
            }
            if (n.has("detail") && !n.get("detail").isNull()) {
                return n.get("detail").asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    private MentorshipView toView(MentorshipEntity m) {
        return new MentorshipView(
                String.valueOf(m.getId()),
                m.getMentorEmail(),
                null,
                null,
                null,
                m.getStudentEmail(),
                null,
                m.getAreaOfExpertise(),
                m.getStatus());
    }

    private MentorshipView enrich(MentorshipView m) {
        Map<String, Object> mentorProfile = fetchProfile(m.mentorEmail());
        String mentorName = resolveDisplayName(m.mentorEmail(), m.mentorName(), mentorProfile);
        String mentorBio = m.mentorBio() != null ? m.mentorBio()
                : (mentorProfile != null ? (String) mentorProfile.get("bio") : null);
        String mentorAvatarUrl = m.mentorAvatarUrl() != null ? m.mentorAvatarUrl()
                : (mentorProfile != null ? (String) mentorProfile.get("avatarUrl") : null);
        String studentName = resolveDisplayName(m.studentEmail(), m.studentName(), fetchProfile(m.studentEmail()));
        return new MentorshipView(
                m.id(),
                m.mentorEmail(),
                mentorName,
                mentorBio,
                mentorAvatarUrl,
                m.studentEmail(),
                studentName,
                m.areaOfExpertise(),
                m.status());
    }

    private String resolveDisplayName(String email, String cached, Map<String, Object> profile) {
        if (email == null || email.isBlank()) {
            return cached;
        }
        if (cached != null && !cached.isBlank() && !cached.equals(email)) {
            return cached;
        }
        if (profile == null) {
            return email;
        }
        String first = (String) profile.get("firstName");
        String last = (String) profile.get("lastName");
        if (first != null && !first.isBlank()) {
            return last != null && !last.isBlank() ? first + " " + last : first;
        }
        return email;
    }

    private Map<String, Object> fetchProfile(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
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
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, String>>>() {}).getBody();
            return result != null ? result : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
}
