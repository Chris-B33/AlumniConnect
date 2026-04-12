package com.alumniconnect.identityservice.repository;

import java.util.Optional;

import com.alumniconnect.identityservice.domain.User;

public interface UserRepository {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    void save(User user);
}
