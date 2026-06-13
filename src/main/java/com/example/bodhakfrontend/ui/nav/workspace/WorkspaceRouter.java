package com.example.bodhakfrontend.ui.nav.workspace;

import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.ui.nav.NavTab;
import com.example.bodhakfrontend.ui.nav.OverviewPanel;
import com.example.bodhakfrontend.ui.nav.workspace.impl.ArchitectureWorkspace;
import com.example.bodhakfrontend.ui.nav.workspace.impl.CodeHealthWorkspace;
import com.example.bodhakfrontend.ui.nav.workspace.impl.DefectWorkspace;
import com.example.bodhakfrontend.ui.nav.workspace.impl.OverviewWorkspace;
import com.example.bodhakfrontend.ui.nav.workspace.impl.PerformanceWorkspace;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central routing authority for Bodhak's top-level navigation.
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Owns a single {@link StackPane} that fills the main content area.
 *       This pane is passed to {@code root.setCenter()} exactly once and
 *       never replaced.</li>
 *   <li>All {@link Workspace} instances are created <em>once</em> in the
 *       constructor and remain alive for the application lifetime.</li>
 *   <li>Switching tabs is {@code O(1)}: set one workspace visible, hide the
 *       rest.  No node is rebuilt, no state is lost.</li>
 *   <li>On project load, {@link #refreshAll(AnalysisEngine)} propagates the
 *       new engine to every workspace so each can update its data in-place.</li>
 * </ul>
 *
 * <h3>Usage in App.java</h3>
 * <pre>{@code
 * workspaceRouter = new WorkspaceRouter(splitPane, overviewPanel);
 * navBar = new BodhakNavBar(workspaceRouter::show);
 * root.setCenter(workspaceRouter.getRoot());   // called once, never again
 * // ...after project load:
 * workspaceRouter.refreshAll(engine);
 * }</pre>
 */
public class WorkspaceRouter {

    private final StackPane root;
    private final Map<NavTab, Workspace> workspaces = new EnumMap<>(NavTab.class);
    private NavTab activeTab = NavTab.OVERVIEW;

    /**
     * Creates the router and all workspace instances.
     *
     * @param overviewSplitPane the full IDE split-pane (file tree + editor + right panel)
     *                          shown exclusively in the OVERVIEW tab
     * @param overviewPanel     the {@link OverviewPanel} displayed inside the split-pane;
     *                          the router delegates refresh calls to it
     */
    public WorkspaceRouter(Node overviewSplitPane, OverviewPanel overviewPanel) {
        this.root = new StackPane();
        this.root.setStyle("-fx-background-color: #0e1415;");

        // ── Instantiate every workspace once ────────────────────────────────
        workspaces.put(NavTab.OVERVIEW,      new OverviewWorkspace(overviewSplitPane, overviewPanel));
        workspaces.put(NavTab.ARCHITECTURE,  new ArchitectureWorkspace());
        workspaces.put(NavTab.CODE_HEALTH,   new CodeHealthWorkspace());
        workspaces.put(NavTab.PERFORMANCE,   new PerformanceWorkspace());
        workspaces.put(NavTab.DEFECTS,       new DefectWorkspace());

        // ── Add every workspace root to the StackPane ────────────────────────
        for (NavTab tab : NavTab.values()) {
            Workspace ws = workspaces.get(tab);
            if (ws == null) continue;
            Node wsRoot = ws.getRoot();
            wsRoot.setVisible(false);
            wsRoot.setManaged(false);
            root.getChildren().add(wsRoot);
        }

        // Show initial tab (OVERVIEW) without animation
        applyVisibility(NavTab.OVERVIEW);
    }

    // =========================================================================
    // ── Public API ────────────────────────────────────────────────────────────
    // =========================================================================

    /**
     * Returns the StackPane that should be set as the application's main content
     * node.  Call {@code root.setCenter(workspaceRouter.getRoot())} once at
     * startup.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Switches the visible workspace to the given tab.
     * Called directly from {@link com.example.bodhakfrontend.ui.nav.BodhakNavBar}.
     *
     * <p>This method is safe to call repeatedly with the same tab; it is a no-op
     * when the requested tab is already active.
     *
     * @param tab the tab to navigate to
     */
    public void show(NavTab tab) {
        if (tab == activeTab) return;
        applyVisibility(tab);
        activeTab = tab;
    }

    /**
     * Propagates a new (or updated) {@link AnalysisEngine} to every workspace.
     *
     * <p>Must be called on the JavaFX Application Thread after a project is
     * loaded or reloaded.
     *
     * @param engine the fresh engine, or {@code null} to reset all workspaces
     *               to their empty state
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
     * participate in layout and consume no extra memory budget.
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
