package com.alumniconnect.identityservice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAvatarRepository extends JpaRepository<UserAvatarEntity, String> {
}
