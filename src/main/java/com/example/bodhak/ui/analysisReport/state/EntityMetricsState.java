package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EntityMetricsState implements AnalysisReportSection {

    public record EntityEntry(String name, String kind, int loc, int complexity, int fanOut, int fanIn, String risk) {}

    private final ObservableList<EntityEntry> entities = FXCollections.observableArrayList();
    private final ObservableList<EntityEntry> allEntities = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        entities.clear();
        allEntities.clear();
        if (context == null) return;
        
        // Populate full list sorted by LOC
        context.getEntities().stream()
                .sorted((a, b) -> Integer.compare(b.getMetrics().linesOfCode(), a.getMetrics().linesOfCode()))
                .forEach(e -> {
                    EntityEntry entry = new EntityEntry(
                            e.getSimpleName(),
                            formatKind(e),
                            e.getMetrics().linesOfCode(),
                            e.getMetrics().cyclomaticComplexity(),
                            e.getDependsOn().size(),
                            e.getUsedBy().size(),
                            getRiskLevel(e) // Dynamic calculation of risk instead of hardcoded placeholder
                    );
                    allEntities.add(entry);
                });

        // Populate top 5 list
        entities.addAll(allEntities.stream().limit(5).toList());
    }

    private String getRiskLevel(EntityInfo e) {
        int loc = e.getMetrics().linesOfCode();
        int complexity = e.getMetrics().cyclomaticComplexity();
        int coupling = e.getDependsOn().size() + e.getUsedBy().size();
        
        if (loc > 500 || complexity > 25 || coupling > 15) {
            return "High";
        } else if (loc > 200 || complexity > 10 || coupling > 7) {
            return "Medium";
        }
        return "Low";
    }

    private String formatKind(EntityInfo e) {
        if (e.getKind() == null) return "Unknown";
        String name = e.getKind().name();
        if (name.length() == 0) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public ObservableList<EntityEntry> getEntities() {
        return entities;
    }

    public ObservableList<EntityEntry> getAllEntities() {
        return allEntities;
    }
}
