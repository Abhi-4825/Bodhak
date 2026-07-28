package com.example.anuviya.ui.nav.workspace.impl;

import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.ui.nav.workspace.Workspace;
import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Workspace for the ANALYSIS_REPORT tab.
 * Contains the Project Analysis Dashboard.
 */
public class AnalysisReportView implements Workspace {

    private final StackPane root;
    private final AnalysisReportState state;
    private final com.example.anuviya.ui.analysisReport.AnalysisReportView dashboardView;

    public AnalysisReportView() {
        this.root = new StackPane();
        this.root.setStyle("-fx-background-color: #0e1415;");
        
        this.state = new AnalysisReportState();
        this.dashboardView = new com.example.anuviya.ui.analysisReport.AnalysisReportView(this.state);
        
        this.root.getChildren().add(dashboardView);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        if (engine != null && engine.getAnalysisContext() != null) {
            state.update(engine.getAnalysisContext());
        }
    }
}
