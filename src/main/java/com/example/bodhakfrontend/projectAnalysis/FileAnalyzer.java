package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.LanguageDetector;
import com.example.bodhakfrontend.Models.EntryPointInfo;
import com.example.bodhakfrontend.Models.PackageAnalysisInfo;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileAnalyzer {

    private final LanguageDetector languageDetector = new LanguageDetector();

    /* =========================================================
       PHASE A — PROJECT STRUCTURE OVERVIEW
       ========================================================= */

    public PackageAnalysisInfo buildPackageAnalysisInfo(Path rootFolder) {

        AtomicInteger folderCount = new AtomicInteger();
        AtomicInteger fileCount   = new AtomicInteger();
        Map<String, Integer> languageCount = new HashMap<>();

        try {
            Files.walk(rootFolder)
                    .filter(this::isRelevantPath)
                    .forEach(path -> {
                        File file = path.toFile();

                        if (file.isDirectory()) {
                            folderCount.incrementAndGet();
                        } else if (file.isFile()) {
                            fileCount.incrementAndGet();

                            String language =
                                    languageDetector.detectFileType(file);

                            languageCount.put(
                                    language,
                                    languageCount.getOrDefault(language, 0) + 1
                            );
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze project structure", e);
        }

        return new PackageAnalysisInfo(
                folderCount.get(),
                fileCount.get(),
                languageCount,
                analyzeEntryPoints(rootFolder),
                analyzeLargestFiles(rootFolder)
        );
    }

    /* =========================================================
       ENTRY POINT ANALYSIS
       ========================================================= */

    private EntryPointInfo analyzeEntryPoints(Path projectRoot) {

        class EntryState {
            EntryPointInfo.ProjectType projectType =
                    EntryPointInfo.ProjectType.PLAIN_JAVA;
            String primary = null;
            List<String> secondary = new ArrayList<>();
        }

        EntryState state = new EntryState();

        try {
            Files.walk(projectRoot)
                    .filter(this::isJavaFile)
                    .forEach(path -> {
                        try {
                            CompilationUnit cu =
                                    StaticJavaParser.parse(path);

                            String fileName =
                                    path.getFileName().toString();

                            // ---- SPRING BOOT ----
                            boolean isSpringBoot =
                                    cu.findAll(ClassOrInterfaceDeclaration.class)
                                            .stream()
                                            .anyMatch(c ->
                                                    c.getAnnotations()
                                                            .stream()
                                                            .anyMatch(a ->
                                                                    a.getNameAsString()
                                                                            .equals("SpringBootApplication")
                                                            )
                                            );

                            if (isSpringBoot) {
                                if (state.projectType
                                        != EntryPointInfo.ProjectType.SPRING_BOOT) {
                                    state.projectType =
                                            EntryPointInfo.ProjectType.SPRING_BOOT;
                                    state.primary = fileName;
                                } else {
                                    state.secondary.add(fileName);
                                }
                                return;
                            }

                            // ---- JAVAFX ----
                            boolean isJavaFx =
                                    cu.findAll(ClassOrInterfaceDeclaration.class)
                                            .stream()
                                            .anyMatch(c ->
                                                    c.getExtendedTypes()
                                                            .stream()
                                                            .anyMatch(t ->
                                                                    t.getNameAsString()
                                                                            .equals("Application")
                                                            )
                                            );

                            if (isJavaFx
                                    && state.projectType
                                    == EntryPointInfo.ProjectType.PLAIN_JAVA) {

                                state.projectType =
                                        EntryPointInfo.ProjectType.JAVAFX;

                                if (state.primary == null) {
                                    state.primary = fileName;
                                } else {
                                    state.secondary.add(fileName);
                                }
                                return;
                            }

                            // ---- MAIN METHOD ----
                            boolean hasMain =
                                    cu.findAll(MethodDeclaration.class)
                                            .stream()
                                            .anyMatch(m ->
                                                    m.isPublic()
                                                            && m.isStatic()
                                                            && m.getNameAsString()
                                                            .equals("main")
                                            );

                            if (hasMain
                                    && state.projectType
                                    == EntryPointInfo.ProjectType.PLAIN_JAVA) {

                                if (state.primary == null) {
                                    state.primary = fileName;
                                } else {
                                    state.secondary.add(fileName);
                                }
                            }

                        } catch (Exception ignored) {}
                    });

        } catch (Exception ignored) {}

        return new EntryPointInfo(
                state.projectType,
                state.primary,
                state.secondary
        );
    }

    /* =========================================================
       LARGEST FILES (LOC)
       ========================================================= */

    private Map<String,Integer> analyzeLargestFiles(Path root) {

        Map<String, Integer> locMap = new HashMap<>();

        try {
            Files.walk(root)
                    .filter(this::isJavaFile)
                    .forEach(p -> {
                        try {
                            int loc = (int) Files.lines(p)
                                    .map(String::trim)
                                    .filter(this::isCodeLine)
                                    .count();

                            locMap.put(
                                    p.getFileName().toString(),
                                    loc
                            );
                        } catch (Exception ignored) {}
                    });

        } catch (Exception ignored) {}

        return locMap.entrySet()
                .stream()
                .sorted((a, b) ->
                        Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(
                        LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll
                );
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private boolean isRelevantPath(Path path) {
        String p = path.toString();
        return !p.contains("target")
                && !p.contains(".git")
                && !p.contains(".idea");
    }

    private boolean isJavaFile(Path path) {
        return isRelevantPath(path)
                && path.toString().endsWith(".java");
    }

    private boolean isCodeLine(String line) {
        return !line.isEmpty()
                && !line.startsWith("//")
                && !line.startsWith("/*")
                && !line.startsWith("*")
                && !line.startsWith("*/");
    }
}
