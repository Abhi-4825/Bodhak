package com.example.bodhakfrontend.ui.workspace;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages per-tab workspace state.
 * Each tab independently tracks whether it is in EDITOR mode or ANALYSIS mode.
 * Switching tabs instantly restores the previous state of the newly selected tab.
 */
public class TabStateManager {

    private final SplitPane splitPane;
    private final Map<Tab, AnalysisWorkspaceView> analysisViews = new HashMap<>();
    private final Map<Tab, Boolean> tabAnalysisMode = new HashMap<>();

    public TabStateManager(SplitPane splitPane) {
        this.splitPane = splitPane;
    }

    /**
     * Wraps a code node in a StackPane suitable for use as tab content.
     * The StackPane acts as the base layer; the analysis overlay is added on demand.
     */
    public static StackPane createTabContentStack(Node codeNode) {
        StackPane stack = new StackPane(codeNode);
        return stack;
    }

    /**
     * Enters analysis mode for the given tab.
     * Shows the AnalysisWorkspaceView overlay on top of the code area.
     */
    public void enterAnalysisMode(Tab tab, Node depsView, Node methodsView, Node healthView, Node selector) {
        if (!(tab.getContent() instanceof StackPane contentStack)) return;

        // Create or reuse the per-tab analysis view
        AnalysisWorkspaceView analysisView = analysisViews.computeIfAbsent(tab, t -> {
            AnalysisWorkspaceView view = new AnalysisWorkspaceView();
            view.getRestoreBtn().setOnAction(e -> exitAnalysisMode(t));
            return view;
        });

        analysisView.setOverviewContent(depsView, methodsView, healthView);
        if (selector != null) analysisView.setEntitySelector(selector);

        // Add to the tab's StackPane if not already present
        if (!contentStack.getChildren().contains(analysisView)) {
            contentStack.getChildren().add(analysisView);
        }
        analysisView.setVisible(true);
        analysisView.setManaged(true);

        tabAnalysisMode.put(tab, true);
        animateSidebar(true);
    }

    /**
     * Exits analysis mode for the given tab, restoring the code area.
     */
    public void exitAnalysisMode(Tab tab) {
        AnalysisWorkspaceView analysisView = analysisViews.get(tab);
        if (analysisView != null) {
            analysisView.setVisible(false);
            analysisView.setManaged(false);
        }
        tabAnalysisMode.put(tab, false);
        animateSidebar(false);
    }

    public boolean isAnalysisMode(Tab tab) {
        return tabAnalysisMode.getOrDefault(tab, false);
    }

    /**
     * Called when the user switches to a different tab.
     * Animates the sidebar to match the new tab's state.
     */
    public void onTabSwitched(Tab newTab) {
        if (newTab == null) return;
        animateSidebar(isAnalysisMode(newTab));
    }

    /** Cleans up state when a tab is closed. */
    public void onTabClosed(Tab tab) {
        analysisViews.remove(tab);
        tabAnalysisMode.remove(tab);
    }

    /** Clears all state, e.g. when a new project is loaded. */
    public void clearAll() {
        analysisViews.clear();
        tabAnalysisMode.clear();
    }

    private void animateSidebar(boolean collapse) {
        if (splitPane.getDividers().size() < 2) return;
        double targetRight = collapse ? 0.98 : 0.58;
        new Timeline(new KeyFrame(Duration.millis(280),
                new KeyValue(splitPane.getDividers().get(1).positionProperty(), targetRight)
        )).play();
    }
}
