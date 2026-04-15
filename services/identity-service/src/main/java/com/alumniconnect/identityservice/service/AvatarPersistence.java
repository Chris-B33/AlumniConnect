package com.alumniconnect.identityservice.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alumniconnect.identityservice.persistence.UserAvatarEntity;
import com.alumniconnect.identityservice.persistence.UserAvatarRepository;

/**
 * Persists profile avatar bytes in PostgreSQL (identity.user_avatars).
 */
@Service
public class AvatarPersistence {

    public record AvatarBlob(byte[] data, String contentType) {}

    private final UserAvatarRepository avatars;

    public AvatarPersistence(UserAvatarRepository avatars) {
        this.avatars = avatars;
    }

    @Transactional
    public void save(String userId, byte[] data, String contentType) {
        UserAvatarEntity row = avatars.findById(userId).orElseGet(UserAvatarEntity::new);
        row.setUserId(userId);
        row.setImageData(data);
        row.setContentType(contentType);
        avatars.save(row);
    }

    @Transactional(readOnly = true)
    public Optional<AvatarBlob> load(String userId) {
        return avatars.findById(userId)
                .map(e -> new AvatarBlob(e.getImageData(), e.getContentType()));
    }
}
