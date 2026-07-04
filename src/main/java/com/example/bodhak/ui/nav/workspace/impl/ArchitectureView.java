package com.example.bodhak.ui.nav.workspace.impl;

import com.example.bodhak.orchestration.AnalysisEngine;
import com.example.bodhak.ui.dashboard.architecture.ArchitectureDashboard;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureGraphState;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureWorkspaceState;
import com.example.bodhak.ui.nav.workspace.Workspace;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Persistent workspace for the ARCHITECTURE tab.
 * Renamed from ArchitectureWorkspace for IDE WorkspaceView architectural alignment.
 */
public class ArchitectureView implements Workspace {

    private final StackPane root;
    private final ArchitectureGraphState graphState;
    private final ArchitectureWorkspaceState workspaceState;
    private AnalysisEngine lastEngine;

    public ArchitectureView() {
        this.root           = new StackPane();
        this.graphState     = new ArchitectureGraphState();
        this.workspaceState = new ArchitectureWorkspaceState();

        root.setStyle("-fx-background-color: #0e1415;");

        // Show empty state until a project is loaded
        root.getChildren().add(buildEmptyState());
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        if (engine == lastEngine) return;
        lastEngine = engine;

        // Clear stale graph/workspace state for the new project
        graphState.clear();
        workspaceState.clear();

        root.getChildren().clear();

        if (engine == null) {
            root.getChildren().add(buildEmptyState());
            return;
        }

        // Build the dashboard — this is the ONLY time the node tree is created
        try {
            Node dashboard = new ArchitectureDashboard(engine, graphState, workspaceState).build();
            root.getChildren().add(dashboard);
        } catch (Exception ex) {
            System.err.println("[ArchitectureView] Dashboard build failed: " + ex.getMessage());
            ex.printStackTrace();
            root.getChildren().add(buildErrorState(ex));
        }
    }

    private Node buildEmptyState() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));

        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 52px; -fx-text-fill: #2f3637;");

        Label msg = new Label("Load a project to view the architecture graph.");
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494;");
        msg.setWrapText(true);

        box.getChildren().addAll(icon, msg);
        return box;
    }

    private Node buildErrorState(Exception ex) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        Label icon = new Label("⚠");
        icon.setStyle("-fx-font-size: 40px; -fx-text-fill: #ff8a80;");

        Label msg = new Label("Architecture dashboard failed to load.");
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff8a80;");

        Label detail = new Label(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        detail.setStyle("-fx-font-size: 11px; -fx-text-fill: #566465; -fx-font-family: 'JetBrains Mono', monospace;");
        detail.setWrapText(true);

        box.getChildren().addAll(icon, msg, detail);
        return box;
    }
}
