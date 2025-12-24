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

    public  List<Path> detectSourceRoots(Path projectRoot) {

        Set<Path> sourceRoots = new HashSet<>();

        try (Stream<Path> paths = Files.walk(projectRoot)) {

            paths.filter(Files::isDirectory)
                    .filter(MultiModuleSourceRootDetector::isNotIgnored)
                    .forEach(dir -> {

                        // Standard Maven / Gradle layout
                        if (dir.endsWith("src/main/java")
                                || dir.endsWith("src/test/java")) {
                            sourceRoots.add(dir);
                        }

                        // Plain src folder containing .java files
                        else if (dir.getFileName().toString().equals("src")
                                && containsJavaFiles(dir)) {
                            sourceRoots.add(dir);
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(sourceRoots);
    }

    private static boolean isNotIgnored(Path path) {
        return !IGNORED_DIRS.contains(path.getFileName().toString());
    }

    private static boolean containsJavaFiles(Path dir) {
        try (Stream<Path> files = Files.walk(dir, 2)) {
            return files.anyMatch(p -> p.toString().endsWith(".java"));
        } catch (IOException e) {
            return false;
        }
    }



}

