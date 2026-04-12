package com.alumniconnect.identityservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {

    @GetMapping("/api/identity/status")
    public String status() {
        return "Identity service is UP";
    }
}
