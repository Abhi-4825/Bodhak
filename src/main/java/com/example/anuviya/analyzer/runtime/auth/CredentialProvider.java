package com.example.anuviya.analyzer.runtime.auth;

import java.util.Optional;

/**
 * Extensible interface to retrieve credentials dynamically from keychain, user prompts, or memory.
 */
public interface CredentialProvider {
    Optional<String> getCredential(String key);
    void storeCredential(String key, String secret);
}
