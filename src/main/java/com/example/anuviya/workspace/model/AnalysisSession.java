package com.example.anuviya.workspace.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisSession(
    UUID sessionId,
    UUID projectId,
    Instant timestamp,
    long durationMillis,
    String compilerVersion,
    String analysisVersion,
    String sessionSchemaVersion,
    String projectVersion,
    CacheReference cache,
    List<ReportReference> reports,
    List<com.example.anuviya.model.runtime.RuntimeAnalysis> runtimeAnalyses
) {}
