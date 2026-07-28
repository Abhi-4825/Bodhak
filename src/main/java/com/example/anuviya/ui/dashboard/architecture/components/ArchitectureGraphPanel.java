package com.example.anuviya.ui.dashboard.architecture.components;

import com.example.anuviya.ui.dashboard.architecture.graph.GraphViewMode;
import com.example.anuviya.ui.dashboard.architecture.model.ArchitectureWorkspaceState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ArchitectureGraphPanel extends VBox {
    private final ArchitectureWorkspaceState workspaceState;
    private GraphViewMode currentMode = GraphViewMode.ARCHITECTURE;

    private final StackPane graphContainer = new StackPane();

    public ArchitectureGraphPanel(ArchitectureWorkspaceState workspaceState) {
        this.workspaceState = workspaceState;

        setStyle("-fx-background-color: #0e1415; -fx-background-radius: 8; -fx-border-color: #1a2122; -fx-border-radius: 8; -fx-border-width: 1;");
        setPadding(new Insets(0));

        ArchitectureGraphToolbar toolbar = new ArchitectureGraphToolbar(this::changeMode);
        
        StackPane overlay = new StackPane();
        overlay.getChildren().addAll(graphContainer, toolbar);
        StackPane.setAlignment(toolbar, Pos.TOP_CENTER);
        StackPane.setMargin(toolbar, new Insets(20, 0, 0, 0));

        graphContainer.setMinHeight(600);
        VBox.setVgrow(overlay, Priority.ALWAYS);

        getChildren().addAll(overlay);
        
        // Add immediately built architecture view
        javafx.scene.Node archView = workspaceState.getArchitectureView();
        if (archView != null) {
            graphContainer.getChildren().add(archView);
        }
        
        // Trigger initial view setup
        changeMode(GraphViewMode.ARCHITECTURE);
    }

    private void changeMode(GraphViewMode mode) {
        currentMode = mode;
        javafx.scene.Node selected = switch (mode) {
            case ARCHITECTURE -> workspaceState.getArchitectureView();
            case DEPENDENCY -> workspaceState.getDependencyView();
            case CYCLES -> workspaceState.getCycleView();
            case HOTSPOTS -> workspaceState.getHotspotView();
        };

        if (selected != null && !graphContainer.getChildren().contains(selected)) {
            graphContainer.getChildren().add(selected);
        }

        showView(selected);
    }

    private void showView(javafx.scene.Node selected) {
        for (javafx.scene.Node view : graphContainer.getChildren()) {
            boolean active = (view == selected);
            view.setVisible(active);
            view.setManaged(active);
        }
    }
}
