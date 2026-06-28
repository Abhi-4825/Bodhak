package com.example.bodhakfrontend.core.projectType.detection;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.DependencyGraph;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enriched query context passed to framework detectors.
 */
public final class DetectionContext {

    private final AnalysisContext analysisContext;
    private final ProjectInfo projectInfo;
    private final List<EntityInfo> allEntities;
    private final DependencyGraph dependencyGraph;
    private final Map<String, List<EntityInfo>> entitiesByLanguage;

    public DetectionContext(AnalysisContext analysisContext) {
        this.analysisContext = analysisContext;
        this.projectInfo = analysisContext.getProjectInfo();
        this.allEntities = analysisContext.getEntities();
        this.dependencyGraph = analysisContext.getDependencyGraph();
        this.entitiesByLanguage = allEntities.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getLanguage().toLowerCase(),
                        Collectors.toUnmodifiableList()
                ));
    }

    public DetectionContext(List<EntityInfo> entities, ProjectInfo projectInfo) {
        this.analysisContext = null;
        this.projectInfo = projectInfo;
        this.allEntities = entities;
        this.dependencyGraph = null;
        this.entitiesByLanguage = entities.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getLanguage().toLowerCase(),
                        Collectors.toUnmodifiableList()
                ));
    }

    // ── Delegated access ───────────────────────────────────────
    public AnalysisContext analysisContext()  { return analysisContext; }
    public ProjectInfo projectInfo()         { return projectInfo; }
    public DependencyGraph dependencyGraph() { return dependencyGraph; }
    public List<EntityInfo> allEntities()    { return allEntities; }

    // ── Language-filtered queries ──────────────────────────────
    public List<EntityInfo> entitiesForLanguage(String languageId) {
        return entitiesByLanguage.getOrDefault(languageId.toLowerCase(), List.of());
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
        return projectInfo.knownFiles().stream()
                .anyMatch(p -> p.getFileName().toString()
                        .equalsIgnoreCase(fileName));
    }

    public boolean hasFileMatching(String glob) {
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + glob);
        return projectInfo.knownFiles().stream()
                .anyMatch(p -> matcher.matches(p.getFileName()));
    }

    public Set<Path> filesMatching(String glob) {
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + glob);
        return projectInfo.knownFiles().stream()
                .filter(p -> matcher.matches(p.getFileName()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasDirectory(String dirName) {
        return projectInfo.knownFolders().stream()
                .anyMatch(p -> p.getFileName() != null
                        && p.getFileName().toString()
                        .equalsIgnoreCase(dirName));
    }

    // ── Build file content queries ────────────────────────────
    public Optional<String> readFileContent(String fileName) {
        return projectInfo.knownFiles().stream()
                .filter(p -> p.getFileName().toString()
                        .equalsIgnoreCase(fileName))
                .findFirst()
                .map(path -> {
                    try {
                        return java.nio.file.Files.readString(path);
                    } catch (Exception e) {
                        return null;
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
