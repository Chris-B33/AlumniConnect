package com.alumniconnect.identityservice.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/users")
    @PreAuthorize("hasRole('ALUMNI')")
    public List<String> listUsers() {
        // Stub — replace with real repository lookup when persistence is added
        return List.of("admin-stub: real user list requires persistence layer");
    }
}
