package com.alumniconnect.identityservice.service;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.alumniconnect.identityservice.domain.User;
import com.alumniconnect.identityservice.dto.AvatarResponse;
import com.alumniconnect.identityservice.dto.ProfileResponse;
import com.alumniconnect.identityservice.dto.UpdateProfileRequest;
import com.alumniconnect.identityservice.repository.UserRepository;

@Service
public class ProfileService {

    private final UserRepository users;
    private final AvatarPersistence avatarPersistence;

    public ProfileService(UserRepository users, AvatarPersistence avatarPersistence) {
        this.users = users;
        this.avatarPersistence = avatarPersistence;
    }

    public ProfileResponse getProfile(String email) {
        return ProfileResponse.from(findOrThrow(email));
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User updated = findOrThrow(email).withProfile(request.firstName(), request.lastName(), request.bio());
        users.save(updated);
        return ProfileResponse.from(updated);
    }

    @Transactional
    public AvatarResponse updateAvatar(String email, MultipartFile file) {
        User user = findOrThrow(email);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be an image");
        }

        try {
            avatarPersistence.save(user.getId(), file.getBytes(), contentType);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store avatar");
        }

        String avatarUrl = "/identity/api/profile/avatar/" + user.getId();
        users.save(user.withAvatarUrl(avatarUrl));
        return new AvatarResponse(avatarUrl);
    }

    private User findOrThrow(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
