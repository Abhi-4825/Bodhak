package com.example.anuviya.analyzer.runtime.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Standard implementation storing credentials transiently in memory.
 */
public class DefaultCredentialProvider implements CredentialProvider {
    private final Map<String, String> memoryStore = new HashMap<>();

    @Override
    public Optional<String> getCredential(String key) {
        return Optional.ofNullable(memoryStore.get(key));
    }

    @Override
    public void storeCredential(String key, String secret) {
        memoryStore.put(key, secret);
    }
}
