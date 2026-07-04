package com.example.bodhak.ui.nav.workspace.impl;

import com.example.bodhak.orchestration.AnalysisEngine;
import com.example.bodhak.ui.nav.OverviewPanel;
import com.example.bodhak.ui.nav.workspace.Workspace;

import javafx.scene.Node;

/**
 * Workspace for the OVERVIEW tab.
 *
 * <p>This is the only workspace that embeds the full IDE split layout
 * (file tree + code editor + right analysis panel).  All other workspaces
 * are full-screen analytical dashboards.
 *
 * <p>Refreshing delegates to {@link OverviewPanel#update(AnalysisEngine)},
 * which itself performs a no-op if the engine reference has not changed.
 */
public class OverviewWorkspace implements Workspace {

    /** The full IDE split-pane (file tree + editor + right panel). */
    private final Node splitPane;

    /** Right-panel overview widget — updated on every project load. */
    private final OverviewPanel overviewPanel;

    /**
     * @param splitPane     the existing SplitPane node from App.java
     * @param overviewPanel the live OverviewPanel already embedded in the split-pane
     */
    public OverviewWorkspace(Node splitPane, OverviewPanel overviewPanel) {
        this.splitPane    = splitPane;
        this.overviewPanel = overviewPanel;
    }

    @Override
    public Node getRoot() {
        return splitPane;
    }

    /**
     * Refreshes the overview panel with fresh engine data.
     * Safe to call when {@code engine} is {@code null} (shows empty state).
     */
    @Override
    public void refresh(AnalysisEngine engine) {
        overviewPanel.update(engine);
    }
}
