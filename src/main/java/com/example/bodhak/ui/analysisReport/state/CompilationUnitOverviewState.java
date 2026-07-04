package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CompilationUnitOverviewState implements AnalysisReportSection {

    public record CompilationUnitEntry(String file, int loc, int entities, int complexity) {}

    private final ObservableList<CompilationUnitEntry> compilationUnits = FXCollections.observableArrayList();
    private final ObservableList<CompilationUnitEntry> allCompilationUnits = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        compilationUnits.clear();
        allCompilationUnits.clear();
        if (context == null) return;
        
        // Populate full list sorted by LOC
        context.getCompilationUnits().stream()
                .filter(cu -> cu.getAggregatedMetrics() != null)
                .sorted((a, b) -> Integer.compare(b.getAggregatedMetrics().linesOfCode(), a.getAggregatedMetrics().linesOfCode()))
                .forEach(cu -> {
                    CompilationUnitEntry entry = new CompilationUnitEntry(
                            cu.getFilePath().getFileName().toString(),
                            cu.getAggregatedMetrics().linesOfCode(),
                            cu.getEntities().size(),
                            cu.getAggregatedMetrics().cyclomaticComplexity()
                    );
                    allCompilationUnits.add(entry);
                });

        // Populate top 5 list
        compilationUnits.addAll(allCompilationUnits.stream().limit(5).toList());
    }

    public ObservableList<CompilationUnitEntry> getCompilationUnits() {
        return compilationUnits;
    }

    public ObservableList<CompilationUnitEntry> getAllCompilationUnits() {
        return allCompilationUnits;
    }
}
