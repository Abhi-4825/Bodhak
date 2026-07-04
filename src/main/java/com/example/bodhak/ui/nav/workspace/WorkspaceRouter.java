package com.example.bodhak.ui.nav.workspace;

import com.example.bodhak.orchestration.AnalysisEngine;
import com.example.bodhak.ui.nav.NavTab;
import com.example.bodhak.ui.nav.OverviewPanel;
import com.example.bodhak.ui.nav.workspace.impl.*;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central routing authority for Bodhak's top-level navigation.
 */
public class WorkspaceRouter {

    private final StackPane root;
    private final Map<NavTab, Workspace> workspaces = new EnumMap<>(NavTab.class);
    private NavTab activeTab = NavTab.WORKSPACE;

    /**
     * Creates the router and all workspace instances.
     *
     * @param overviewSplitPane the full IDE split-pane (file tree + editor + right panel)
     *                          shown exclusively in the WORKSPACE tab
     * @param overviewPanel     the {@link OverviewPanel} displayed inside the split-pane;
     *                          the router delegates refresh calls to it
     */
    public WorkspaceRouter(Node overviewSplitPane, OverviewPanel overviewPanel) {
        this.root = new StackPane();
        this.root.setStyle("-fx-background-color: #0e1415;");

        // ── Instantiate every workspace once ────────────────────────────────
        workspaces.put(NavTab.WORKSPACE,            new WorkspaceView(overviewSplitPane, overviewPanel));
        workspaces.put(NavTab.ANALYSIS_REPORT,      new AnalysisReportView());
        workspaces.put(NavTab.ARCHITECTURE,         new ArchitectureView());
        workspaces.put(NavTab.DEPENDENCY_EXPLORER,  new DependencyExplorerView());
        workspaces.put(NavTab.DEFECTS,              new DefectView());

        // ── Add every workspace root to the StackPane ────────────────────────
        for (NavTab tab : NavTab.values()) {
            Workspace ws = workspaces.get(tab);
            if (ws == null) continue;
            Node wsRoot = ws.getRoot();
            wsRoot.setVisible(false);
            wsRoot.setManaged(false);
            root.getChildren().add(wsRoot);
        }

        // Show initial tab (WORKSPACE) without animation
        applyVisibility(NavTab.WORKSPACE);
    }

    // =========================================================================
    // ── Public API ────────────────────────────────────────────────────────────
    // =========================================================================

    /**
     * Returns the StackPane that should be set as the application's main content
     * node.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Switches the visible workspace to the given tab.
     */
    public void show(NavTab tab) {
        if (tab == activeTab) return;
        applyVisibility(tab);
        activeTab = tab;
    }

    /**
     * Propagates a new (or updated) {@link AnalysisEngine} to every workspace.
     */
    public void refreshAll(AnalysisEngine engine) {
        for (Workspace ws : workspaces.values()) {
            try {
                ws.refresh(engine);
            } catch (Exception ex) {
                System.err.println("[WorkspaceRouter] refresh failed for " + ws.getClass().getSimpleName()
                        + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Returns the currently active tab.
     */
    public NavTab getActiveTab() {
        return activeTab;
    }

    // =========================================================================
    // ── Private helpers ───────────────────────────────────────────────────────
    // =========================================================================

    /**
     * Makes {@code target} visible and hides all others.
     * Uses {@code setManaged()} in tandem so the hidden workspaces do not
     * participate in layout.
     */
    private void applyVisibility(NavTab target) {
        for (Map.Entry<NavTab, Workspace> entry : workspaces.entrySet()) {
            boolean active = entry.getKey() == target;
            Node wsRoot = entry.getValue().getRoot();
            wsRoot.setVisible(active);
            wsRoot.setManaged(active);
        }
    }
}
