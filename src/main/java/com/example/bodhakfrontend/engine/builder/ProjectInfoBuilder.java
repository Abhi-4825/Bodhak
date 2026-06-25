package com.example.bodhakfrontend.engine.builder;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.model.project.LargestFileInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.plugin.EntryPointDetector;
import com.example.bodhakfrontend.engine.ProjectScanner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure metadata builder for {@link ProjectInfo}.
 *
 * Responsibilities:
 *   - projectName / projectRoot
 *   - languageCountMap (file extension → paths)
 *   - largestFiles (top-5 by LOC)
 *   - knownFolders / knownFiles (from scanner)
 *   - entryPointInfo (detected from entities)
 *   - totalEntities (entity count)
 *
 * Does NOT perform: hotspot analysis, unused-entity detection,
 * health counting, god-class counting, or any metrics computation.
 */
public class ProjectInfoBuilder {

    private final ProjectScanner scanner;
    private final EntryPointDetector entryPointDetector;

    private final List<LargestFileInfo> largestFiles  = new ArrayList<>();
    private List<LargestFileInfo>       top5          = new ArrayList<>();
    private final Map<String, Set<Path>> languageCountMap = new HashMap<>();

    public ProjectInfoBuilder(ProjectScanner scanner, EntryPointDetector entryPointDetector) {
        this.scanner              = scanner;
        this.entryPointDetector   = entryPointDetector;
    }

    /**
     * Build a complete {@link ProjectInfo} from the scanned files and parsed entities.
     *
     * @param projectPath root directory of the project
     * @param entities    all entities extracted during the analysis pass
     * @return immutable project metadata record
     */
    public ProjectInfo buildAll(Path projectPath, List<EntityInfo> entities) {
        largestFiles.clear();
        languageCountMap.clear();

        for (Path file : scanner.getKnownFiles()) {
            updateFileInfo(file);
        }
        recomputeTop5();

        EntryPointInfo entryPointInfo = entryPointDetector.detect(entities);

        return createProjectInfo(projectPath, entryPointInfo, entities.size());
    }

    private ProjectInfo createProjectInfo(Path projectPath,
                                          EntryPointInfo entryPointInfo,
                                          int totalEntities) {
        String projectName = projectPath.getFileName().toString();

        return new ProjectInfo(
                projectName,
                projectPath,
                Map.copyOf(languageCountMap),
                List.copyOf(top5),
                scanner.getKnownFolders(),
                scanner.getKnownFiles(),
                entryPointInfo,
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
