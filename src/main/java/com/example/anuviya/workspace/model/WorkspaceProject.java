package com.example.anuviya.workspace.model;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceProject(
    UUID id,
    String name,
    String location,
    String language,
    String buildTool,
    String projectType,
    String compilerVersion,
    String analysisVersion,
    Instant created,
    Instant lastOpened,
    Instant lastAnalysis,
    UUID latestSessionId,
    ProjectFingerprint fingerprint,
    boolean pinned,
    boolean favorite,
    ProjectStatus status
) {}
