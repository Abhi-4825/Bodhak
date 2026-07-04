package com.example.bodhak.orchestration.incremental.engine;

import java.nio.file.Path;

public record ChangeEvent(
    Path filePath,
    ChangeType changeType,
    String oldHash,
    String newHash
) {}
