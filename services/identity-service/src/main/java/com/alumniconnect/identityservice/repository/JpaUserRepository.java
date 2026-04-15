package com.alumniconnect.identityservice.repository;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.alumniconnect.identityservice.domain.User;
import com.alumniconnect.identityservice.persistence.UserEntity;
import com.alumniconnect.identityservice.persistence.UserEntityRepository;

@Repository
public class JpaUserRepository implements UserRepository {

    private final UserEntityRepository repo;

    public JpaUserRepository(UserEntityRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return repo.findByEmail(normalize(email)).map(JpaUserRepository::toDomain);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return email != null && repo.existsByEmail(normalize(email));
    }

    @Override
    public void save(User user) {
        repo.save(toEntity(user));
    }

    @Override
    public Collection<User> findAll() {
        return repo.findAll().stream().map(JpaUserRepository::toDomain).collect(Collectors.toList());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static User toDomain(UserEntity e) {
        return new User(
                e.getId(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getRole(),
                e.getCreatedAt(),
                e.getFirstName(),
                e.getLastName(),
                e.getBio(),
                e.getAvatarUrl());
    }

    private static UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setId(u.getId());
        e.setEmail(normalize(u.getEmail()));
        e.setPasswordHash(u.getPasswordHash());
        e.setRole(u.getRole());
        e.setCreatedAt(u.getCreatedAt());
        e.setFirstName(u.getFirstName());
        e.setLastName(u.getLastName());
        e.setBio(u.getBio());
        e.setAvatarUrl(u.getAvatarUrl());
        return e;
    }
}
