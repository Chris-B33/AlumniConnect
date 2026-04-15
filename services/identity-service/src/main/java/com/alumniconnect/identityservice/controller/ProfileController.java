package com.alumniconnect.identityservice.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.alumniconnect.identityservice.dto.AvatarResponse;
import com.alumniconnect.identityservice.dto.ProfileResponse;
import com.alumniconnect.identityservice.dto.UpdateProfileRequest;
import com.alumniconnect.identityservice.service.AvatarStore;
import com.alumniconnect.identityservice.service.ProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AvatarStore avatarStore;

    public ProfileController(ProfileService profileService, AvatarStore avatarStore) {
        this.profileService = profileService;
        this.avatarStore = avatarStore;
    }

    @GetMapping
    public ProfileResponse getProfile() {
        return profileService.getProfile(currentUserEmail());
    }

    @PutMapping
    public ProfileResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(currentUserEmail(), request);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AvatarResponse uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        return profileService.updateAvatar(currentUserEmail(), file);
    }

    @GetMapping("/avatar/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String userId) {
        return avatarStore.get(userId)
                .map(entry -> ResponseEntity.ok()
                        .contentType(entry.contentType() != null
                                ? MediaType.parseMediaType(entry.contentType())
                                : MediaType.APPLICATION_OCTET_STREAM)
                        .body(entry.data()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found"));
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
