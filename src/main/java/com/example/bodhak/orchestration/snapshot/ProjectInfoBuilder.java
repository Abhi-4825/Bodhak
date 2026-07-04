package com.example.bodhak.orchestration.snapshot;

import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.project.ProjectRootInfo;
import com.example.bodhak.model.project.LargestFileInfo;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.orchestration.ProjectScanner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure metadata builder for {@link ProjectInfo}.
 */
public class ProjectInfoBuilder {

    private final ProjectScanner scanner;

    private final List<LargestFileInfo> largestFiles  = new ArrayList<>();
    private List<LargestFileInfo>       top5          = new ArrayList<>();
    private final Map<String, Set<Path>> languageCountMap = new HashMap<>();

    public ProjectInfoBuilder(ProjectScanner scanner) {
        this.scanner              = scanner;
    }

    @Deprecated
    public ProjectInfoBuilder(ProjectScanner scanner, Object entryPointDetector) {
        this(scanner);
    }

    public ProjectInfo buildAll(Path projectPath, List<EntityInfo> entities) {
        return buildAll(projectPath, entities, null);
    }

    public ProjectInfo buildAll(Path projectPath, List<EntityInfo> entities, ProjectRootInfo projectRootInfo) {
        largestFiles.clear();
        languageCountMap.clear();

        for (Path file : scanner.getKnownFiles()) {
            updateFileInfo(file);
        }
        recomputeTop5();

        ProjectRootInfo pri = projectRootInfo != null ? projectRootInfo : new ProjectRootInfo(Collections.emptySet(), Collections.emptyList());

        return createProjectInfo(projectPath, pri, entities.size());
    }

    private ProjectInfo createProjectInfo(Path projectPath,
                                          ProjectRootInfo projectRootInfo,
                                          int totalEntities) {
        String projectName = projectPath.getFileName().toString();

        return new ProjectInfo(
                projectName,
                projectPath,
                Map.copyOf(languageCountMap),
                List.copyOf(top5),
                scanner.getKnownFolders(),
                scanner.getKnownFiles(),
                projectRootInfo,
                totalEntities
        );
    }

    // ── File scanning helpers ─────────────────────────────────────────────────

    private void updateFileInfo(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        File f = normalized.toFile();

        try {
            if (Files.isRegularFile(normalized) && Files.isReadable(normalized)) {
                String name = normalized.getFileName().toString();
                if (name.startsWith(".") || name.endsWith(".class")) return;

                // Language distribution by extension
                String ext = getExtension(name);
                if (!ext.isEmpty()) {
                    languageCountMap.computeIfAbsent(ext, k -> new HashSet<>()).add(normalized);
                }

                // LOC & largest files
                long loc = Files.lines(normalized)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.startsWith("//") && !s.startsWith("#"))
                        .count();
                largestFiles.add(new LargestFileInfo(name, f, (int) loc));
            }
        } catch (Exception ignored) {}
    }

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i > 0 ? fileName.substring(i) : "";
    }

    private void recomputeTop5() {
        top5 = largestFiles.stream()
                .sorted(Comparator.comparingInt(LargestFileInfo::getLoc).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // ── Incremental update hooks ───────────────────────────────────────────────

    public void onFileCreated(Path path) {
        updateFileInfo(path);
        recomputeTop5();
    }

    public void onFileDeleted(Path path) {
        Path normalized = path.toAbsolutePath().normalize();

        largestFiles.removeIf(lf ->
                lf.getSourceFile().toPath().toAbsolutePath().normalize().equals(normalized));
        recomputeTop5();

        for (Set<Path> files : languageCountMap.values()) {
            files.remove(normalized);
        }
        languageCountMap.values().removeIf(Set::isEmpty);
    }
}
