package com.example.bodhakfrontend.ui.nav.workspace;

import com.example.bodhakfrontend.engine.AnalysisEngine;
import javafx.scene.Node;

/**
 * Contract for every top-level workspace in Bodhak.
 *
 * <p>A Workspace is created <em>once</em> and lives for the full application
 * lifetime.  Navigation never recreates a workspace — it only toggles
 * visibility.  State (zoom, scroll, AI findings, selected nodes, etc.) is
 * therefore preserved automatically.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>Construction — build the entire UI tree and store it in a field.
 *       Do NOT trigger any engine calls here; the engine is not ready yet.</li>
 *   <li>{@link #refresh(AnalysisEngine)} — called by {@link WorkspaceRouter}
 *       whenever a new project is loaded.  Update displayed data in-place
 *       (never replace the root node).</li>
 *   <li>Navigation — {@link WorkspaceRouter} toggles
 *       {@code setVisible / setManaged} on {@link #getRoot()}.
 *       No other action is taken.</li>
 * </ol>
 */
public interface Workspace {

    /**
     * Returns the persistent root node for this workspace.
     * This node is added to the router's {@link javafx.scene.layout.StackPane}
     * exactly once and is never replaced.
     */
    Node getRoot();

    /**
     * Refreshes displayed data using the provided engine.
     * Called on the JavaFX Application Thread after every project load.
     *
     * <p>Implementations should be idempotent: if {@code engine} is the same
     * reference as the last call, they may skip the refresh.
     *
     * @param engine the current {@link AnalysisEngine}, or {@code null} if no
     *               project is loaded
     */
    void refresh(AnalysisEngine engine);
}
