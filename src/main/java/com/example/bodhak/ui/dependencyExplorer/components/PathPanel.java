package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.bodhak.ui.dependencyExplorer.state.PathState;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PathPanel extends VBox {

    private final DependencyExplorerState state;
    private final PathState pathState;
    private final HBox pathRow = new HBox(8);

    public PathPanel(DependencyExplorerState state) {
        this.state = state;
        this.pathState = state.getPathState();
        initialise();
    }

    private void initialise() {
        getStyleClass().add("dd-card");
        setSpacing(12);

        Label title = new Label("DEPENDENCY PATH (FROM ROOT)");
        title.getStyleClass().add("dd-card-title");

        pathRow.setAlignment(Pos.CENTER_LEFT);
        
        pathState.getPathNodes().addListener((ListChangeListener<String>) c -> rebuildPath());

        ScrollPane scroll = new ScrollPane(pathRow);
        scroll.getStyleClass().add("dd-scroll-pane");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setFitToHeight(true);

        getChildren().addAll(title, scroll);
        rebuildPath();
    }

    private void rebuildPath() {
        pathRow.getChildren().clear();
        for (int i = 0; i < pathState.getPathNodes().size(); i++) {
            String node = pathState.getPathNodes().get(i);
            
            int lastDot = node.lastIndexOf('.');
            String simpleName = lastDot == -1 ? node : node.substring(lastDot + 1);

            Label lbl = new Label(simpleName);
            lbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-text-fill: #00daf3; -fx-background-color: rgba(0,218,243,0.06); -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
            lbl.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            
            // Interactivity: hover highlights graph node
            lbl.setOnMouseEntered(e -> {
                lbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-text-fill: #00daf3; -fx-background-color: rgba(0,218,243,0.15); -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                state.getDependencyGraphState().hoveredNodeNameProperty().set(node);
            });
            lbl.setOnMouseExited(e -> {
                lbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-text-fill: #00daf3; -fx-background-color: rgba(0,218,243,0.06); -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                state.getDependencyGraphState().hoveredNodeNameProperty().set("");
            });

            // Interactivity: click navigates
            lbl.setOnMouseClicked(e -> {
                if (state.analysisContextProperty().get() != null) {
                    state.analysisContextProperty().get().findEntity(node)
                         .ifPresent(entity -> state.selectedEntityProperty().set(entity));
                }
            });

            pathRow.getChildren().add(lbl);

            if (i < pathState.getPathNodes().size() - 1) {
                Label arrow = new Label("→");
                arrow.setStyle("-fx-text-fill: #849396; -fx-font-size: 12px;");
                arrow.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
                pathRow.getChildren().add(arrow);
            }
        }
        if (pathState.getPathNodes().isEmpty()) {
            Label empty = new Label("No active path");
            empty.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
            pathRow.getChildren().add(empty);
        }
    }
}
