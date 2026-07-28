package com.example.anuviya.workspace.model;

public record ProjectFingerprint(
    String projectId,
    String buildTool,
    String language,
    long sourceFileCount,
    String projectHash
) {}
