package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.quality.flag.EntityFlag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;

public class RiskHotspotsState implements AnalysisReportSection {

    public record RiskEntry(String entity, String riskLevel, String activeFlags, int loc) {}

    private final ObservableList<RiskEntry> hotspots = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        hotspots.clear();
        if (context == null || context.getCharacteristics() == null) return;

        context.getCharacteristics().stream()
                .filter(c -> !c.flags().isEmpty())
                .sorted((a, b) -> Integer.compare(b.flags().size(), a.flags().size())) // Sort by number of flags
                .limit(10)
                .forEach(c -> {
                    String flags = c.flags().stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", "));
                    String riskLevel = c.flags().size() >= 3 ? "HIGH" : (c.flags().size() == 2 ? "MEDIUM" : "LOW");
                    
                    hotspots.add(new RiskEntry(
                            c.entityInfo().getSimpleName(),
                            riskLevel,
                            flags,
                            c.entityInfo().getMetrics().linesOfCode()
                    ));
                });
    }

    public ObservableList<RiskEntry> getHotspots() {
        return hotspots;
    }
}
