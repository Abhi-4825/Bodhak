package com.example.anuviya.workspace.model;

import java.time.Instant;
import java.util.UUID;

public record ProjectIntelligence(
    UUID projectId,
    UUID sessionId,
    HealthStatus architecture,
    HealthStatus scalability,
    int defects,
    int technologies,
    int dependencies,
    int circularDependencies,
    int architectureViolations,
    String deltaDescription,
    Instant analyzedAt
) {}
