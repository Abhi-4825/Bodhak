package com.example.anuviya.workspace.model;

import com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus;

public record PlatformStatus(
    String preferredProviderName,
    RuntimeStatus providerStatus,
    String activeModelName,
    boolean compilerReady
) {}
