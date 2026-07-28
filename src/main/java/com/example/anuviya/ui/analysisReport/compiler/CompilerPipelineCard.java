package com.example.anuviya.ui.analysisReport.compiler;

import com.example.anuviya.ui.analysisReport.state.CompilerPipelineState;
import com.example.anuviya.ui.analysisReport.state.CompilerPipelineState.PipelinePhase;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Compiler Pipeline Card — Row 5 (Full width).
 *
 * Shows the analysis pipeline stages horizontally connected by a line.
 * Automatically adds/removes nodes based on the state list size.
 * Hides completely if empty.
 */
public class CompilerPipelineCard extends VBox {

    private final CompilerPipelineState state;
    private final HBox pipelineContainer = new HBox();

    // Map phase names to icons
    private String getIconForPhase(String phase) {
        String p = phase.toLowerCase();
        if (p.contains("pars")) return "code";
        if (p.contains("resol")) return "hub";
        if (p.contains("analy")) return "analytics";
        if (p.contains("valid")) return "fact_check";
        if (p.contains("emit")) return "output";
        return "radio_button_checked";
    }

    public CompilerPipelineCard(CompilerPipelineState state) {
        this.state = state;
        initialise();
        bindState();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setStyle("-fx-border-top-color: rgba(0,218,243,0.5); -fx-border-top-width: 3;");
        setPadding(new Insets(24));
        setSpacing(24);
        setMaxWidth(Double.MAX_VALUE);

        // ── Title ──────────────────────────────────────────────────
        Label title = new Label("Compiler Pipeline");
        title.getStyleClass().add("ar-section-title");

        // ── Pipeline Container ─────────────────────────────────────
        pipelineContainer.setAlignment(Pos.CENTER);
        pipelineContainer.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(title, pipelineContainer);
    }

    private void bindState() {
        state.getPhases().addListener((ListChangeListener<PipelinePhase>) c -> rebuildPipeline());
        rebuildPipeline();

        // Hide entirely if empty
        visibleProperty().bind(Bindings.isNotEmpty(state.getPhases()));
        managedProperty().bind(visibleProperty());
    }

    private void rebuildPipeline() {
        pipelineContainer.getChildren().clear();
        var phases = state.getPhases();

        if (phases.size() < 2) {
            Label empty = new Label(phases.isEmpty() ? "Pipeline not run" : "Pipeline running...");
            empty.getStyleClass().add("ar-empty-state");
            pipelineContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < phases.size(); i++) {
            PipelinePhase phase = phases.get(i);
            boolean isLast = (i == phases.size() - 1);
            boolean isDone = "OK".equalsIgnoreCase(phase.status()) || "DONE".equalsIgnoreCase(phase.status());

            // Node
            VBox node = buildNode(phase, isDone);
            pipelineContainer.getChildren().add(node);

            // Connector (skip for last)
            if (!isLast) {
                PipelinePhase nextPhase = phases.get(i + 1);
                boolean nextStarted = "OK".equalsIgnoreCase(nextPhase.status()) || "DONE".equalsIgnoreCase(nextPhase.status()) || "RUNNING".equalsIgnoreCase(nextPhase.status());
                
                Region connector = new Region();
                connector.getStyleClass().add(isDone && nextStarted ? "ar-pipeline-connector-done" : "ar-pipeline-connector-pending");
                HBox.setHgrow(connector, Priority.ALWAYS);
                
                // Align connector with the center of the circles (20px radius + padding)
                HBox.setMargin(connector, new Insets(-35, 0, 0, 0)); 
                
                pipelineContainer.getChildren().add(connector);
            }
        }
    }

    private VBox buildNode(PipelinePhase phase, boolean isDone) {
        VBox col = new VBox(8);
        col.setAlignment(Pos.CENTER);
        col.setMinWidth(100);

        // Circle
        StackPane circle = new StackPane();
        circle.getStyleClass().add(isDone ? "ar-pipeline-node-active" : "ar-pipeline-node-pending");

        Label icon = new Label(getIconForPhase(phase.phase()));
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; " +
                      "-fx-font-size: 20px; -fx-text-fill: " + (isDone ? "#00363d" : "#849494") + ";");
        circle.getChildren().add(icon);

        // Labels
        Label name = new Label(phase.phase());
        name.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 13px; " +
                      "-fx-font-weight: 700; -fx-text-fill: " + (isDone ? "#00daf3" : "#bac9cc") + ";");

        Label status = new Label(phase.status());
        status.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; " +
                        "-fx-font-weight: 700; -fx-letter-spacing: 0.1em; -fx-text-fill: #849494;");

        col.getChildren().addAll(circle, name, status);
        return col;
    }
}
