package com.example.bodhakfrontend.core.projectType.detection;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.DependencyGraph;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Rich context object passed to every FrameworkDetector.
 * Wraps AnalysisContext and provides convenience query methods
 * for common detection patterns.
 *
 * Thread-safe: all returned collections are unmodifiable snapshots.
 */
public final class DetectionContext {

    private final AnalysisContext analysisContext;
    private final Map<String, List<EntityInfo>> entitiesByLanguage;

    public DetectionContext(AnalysisContext analysisContext) {
        this.analysisContext = analysisContext;
        this.entitiesByLanguage = analysisContext.getEntities().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getLanguage().toLowerCase(),
                        Collectors.toUnmodifiableList()
                ));
    }

    // ── Delegated access ───────────────────────────────────────
    public AnalysisContext analysisContext()  { return analysisContext; }
    public ProjectInfo projectInfo()         { return analysisContext.getProjectInfo(); }
    public DependencyGraph dependencyGraph() { return analysisContext.getDependencyGraph(); }
    public List<EntityInfo> allEntities()    { return analysisContext.getEntities(); }

    // ── Language-filtered queries ──────────────────────────────
    public List<EntityInfo> entitiesForLanguage(String languageId) {
        return entitiesByLanguage.getOrDefault(
                languageId.toLowerCase(), List.of());
    }

    // ── Decorator / annotation queries ────────────────────────
    public List<EntityInfo> entitiesWithDecorator(String languageId, String decorator) {
        return entitiesForLanguage(languageId).stream()
                .filter(e -> e.getDecorators().contains(decorator))
                .toList();
    }

    public boolean anyEntityHasDecorator(String languageId, String decorator) {
        return entitiesForLanguage(languageId).stream()
                .anyMatch(e -> e.getDecorators().contains(decorator));
    }

    // ── EntityContribution tag queries ────────────────────────
    public List<EntityInfo> entitiesWithTag(String languageId, String tag) {
        return entitiesForLanguage(languageId).stream()
                .filter(e -> e.getContribution().hasTag(tag))
                .toList();
    }

    public boolean anyEntityHasTag(String languageId, String tag) {
        return entitiesForLanguage(languageId).stream()
                .anyMatch(e -> e.getContribution().hasTag(tag));
    }

    // ── File-system queries (using ProjectInfo.knownFiles) ────
    public boolean hasFile(String fileName) {
        return projectInfo().getKnownFiles().stream()
                .anyMatch(p -> p.getFileName().toString()
                        .equalsIgnoreCase(fileName));
    }

    public boolean hasFileMatching(String glob) {
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + glob);
        return projectInfo().getKnownFiles().stream()
                .anyMatch(p -> matcher.matches(p.getFileName()));
    }

    public Set<Path> filesMatching(String glob) {
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + glob);
        return projectInfo().getKnownFiles().stream()
                .filter(p -> matcher.matches(p.getFileName()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasDirectory(String dirName) {
        return projectInfo().getKnownFolders().stream()
                .anyMatch(p -> p.getFileName() != null
                        && p.getFileName().toString()
                        .equalsIgnoreCase(dirName));
    }

    // ── Build file content queries ────────────────────────────
    public Optional<String> readFileContent(String fileName) {
        return projectInfo().getKnownFiles().stream()
                .filter(p -> p.getFileName().toString()
                        .equalsIgnoreCase(fileName))
                .findFirst()
                .flatMap(p -> {
                    try {
                        return Optional.of(Files.readString(p));
                    } catch (IOException e) {
                        return Optional.empty();
                    }
                });
    }

    public boolean fileContains(String fileName, String content) {
        return readFileContent(fileName)
                .map(c -> c.contains(content))
                .orElse(false);
    }

    // ── Member / method queries ───────────────────────────────
    public boolean anyMemberNameContains(String languageId, String pattern) {
        return entitiesForLanguage(languageId).stream()
                .flatMap(e -> e.getMembers().stream())
                .anyMatch(m -> m.getName().contains(pattern));
    }
}
