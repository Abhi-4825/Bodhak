package com.example.anuviya.ui.analysisReport.state;

import com.example.anuviya.context.AnalysisContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NamespaceOverviewState implements AnalysisReportSection {

    public record NamespaceEntry(String name, int entitiesCount, long loc, int fanOut, int fanIn) {}

    private final ObservableList<NamespaceEntry> namespaces = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        namespaces.clear();
        if (context == null) return;
        
        var namespaceMap = context.getNamespaces();
        if (namespaceMap != null) {
            namespaceMap.values().stream()
                    .map(ns -> {
                        long loc = ns.getEntities().stream()
                                .mapToLong(e -> e.getMetrics().linesOfCode())
                                .sum();
                        return new NamespaceEntry(
                                ns.getNamespaceName(),
                                ns.getEntities().size(),
                                loc,
                                ns.getFanOut(),
                                ns.getFanIn()
                        );
                    })
                    .sorted((a, b) -> Long.compare(b.loc(), a.loc())) // Sort by LOC descending
                    .forEach(namespaces::add);
        }
    }

    public ObservableList<NamespaceEntry> getNamespaces() {
        return namespaces;
    }
}
