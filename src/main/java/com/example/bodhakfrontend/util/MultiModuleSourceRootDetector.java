package com.example.bodhakfrontend.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
public class MultiModuleSourceRootDetector {

    private static final Set<String> IGNORED_DIRS = Set.of(
            ".git", ".idea", ".vscode",
            "node_modules", "target", "build",
            ".gradle", ".m2", "out"
    );

    public List<Path> detectSourceRoots(Path projectRoot) {

        Set<Path> roots = new HashSet<>();

        try (Stream<Path> stream = Files.walk(projectRoot)) {

            stream.filter(Files::isDirectory)
                    .filter(this::notIgnored)
                    .forEach(dir -> {

                        // Maven / Gradle Java
                        if (endsWith(dir, "src", "main", "java") ||
                                endsWith(dir, "src", "test", "java")) {
                            roots.add(dir);
                        }

                        // Kotlin support
                        else if (endsWith(dir, "src", "main", "kotlin") ||
                                endsWith(dir, "src", "test", "kotlin")) {
                            roots.add(dir);
                        }

                        // Plain src folder
                        else if (dir.getFileName().toString().equals("src")
                                && containsJavaFiles(dir)
                                && !hasDeeperSourceRoot(dir)) {
                            roots.add(dir);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException("Source root detection failed", e);
        }

        return new ArrayList<>(roots);
    }

    private boolean notIgnored(Path p) {
        return !IGNORED_DIRS.contains(p.getFileName().toString());
    }

    private boolean endsWith(Path path, String... parts) {
        return path.endsWith(Paths.get("", parts));
    }

    private boolean containsJavaFiles(Path dir) {
        try (Stream<Path> files = Files.walk(dir, 3)) {
            return files.anyMatch(p -> p.toString().endsWith(".java"));
        } catch (IOException e) {
            return false;
        }
    }

    // Prevent adding src if src/main/java exists
    private boolean hasDeeperSourceRoot(Path srcDir) {
        return Files.exists(srcDir.resolve("main/java")) ||
                Files.exists(srcDir.resolve("test/java"));
    }
}

