package com.example.bodhak.model.project;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public record BuildModel(
    String primaryToolName, // e.g., Maven, Gradle, npm, pip
    Set<Path> buildFiles,
    Map<String, String> dependencies, // Name -> Version
    Set<String> plugins,              // Plugin IDs / Names
    Map<String, String> properties
) {
    public static BuildModel empty() {
        return new BuildModel("unknown", Set.of(), Map.of(), Set.of(), Map.of());
    }
}
