package com.example.bodhakfrontend.ui.nav.workspace.impl;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.engine.GraphSnapshot;
import com.example.bodhakfrontend.ui.dashboard.architecture.ArchitectureDashboard;
import com.example.bodhakfrontend.ui.dashboard.architecture.components.NodeInspectorPanel;
import com.example.bodhakfrontend.ui.dashboard.architecture.model.ArchitectureGraphState;
import com.example.bodhakfrontend.ui.dashboard.architecture.model.ArchitectureWorkspaceState;
import com.example.bodhakfrontend.ui.nav.workspace.Workspace;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Persistent workspace for the ARCHITECTURE tab.
 *
 * <h3>State preservation</h3>
 * <ul>
 *   <li>Graph zoom and pan — stored in {@link ArchitectureGraphState}</li>
 *   <li>Node layout positions (per view mode) — cached in {@link ArchitectureGraphState}</li>
 *   <li>Selected node and inspector content — {@link ArchitectureWorkspaceState}</li>
 *   <li>Constructed graph view nodes — held by {@link ArchitectureWorkspaceState}</li>
 * </ul>
 *
 * <p>The dashboard is built <em>lazily</em> on the first {@link #refresh(AnalysisEngine)}
 * call that provides a non-null engine.  Subsequent navigation to this tab
 * simply toggles visibility; the node tree is never recreated.
 *
 * <p>When a <em>new</em> project is loaded (different engine reference),
 * the graph and workspace states are cleared and the dashboard is rebuilt
 * inside the same persistent root container.
 */
public class ArchitectureWorkspace implements Workspace {

    // ── Persistent root — added to the StackPane once, never replaced ────────
    private final StackPane root;

    // ── Graph state — survives tab switching ─────────────────────────────────
    private final ArchitectureGraphState graphState;
    private final ArchitectureWorkspaceState workspaceState;

    // ── Last engine seen — guards against unnecessary rebuilds ────────────────
    private AnalysisEngine lastEngine;

    public ArchitectureWorkspace() {
        this.root           = new StackPane();
        this.graphState     = new ArchitectureGraphState();
        this.workspaceState = new ArchitectureWorkspaceState();

        root.setStyle("-fx-background-color: #0e1415;");

        // Show empty state until a project is loaded
        root.getChildren().add(buildEmptyState());
    }

    // =========================================================================
    // ── Workspace API ─────────────────────────────────────────────────────────
    // =========================================================================

    @Override
    public Node getRoot() {
        return root;
    }

    /**
     * Refreshes the Architecture dashboard with the supplied engine.
     *
     * <ul>
     *   <li>If {@code engine == null} → show the "no project" empty state.</li>
     *   <li>If {@code engine == lastEngine} → no-op (state fully preserved).</li>
     *   <li>If {@code engine} is a new reference → clear state and rebuild.</li>
     * </ul>
     */
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
            System.err.println("[ArchitectureWorkspace] Dashboard build failed: " + ex.getMessage());
            ex.printStackTrace();
            root.getChildren().add(buildErrorState(ex));
        }
    }

    // =========================================================================
    // ── Private helpers ───────────────────────────────────────────────────────
    // =========================================================================

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
