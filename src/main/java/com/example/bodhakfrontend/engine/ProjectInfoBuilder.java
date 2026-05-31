package com.example.bodhakfrontend.engine;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.namespace.NamespaceInfo;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.model.project.Hotspot;
import com.example.bodhakfrontend.core.model.project.LargestFileInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.project.UnusedEntityInfo;
import com.example.bodhakfrontend.core.plugin.EntryPointDetector;
import com.example.bodhakfrontend.engine.analyzer.HotspotAnalyzer;
import com.example.bodhakfrontend.engine.analyzer.UnusedEntityAnalyzer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectInfoBuilder {

    private final ProjectScanner scanner;
    private final HotspotAnalyzer hotspotAnalyzer = new HotspotAnalyzer();
    private final EntryPointDetector entryPointDetector;
    private final UnusedEntityAnalyzer unusedEntityAnalyzer = new UnusedEntityAnalyzer();

    private final List<LargestFileInfo> largestFiles = new ArrayList<>();
    private List<LargestFileInfo> top5 = new ArrayList<>();
    private final Map<String, Set<Path>> languageCountMap = new HashMap<>();

    public ProjectInfoBuilder(ProjectScanner scanner, EntryPointDetector entryPointDetector) {
        this.scanner = scanner;
        this.entryPointDetector = entryPointDetector;
    }

    public ProjectInfo buildAll(Path projectPath, List<EntityInfo> entities, Map<String, NamespaceInfo> namespaces) {
        largestFiles.clear();
        languageCountMap.clear();

        List<Hotspot> hotspots = hotspotAnalyzer.analyzeAll(entities);
        EntryPointInfo entryPointInfo = entryPointDetector.detect(entities);
        Set<UnusedEntityInfo> unusedEntityInfos = unusedEntityAnalyzer.analyze(entities, entryPointInfo);

        for (Path file : scanner.getKnownFiles()) {
            updateFileInfo(file);
        }

        recomputeTop5();

        return createProjectInfo(entities, namespaces, hotspots, entryPointInfo, unusedEntityInfos);
    }

    private ProjectInfo createProjectInfo(
            List<EntityInfo> entities, 
            Map<String, NamespaceInfo> namespaces,
            List<Hotspot> hotspots, 
            EntryPointInfo entryPointInfo, 
            Set<UnusedEntityInfo> unused
    ) {
        int totalEntities = entities.size();
        int healthy = 0, warnings = 0, god = 0, circular = 0, coupled = 0;

        for (EntityInfo e : entities) {
            if (e.getWarnings().isEmpty()) healthy++;
            else warnings++;
            
            // Just count types among issues loosely
            boolean isGod = e.getWarnings().stream().anyMatch(w -> w.getRuleId().contains("001"));
            boolean isCircular = !e.getCircularGroups().isEmpty();
            boolean isCoupled = (e.getDependsOn().size() + e.getUsedBy().size()) > 10;
            
            if (isGod) god++;
            if (isCircular) circular++;
            if (isCoupled) coupled++;
        }

        return new ProjectInfo(
                languageCountMap,
                top5,
                scanner.getKnownFolders(),
                scanner.getKnownFiles(),
                entities,
                namespaces,
                hotspots,
                entryPointInfo,
                unused,
                totalEntities,
                healthy,
                warnings,
                god,
                circular,
                coupled
        );
    }

    private void updateFileInfo(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        File f = normalized.toFile();

        try {
            if (Files.isRegularFile(normalized) && Files.isReadable(normalized)) {
                String name = normalized.getFileName().toString();
                if (name.startsWith(".") || name.endsWith(".class")) return;

                // Update language distribution simply by extension
                String ext = getExtension(name);
                if (!ext.isEmpty()) {
                    languageCountMap.computeIfAbsent(ext, k -> new HashSet<>()).add(normalized);
                }

                // LOC & Largest files
                long loc = Files.lines(normalized).map(String::trim).filter(s -> !s.isEmpty() && !s.startsWith("//") && !s.startsWith("#")).count();
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

    public void onFileCreated(Path path) {
        updateFileInfo(path);
        recomputeTop5();
    }

    public void onFileDeleted(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        
        largestFiles.removeIf(lf -> lf.getSourceFile().toPath().toAbsolutePath().normalize().equals(normalized));
        recomputeTop5();

        for (Set<Path> files : languageCountMap.values()) {
            files.remove(normalized);
        }
        languageCountMap.values().removeIf(Set::isEmpty);
    }
}
