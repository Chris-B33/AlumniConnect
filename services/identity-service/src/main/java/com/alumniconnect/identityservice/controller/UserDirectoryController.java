package com.alumniconnect.identityservice.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.alumniconnect.identityservice.domain.User;
import com.alumniconnect.identityservice.domain.UserRole;
import com.alumniconnect.identityservice.dto.ProfileResponse;
import com.alumniconnect.identityservice.repository.UserRepository;

/**
 * Public read APIs used by mentorship-service (and similar) to list alumni and load profiles by email.
 * Secured only by network boundaries in dev; tighten for production if needed.
 */
@RestController
@RequestMapping("/api/users")
public class UserDirectoryController {

    private final UserRepository users;

    public UserDirectoryController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/alumni")
    public List<Map<String, String>> listAlumni() {
        return users.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ALUMNI)
                .map(UserDirectoryController::toAlumniMap)
                .toList();
    }

    @GetMapping("/profile/{email}")
    public ProfileResponse getProfileByEmail(@PathVariable String email) {
        return users.findByEmailIgnoreCase(email)
                .map(ProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private static Map<String, String> toAlumniMap(User u) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("email", u.getEmail());
        if (u.getFirstName() != null) {
            m.put("firstName", u.getFirstName());
        }
        if (u.getLastName() != null) {
            m.put("lastName", u.getLastName());
        }
        if (u.getBio() != null) {
            m.put("bio", u.getBio());
        }
        if (u.getAvatarUrl() != null) {
            m.put("avatarUrl", u.getAvatarUrl());
        }
        return m;
    }
}
