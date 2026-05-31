package com.example.bodhakfrontend.engine;

import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Scans directories for supported source files based on registered plugins.
 */
public class ProjectScanner {

    private final LanguagePluginRegistry registry;
    private final Set<Path> knownFolders = new HashSet<>();
    private final Set<Path> knownFiles = new HashSet<>();

    public ProjectScanner(LanguagePluginRegistry registry) {
        this.registry = registry;
    }

    public Set<Path> scan(Path root) {
        knownFolders.clear();
        knownFiles.clear();
        Set<Path> sourceFiles = new HashSet<>();

        if (root == null || !Files.exists(root)) return sourceFiles;

        try (Stream<Path> stream = Files.walk(root)) {
            stream.forEach(p -> {
                Path normalized = p.toAbsolutePath().normalize();
                if (Files.isDirectory(normalized)) {
                    // Ignore typical output/hidden directories
                    String name = normalized.getFileName().toString();
                    if (!name.startsWith(".") && !name.equals("target") && !name.equals("build") && !name.equals("node_modules")) {
                        knownFolders.add(normalized);
                    }
                } else if (Files.isRegularFile(normalized)) {
                    String name = normalized.getFileName().toString();
                    if (!name.startsWith(".")) {
                        knownFiles.add(normalized);
                        if (registry.forFile(normalized).isPresent()) {
                            sourceFiles.add(normalized);
                        }
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Error scanning project: " + e.getMessage());
        }
        return sourceFiles;
    }

    public void onFolderCreated(Path folder) {
        knownFolders.add(folder.toAbsolutePath().normalize());
    }

    public void onFolderDeleted(Path folder) {
        knownFolders.remove(folder.toAbsolutePath().normalize());
    }

    public void onFileCreated(Path file) {
        knownFiles.add(file.toAbsolutePath().normalize());
    }

    public void onFileDeleted(Path file) {
        knownFiles.remove(file.toAbsolutePath().normalize());
    }

    public Set<Path> getKnownFolders() {
        return knownFolders;
    }

    public Set<Path> getKnownFiles() {
        return knownFiles;
    }
}
