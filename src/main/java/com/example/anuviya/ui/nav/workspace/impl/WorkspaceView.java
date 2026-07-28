package com.example.anuviya.ui.nav.workspace.impl;

import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.ui.nav.OverviewPanel;
import com.example.anuviya.ui.nav.workspace.Workspace;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Workspace for the WORKSPACE tab.
 * Wraps the full IDE split layout (file tree + code editor + right panel) and includes a left navigation sidebar.
 */
public class WorkspaceView implements Workspace {

    private final BorderPane root;
    private final Node splitPane;
    private final OverviewPanel overviewPanel;

    public WorkspaceView(Node splitPane, OverviewPanel overviewPanel, Runnable onToggleSidebar) {
        this.splitPane = splitPane;
        this.overviewPanel = overviewPanel;

        this.root = new BorderPane();
        this.root.setStyle("-fx-background-color: #0e1415;");

        VBox sidebarNav = new VBox();
        sidebarNav.setPrefWidth(64);
        sidebarNav.setMinWidth(64);
        sidebarNav.setMaxWidth(64);
        sidebarNav.setStyle("-fx-background-color: #111819; -fx-border-color: transparent #1e2526 transparent transparent; -fx-border-width: 0 1 0 0; -fx-alignment: top-center; -fx-padding: 16 0 0 0; -fx-spacing: 12;");

        Button toggleBtn = new Button();
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10; -fx-background-radius: 8;");
        
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent("M3 2 h10 a1 1 0 0 1 1 1 v10 a1 1 0 0 1 -1 1 h-10 a1 1 0 0 1 -1 -1 v-10 a1 1 0 0 1 1 -1 z M6 2 v12");
        path.setStroke(javafx.scene.paint.Color.web("#849494"));
        path.setStrokeWidth(1.5);
        path.setFill(javafx.scene.paint.Color.TRANSPARENT);
        toggleBtn.setGraphic(path);

        toggleBtn.setOnMouseEntered(ev -> path.setStroke(javafx.scene.paint.Color.web("#00daf3")));
        toggleBtn.setOnMouseExited(ev -> path.setStroke(javafx.scene.paint.Color.web("#849494")));
        toggleBtn.setOnAction(e -> onToggleSidebar.run());

        sidebarNav.getChildren().add(toggleBtn);

        this.root.setLeft(sidebarNav);
        this.root.setCenter(splitPane);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        overviewPanel.update(engine);
    }
}
