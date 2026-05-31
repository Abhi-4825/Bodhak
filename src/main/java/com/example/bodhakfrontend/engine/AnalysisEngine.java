package com.example.bodhakfrontend.engine;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.namespace.NamespaceInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.warning.WarningRule;
import com.example.bodhakfrontend.core.plugin.LanguagePlugin;
import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;
import com.example.bodhakfrontend.engine.analyzer.GlobalEntryPointDetector;
import com.example.bodhakfrontend.engine.incremental.EntityViewModelBuilder;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class AnalysisEngine {

    private final LanguagePluginRegistry registry;
    private final ProjectScanner scanner;
    private final DependencyGraph dependencyGraph;
    private final NamespaceBuilder namespaceBuilder;
    private final ProjectInfoBuilder projectInfoBuilder;
    private final EntityViewModelBuilder viewModelBuilder;

    private final Map<Path, List<EntityInfo>> entityPathMap = new ConcurrentHashMap<>();
    private final Map<Path, Set<String>> pathNamesMap = new ConcurrentHashMap<>();
    private final List<EntityInfo> allEntities = new ArrayList<>();

    public AnalysisEngine(LanguagePluginRegistry registry, EntityViewModelBuilder viewModelBuilder) {
        this.registry = registry;
        this.viewModelBuilder = viewModelBuilder;
        this.scanner = new ProjectScanner(registry);
        this.dependencyGraph = new DependencyGraph(registry);
        this.namespaceBuilder = new NamespaceBuilder();
        this.projectInfoBuilder = new ProjectInfoBuilder(scanner, new GlobalEntryPointDetector(registry));
    }

    public void analyze(Path projectPath) {
        // 1. Scan files
        Set<Path> files = scanner.scan(projectPath);

        // 2. Extract names mapped to file paths
        for (Path file : files) {
            LanguagePlugin plugin = registry.forFile(file).orElse(null);
            if (plugin != null) {
                Set<String> names = plugin.getNameExtractor().extractNames(file);
                pathNamesMap.put(file, names);
            }
        }

        Set<String> allKnownNames = pathNamesMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet());

        // 3. Build Dependency Graph
        dependencyGraph.buildInitialGraph(pathNamesMap, allKnownNames);

        // 4. Extract rich EntityInfo via builder
        for (Path file : files) {
            LanguagePlugin plugin = registry.forFile(file).orElse(null);
            if (plugin != null) {
                List<EntityInfo> entities = plugin.getEntityBuilder().build(file);
                
                // Process warnings
                for (EntityInfo e : entities) {
                    List<WarningRule> warnings = plugin.getWarningProvider().evaluate(e);
                    e.getWarnings().addAll(warnings);
                }

                allEntities.addAll(entities);
                entityPathMap.put(file, new ArrayList<>(entities));
            }
        }

        // Apply dependencies to entities
        applyGraphDependencies(allEntities);

        // 5. Build ViewModels
        viewModelBuilder.initialBuild(allEntities);

        // 6. Build Namespaces
        Map<String, NamespaceInfo> namespaces = namespaceBuilder.build(allEntities);

        // 7. Aggregate Project Info
        projectInfoBuilder.buildAll(projectPath, allEntities, namespaces);
    }

    private void applyGraphDependencies(List<EntityInfo> entities) {
        Map<String, Set<String>> revDeps = dependencyGraph.getReverseDependencies();
        Set<Set<String>> cycles = dependencyGraph.getCircularGroups();
        
        for (EntityInfo e : entities) {
            String name = e.getEntityName();
            Path file = e.getSourceFile().toPath().toAbsolutePath().normalize();
            
            // Depends ON
            Map<String, Set<String>> fileDeps = dependencyGraph.getFileDependencies().get(file);
            if (fileDeps != null) {
                e.getDependsOn().addAll(fileDeps.getOrDefault(name, Set.of()));
            }

            // Used BY
            e.getUsedBy().addAll(revDeps.getOrDefault(name, Set.of()));
            
            // Cycles
            Set<Set<String>> myCycles = new HashSet<>();
            for (Set<String> cycle : cycles) {
                if (cycle.contains(name)) myCycles.add(cycle);
            }
            e.getCircularGroups().addAll(myCycles);
        }
    }

    public ProjectInfo getProjectInfo() {
        // Evaluate project level metrics using ProjectInfoBuilder
        return projectInfoBuilder.buildAll(
                (scanner.getKnownFolders().isEmpty() ? null : scanner.getKnownFolders().iterator().next()),
                allEntities, 
                namespaceBuilder.build(allEntities)
        );
    }
    
    public Map<Path, List<EntityInfo>> getEntityPathMap() {
        return entityPathMap;
    }

    /**
     * Returns an unmodifiable snapshot of the entities currently associated with
     * the given file path. Returns an empty list if the path is unknown.
     * Safe to call from any thread.
     */
    public List<EntityInfo> getEntitiesForFile(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        List<EntityInfo> list = entityPathMap.get(normalized);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }
    public DependencyGraph getDependencyGraph(){return dependencyGraph;}
    /**
     * Returns the latest immutable {@link GraphSnapshot} from the dependency graph.
     * Safe to call from any thread.
     */
    public GraphSnapshot getGraphSnapshot() {
        return dependencyGraph.snapshot();
    }

    // Incremental handlers...
    public void onFileCreate(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        scanner.onFileCreated(normalized);
        
        LanguagePlugin plugin = registry.forFile(normalized).orElse(null);
        if (plugin == null) return;
        
        // 1. Name Extractor
        Set<String> names = plugin.getNameExtractor().extractNames(normalized);
        pathNamesMap.put(normalized, names);
        Set<String> allKnownNames = pathNamesMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        
        // 2. Dependency Graph
        dependencyGraph.updateForFile(normalized, allKnownNames);
        
        // 3. Entity creation
        List<EntityInfo> newEntities = plugin.getEntityBuilder().build(normalized);
        for (EntityInfo e : newEntities) {
            e.getWarnings().addAll(plugin.getWarningProvider().evaluate(e));
        }
        
        allEntities.addAll(newEntities);
        entityPathMap.put(normalized, new ArrayList<>(newEntities));
        
        applyGraphDependencies(allEntities); // Refresh all entity dep fields
        
        viewModelBuilder.onFileCreate(newEntities);
        projectInfoBuilder.onFileCreated(file);
    }
    
    public void onFileDelete(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        scanner.onFileDeleted(normalized);
        
        LanguagePlugin plugin = registry.forFile(normalized).orElse(null);
        if (plugin != null) plugin.getEntityBuilder().invalidate(normalized);
        
        pathNamesMap.remove(normalized);
        dependencyGraph.removeFile(normalized);
        
        List<EntityInfo> removed = entityPathMap.remove(normalized);
        if (removed != null) {
            allEntities.removeAll(removed);
            viewModelBuilder.onFileDelete(removed);
        }
        
        applyGraphDependencies(allEntities); // Refresh all entity dep fields
        projectInfoBuilder.onFileDeleted(file);
    }
    
    public void onFileModify(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        LanguagePlugin plugin = registry.forFile(normalized).orElse(null);
        if (plugin == null) return;
        
        List<EntityInfo> oldEntities = new ArrayList<>(entityPathMap.getOrDefault(normalized, new ArrayList<>()));
        plugin.getEntityBuilder().invalidate(normalized);

        // Re-extract
        Set<String> names = plugin.getNameExtractor().extractNames(normalized);
        pathNamesMap.put(normalized, names);
        Set<String> allKnownNames = pathNamesMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        
        dependencyGraph.updateForFile(normalized, allKnownNames);
        
        allEntities.removeAll(oldEntities);
        
        List<EntityInfo> newEntities = plugin.getEntityBuilder().build(normalized);
        for (EntityInfo e : newEntities) {
            e.getWarnings().addAll(plugin.getWarningProvider().evaluate(e));
        }
        
        allEntities.addAll(newEntities);
        entityPathMap.put(normalized, new ArrayList<>(newEntities));
        
        applyGraphDependencies(allEntities);
        
        viewModelBuilder.onFileModify(oldEntities, newEntities);
        projectInfoBuilder.onFileDeleted(file);
        projectInfoBuilder.onFileCreated(file);
    }
    
    public void onFolderCreate(Path folder) {
        scanner.onFolderCreated(folder);
    }
    
    public void onFolderDelete(Path folder) {
        scanner.onFolderDeleted(folder);
    }

    public LanguagePluginRegistry getPluginRegistry() {
        return registry;
    }
}
