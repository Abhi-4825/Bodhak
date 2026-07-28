package com.example.anuviya.analyzer.runtime.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves registered execution providers dynamically by provider ID.
 */
public class ExecutionProviderResolver {
    private final Map<String, ExecutionProvider> providers = new HashMap<>();

    public void registerProvider(ExecutionProvider provider) {
        providers.put(provider.getProviderId().toLowerCase(), provider);
    }

    public Optional<ExecutionProvider> resolve(String providerId) {
        if (providerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(providerId.toLowerCase()));
    }
}
