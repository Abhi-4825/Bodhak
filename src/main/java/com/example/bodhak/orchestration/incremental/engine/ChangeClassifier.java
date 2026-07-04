package com.example.bodhak.orchestration.incremental.engine;

import java.nio.file.Files;
import java.nio.file.Path;

public class ChangeClassifier {

    private final FileContentHasher hasher;

    public ChangeClassifier(FileContentHasher hasher) {
        this.hasher = hasher;
    }

    public ChangeEvent classify(Path file, WatchEventKind eventKind) {
        Path normalized = file.toAbsolutePath().normalize();
        String filename = normalized.getFileName().toString().toLowerCase();

        // 1. Build Configuration Check
        if (filename.equals("pom.xml") || filename.equals("build.gradle") || filename.equals("settings.gradle")) {
            String oldHash = hasher.getCachedHash(normalized);
            String newHash = hasher.computeHash(normalized);
            return new ChangeEvent(normalized, ChangeType.BUILD_CONFIG_CHANGE, oldHash, newHash);
        }

        // 2. Resource/Non-Source Check
        if (!filename.endsWith(".java") && !filename.endsWith(".py")) {
            String oldHash = hasher.getCachedHash(normalized);
            String newHash = hasher.computeHash(normalized);
            return new ChangeEvent(normalized, ChangeType.RESOURCE_CHANGE, oldHash, newHash);
        }

        // 3. Handle Deletions
        if (eventKind == WatchEventKind.DELETE || !Files.exists(normalized)) {
            String oldHash = hasher.getCachedHash(normalized);
            hasher.remove(normalized);
            return new ChangeEvent(normalized, ChangeType.DELETED_FILE, oldHash, "");
        }

        // 4. Handle Create/Modify
        String oldHash = hasher.getCachedHash(normalized);
        String newHash = hasher.computeHash(normalized);

        if (oldHash.isEmpty()) {
            return new ChangeEvent(normalized, ChangeType.NEW_FILE, "", newHash);
        } else if (oldHash.equals(newHash)) {
            // No content difference (e.g. metadata-only update or touch)
            return new ChangeEvent(normalized, ChangeType.GENERATED_FILE_CHANGE, oldHash, newHash);
        } else {
            return new ChangeEvent(normalized, ChangeType.CONTENT_CHANGE, oldHash, newHash);
        }
    }

    public enum WatchEventKind {
        CREATE,
        MODIFY,
        DELETE
    }
}
