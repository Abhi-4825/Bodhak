package com.example.anuviya.analyzer.ai.platform.provider;

import com.example.anuviya.analyzer.ai.platform.provider.impl.OllamaProvider;
import com.example.anuviya.platform.PackageCategory;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.util.*;

public class ProviderRegistry {
    private static final ProviderRegistry INSTANCE = new ProviderRegistry();
    private final Map<String, AIProvider> providers = new LinkedHashMap<>();
    private AIProvider activeProvider;

    private ProviderRegistry() {}

    public static ProviderRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void discover() {
        providers.clear();
        
        // Scan PackageRegistry for AI_PROVIDER packages
        List<ServicePackage> servicePackages = PackageRegistry.getInstance().byCategory(PackageCategory.AI_PROVIDER);
        for (ServicePackage pkg : servicePackages) {
            AIProvider provider = createProviderInstance(pkg.id());
            if (provider != null) {
                providers.put(pkg.id(), provider);
            }
        }

        // Set active provider (default to Ollama, or first available)
        if (providers.containsKey("ollama")) {
            activeProvider = providers.get("ollama");
        } else if (!providers.isEmpty()) {
            activeProvider = providers.values().iterator().next();
        }
    }

    private AIProvider createProviderInstance(String id) {
        if ("ollama".equalsIgnoreCase(id)) {
            return new OllamaProvider();
        }
        // Future extensions: lmstudio, openai
        return null;
    }

    public synchronized AIProvider getActive() {
        return activeProvider;
    }

    public synchronized void setActive(String providerId) {
        if (providers.containsKey(providerId)) {
            activeProvider = providers.get(providerId);
        }
    }

    public synchronized Collection<AIProvider> all() {
        return new ArrayList<>(providers.values());
    }

    public synchronized Optional<AIProvider> get(String id) {
        return Optional.ofNullable(providers.get(id));
    }
}
