package com.alumniconnect.identityservice.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.alumniconnect.identityservice.domain.User;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByEmail.get(normalize(email)));
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return email != null && usersByEmail.containsKey(normalize(email));
    }

    @Override
    public void save(User user) {
        usersByEmail.put(normalize(user.getEmail()), user);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
