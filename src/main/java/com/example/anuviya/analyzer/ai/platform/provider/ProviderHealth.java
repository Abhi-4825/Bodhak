package com.example.anuviya.analyzer.ai.platform.provider;

public record ProviderHealth(
    boolean healthy,
    String message,
    long responseTimeMs
) {}
