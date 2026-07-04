package com.example.bodhak.ui.nav.workspace.impl;

import com.example.bodhak.orchestration.AnalysisEngine;
import com.example.bodhak.ui.nav.OverviewPanel;
import com.example.bodhak.ui.nav.workspace.Workspace;
import javafx.scene.Node;

/**
 * Workspace for the WORKSPACE tab.
 * Wraps the full IDE split layout (file tree + code editor + right panel).
 */
public class WorkspaceView implements Workspace {

    private final Node splitPane;
    private final OverviewPanel overviewPanel;

    public WorkspaceView(Node splitPane, OverviewPanel overviewPanel) {
        this.splitPane = splitPane;
        this.overviewPanel = overviewPanel;
    }

    @Override
    public Node getRoot() {
        return splitPane;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        overviewPanel.update(engine);
    }
}
