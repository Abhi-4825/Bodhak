package com.example.anuviya.ui.analysisReport.state;

import com.example.anuviya.context.AnalysisContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CompilerPipelineState implements AnalysisReportSection {

    public record PipelinePhase(String phase, String duration, String status) {}

    private final ObservableList<PipelinePhase> phases = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        // Placeholder data since pipeline timing isn't exposed yet
        phases.clear();
        phases.addAll(
                new PipelinePhase("Parsing", "Placeholder", "OK"),
                new PipelinePhase("Resolution", "Placeholder", "OK"),
                new PipelinePhase("Analysis", "Placeholder", "OK")
        );
    }

    public ObservableList<PipelinePhase> getPhases() {
        return phases;
    }
}
