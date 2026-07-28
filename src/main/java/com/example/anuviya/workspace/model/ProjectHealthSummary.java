package com.example.anuviya.workspace.model;

public record ProjectHealthSummary(
    HealthStatus architecture,
    HealthStatus scalability,
    int circularDependencies,
    int architectureViolations,
    String delta,
    boolean reanalysisRequired
) {}
