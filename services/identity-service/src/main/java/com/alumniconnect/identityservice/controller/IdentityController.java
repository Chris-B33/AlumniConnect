package com.alumniconnect.identityservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.alumniconnect.identityservice.dto.MeResponse;
import com.alumniconnect.identityservice.repository.UserRepository;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final UserRepository users;

    public IdentityController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/status")
    public String status() {
        return "Identity service is UP";
    }

    @GetMapping("/me")
    public MeResponse me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmailIgnoreCase(email)
                .map(u -> new MeResponse(u.getId(), u.getEmail(), u.getRole().name()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
