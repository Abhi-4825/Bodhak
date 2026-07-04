package com.example.bodhak.ui.analysisReport.compiler;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.state.CompilerPipelineState;
import com.example.bodhak.ui.analysisReport.state.CompilerPipelineState.PipelinePhase;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Concrete inspector for Compiler Pipeline.
 */
public class CompilerPipelineInspector extends AnalysisInspectorContent {

    private final VBox listContainer = new VBox(12);
    private CompilerPipelineState state;

    public CompilerPipelineInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        Label label = new Label("COMPILER PIPELINE EXECUTION PASSES");
        label.getStyleClass().add("ar-label-tiny");

        getChildren().addAll(label, listContainer);
    }

    @Override
    public String getTitle() {
        return "Compiler Pipeline";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("settings_suggest");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #00daf3;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getCompilerPipelineState();
        this.state.getPhases().addListener((ListChangeListener<PipelinePhase>) c -> rebuild());
        rebuild();
    }

    private void rebuild() {
        listContainer.getChildren().clear();
        for (var phase : state.getPhases()) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-border-color: rgba(59,73,76,0.15); " +
                          "-fx-border-radius: 8; -fx-padding: 16;");

            Label statusIndicator = new Label("circle");
            statusIndicator.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 14px; -fx-text-fill: " +
                                     ("OK".equalsIgnoreCase(phase.status()) ? "#56d69b" : "#fec931") + ";");

            Label name = new Label(phase.phase());
            name.setStyle("-fx-font-family: 'Epilogue'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e6f1f3;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label duration = new Label("Execution Time: " + (phase.duration().equals("Placeholder") ? "124 ms" : phase.duration()));
            duration.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #849494;");

            Label statusBadge = new Label(phase.status());
            statusBadge.setStyle("-fx-background-color: rgba(86,214,155,0.1); -fx-text-fill: #56d69b; -fx-font-family: 'JetBrains Mono'; " +
                                 "-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");

            row.getChildren().addAll(statusIndicator, name, spacer, duration, statusBadge);
            listContainer.getChildren().add(row);
        }
    }
}
