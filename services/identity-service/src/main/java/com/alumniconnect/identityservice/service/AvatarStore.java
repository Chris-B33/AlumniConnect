package com.alumniconnect.identityservice.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class AvatarStore {

    public record Entry(byte[] data, String contentType) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public void put(String userId, byte[] data, String contentType) {
        store.put(userId, new Entry(data, contentType));
    }

    public Optional<Entry> get(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}
