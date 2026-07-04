package com.example.bodhak.context.state;

import com.example.bodhak.context.AnalysisContext;

/**
 * Composite state representing the entire IDE's state container.
 * Propagates analysis context updates to all modular sub-states.
 */
public final class AppState {

    private final WorkspaceState workspaceState;
    private final AnalysisReportState dashboardState;
    private final ArchitectureState architectureState;
    private final DependencyExplorerState dependencyState;
    private final AiAssistantState aiState;

    public AppState(
            WorkspaceState workspaceState,
            AnalysisReportState dashboardState,
            ArchitectureState architectureState,
            DependencyExplorerState dependencyState,
            AiAssistantState aiState
    ) {
        this.workspaceState = workspaceState;
        this.dashboardState = dashboardState;
        this.architectureState = architectureState;
        this.dependencyState = dependencyState;
        this.aiState = aiState;
    }

    public WorkspaceState workspaceState() {
        return workspaceState;
    }

    public AnalysisReportState dashboardState() {
        return dashboardState;
    }

    public ArchitectureState architectureState() {
        return architectureState;
    }

    public DependencyExplorerState dependencyExplorerState() {
        return dependencyState;
    }

    public AiAssistantState aiState() {
        return aiState;
    }

    public void update(AnalysisContext context) {
        if (workspaceState != null) workspaceState.update(context);
        if (dashboardState != null) dashboardState.update(context);
        if (architectureState != null) architectureState.update(context);
        if (dependencyState != null) dependencyState.update(context);
        if (aiState != null) aiState.update(context);
    }
}
