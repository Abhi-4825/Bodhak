package com.example.anuviya.workspace.model;

public record ReportReference(
    String reportType,
    String filePath,
    int violationCount,
    int warningCount,
    HealthStatus health
) {}
