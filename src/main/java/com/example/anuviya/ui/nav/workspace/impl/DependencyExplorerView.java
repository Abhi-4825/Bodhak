package com.example.anuviya.ui.nav.workspace.impl;

import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.ui.nav.workspace.Workspace;
import com.example.anuviya.ui.dependencyExplorer.DependencyWorkspace;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Workspace wrapper for the DEPENDENCY_EXPLORER tab.
 */
public class DependencyExplorerView implements Workspace {

    private final StackPane root;
    private final DependencyWorkspace dependencyWorkspace;

    public DependencyExplorerView() {
        this.root = new StackPane();
        this.root.setStyle("-fx-background-color: #0d141a;");
        
        this.dependencyWorkspace = new DependencyWorkspace();
        root.getChildren().add(dependencyWorkspace.getRoot());
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        dependencyWorkspace.refresh(engine);
    }
}
