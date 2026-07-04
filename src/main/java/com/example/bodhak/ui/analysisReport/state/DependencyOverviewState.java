package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.GraphSnapshot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Set;

public class DependencyOverviewState implements AnalysisReportSection {

    public record CoupledEntityEntry(String entity, int fanOut, int fanIn) {}
    public record CycleEntry(String mainEntity, int groupSize, int fanOut) {}

    private final ObservableList<CoupledEntityEntry> coupledEntities = FXCollections.observableArrayList();
    private final ObservableList<CycleEntry> cycleGroups = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        coupledEntities.clear();
        cycleGroups.clear();
        if (context == null || context.getDependencyGraph() == null) return;
        
        GraphSnapshot snapshot = context.getDependencyGraph().snapshot();
        if (snapshot == null) return;

        // Process Highly Coupled Entities
        var globalDeps = snapshot.globalDependencies();
        var reverseDeps = snapshot.reverseDependencies();
        
        // We'll collect all known entities from both maps
        Set<String> allEntities = new java.util.HashSet<>();
        allEntities.addAll(globalDeps.keySet());
        allEntities.addAll(reverseDeps.keySet());

        allEntities.stream()
                .map(entity -> {
                    int fanOut = globalDeps.containsKey(entity) ? globalDeps.get(entity).size() : 0;
                    int fanIn = reverseDeps.containsKey(entity) ? reverseDeps.get(entity).size() : 0;
                    return new CoupledEntityEntry(entity, fanOut, fanIn);
                })
                .sorted((a, b) -> Integer.compare(b.fanOut() + b.fanIn(), a.fanOut() + a.fanIn())) // Sort by total coupling
                .limit(5)
                .forEach(coupledEntities::add);

        // Process Dependency Cycles
        Set<Set<String>> cycles = snapshot.circularGroups();
        if (cycles != null) {
            cycles.stream()
                    .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                    .limit(5)
                    .forEach(cycle -> {
                        if (!cycle.isEmpty()) {
                            String mainEntity = cycle.iterator().next(); // Just pick the first as representative
                            int fanOut = globalDeps.containsKey(mainEntity) ? globalDeps.get(mainEntity).size() : 0;
                            cycleGroups.add(new CycleEntry(mainEntity + " (+" + (cycle.size()-1) + " others)", cycle.size(), fanOut));
                        }
                    });
        }
    }

    public ObservableList<CoupledEntityEntry> getCoupledEntities() {
        return coupledEntities;
    }

    public ObservableList<CycleEntry> getCycleGroups() {
        return cycleGroups;
    }
}
