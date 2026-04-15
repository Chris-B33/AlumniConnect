package com.alumniconnect.identityservice.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alumniconnect.identityservice.dto.AdminUserResponse;
import com.alumniconnect.identityservice.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository users;

    public AdminController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ALUMNI')")
    public List<AdminUserResponse> listUsers() {
        return users.findAll().stream()
                .map(u -> new AdminUserResponse(u.getId(), u.getEmail(), u.getRole().name()))
                .toList();
    }
}
