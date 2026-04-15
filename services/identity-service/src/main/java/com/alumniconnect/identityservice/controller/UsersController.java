package com.alumniconnect.identityservice.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.alumniconnect.identityservice.domain.UserRole;
import com.alumniconnect.identityservice.dto.ProfileResponse;
import com.alumniconnect.identityservice.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserRepository users;

    public UsersController(UserRepository users) {
        this.users = users;
    }

    /** Returns all alumni accounts — consumed by the mentorship service. */
    @GetMapping("/alumni")
    public List<ProfileResponse> listAlumni() {
        return users.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ALUMNI)
                .map(ProfileResponse::from)
                .collect(Collectors.toList());
    }

    /** Returns a single user's profile by email — consumed by the mentorship service for name/bio enrichment. */
    @GetMapping("/profile/{email}")
    public ProfileResponse getProfileByEmail(@PathVariable String email) {
        return users.findByEmailIgnoreCase(email)
                .map(ProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
