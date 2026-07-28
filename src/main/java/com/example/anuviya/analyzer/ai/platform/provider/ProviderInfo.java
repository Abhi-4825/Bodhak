package com.example.anuviya.analyzer.ai.platform.provider;

import com.example.anuviya.analyzer.ai.platform.model.ProviderCapability;
import com.example.anuviya.analyzer.ai.platform.model.ProviderKind;

import java.util.ArrayList;
import java.util.List;

public record ProviderInfo(
    String id,
    String displayName,
    ProviderKind kind,
    List<ProviderCapability> capabilities
) {
    public ProviderInfo {
        if (capabilities == null) {
            capabilities = new ArrayList<>();
        }
    }
}
