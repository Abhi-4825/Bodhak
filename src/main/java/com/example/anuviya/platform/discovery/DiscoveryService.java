package com.example.anuviya.platform.discovery;

import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;

import java.util.List;

public class DiscoveryService {
    private static final DiscoveryService INSTANCE = new DiscoveryService();

    private DiscoveryService() {}

    public static DiscoveryService getInstance() {
        return INSTANCE;
    }

    public List<AIProvider> discoverInstalledProviders() {
        // Discovers installed provider implementations registered in ProviderRegistry
        return com.example.anuviya.analyzer.ai.platform.provider.ProviderRegistry.getInstance().all().stream()
            .filter(provider -> {
                // OpenAI is always considered "installed" if configured, or just always ready as CLOUD kind.
                if (provider.info().kind() == com.example.anuviya.analyzer.ai.platform.model.ProviderKind.CLOUD) {
                    return true;
                }
                return provider.runtimeStatus() == com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.READY 
                    || checkLocalProviderInstalled(provider.info().id());
            })
            .toList();
    }

    public List<ModelInfo> discoverInstalledModels(AIProvider provider) {
        if (provider == null) return List.of();
        return provider.installedModels();
    }

    private boolean checkLocalProviderInstalled(String providerId) {
        return com.example.anuviya.platform.registry.LocalPackageDb.isInstalled(providerId);
    }
}
