package com.spaceapps.aqi.service.impl.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimpleCache<K, V> {

    private final Map<K, CacheEntry<V>> store = new ConcurrentHashMap<>();
    private final Duration ttl;

    public SimpleCache(@Value("${app.cacheTtlMinutes:60}") long cacheTtlMinutes) {
        this.ttl = Duration.ofMinutes(cacheTtlMinutes);
    }

    public Optional<V> get(K key) {
        CacheEntry<V> entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    public void put(K key, V value) {
        store.put(key, new CacheEntry<>(value, Instant.now().plus(ttl)));
    }

    public void invalidate(K key) {
        store.remove(key);
    }

    private record CacheEntry<T>(T value, Instant expiresAt) {
    }
}
