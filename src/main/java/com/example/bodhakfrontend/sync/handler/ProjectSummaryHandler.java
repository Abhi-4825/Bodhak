package com.example.bodhakfrontend.sync.handler;

import com.example.bodhakfrontend.sync.api.UiUpdateEvent;
import com.example.bodhakfrontend.sync.events.ProjectSummaryChangedEvent;
import com.example.bodhakfrontend.sync.store.UIStore;
import com.example.bodhakfrontend.ui.ProjectAnalysis.ProjectAnalysisUi;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;

/**
 * Handles {@link ProjectSummaryChangedEvent}.
 *
 * <ol>
 *   <li>Updates {@link UIStore} with the new {@link com.example.bodhakfrontend.core.model.project.ProjectInfo}.</li>
 *   <li>If the Analyze tab is open, refreshes it — no full rebuild if the tab is closed.</li>
 * </ol>
 *
 * <p>Because the dispatcher collapses consecutive {@link ProjectSummaryChangedEvent}s
 * (last-write-wins), this handler is called at most once per dispatch tick regardless
 * of how many file saves occurred in that window.
 *
 * <p>Runs on the JavaFX Application Thread.
 */
public final class ProjectSummaryHandler implements UiUpdateHandler {

    private final UIStore store;
    private final RightPanelTabManager rightPanel;
    private final ProjectAnalysisUi analysisUi;

    public ProjectSummaryHandler(UIStore store,
                                  RightPanelTabManager rightPanel,
                                  ProjectAnalysisUi analysisUi) {
        this.store      = store;
        this.rightPanel = rightPanel;
        this.analysisUi = analysisUi;
    }

    @Override
    public boolean canHandle(UiUpdateEvent event) {
        return event instanceof ProjectSummaryChangedEvent;
    }

    @Override
    public void apply(UiUpdateEvent event) {
        ProjectSummaryChangedEvent e = (ProjectSummaryChangedEvent) event;
        // 1. Push to store → any bound UI component updates automatically
        store.setProjectInfo(e.context().getProjectInfo());
        // 2. Refresh the analysis tab only if it's already open (lazy update)
        rightPanel.refreshAnalyzeTabIfOpen(
                () -> analysisUi.build(e.context())
        );
    }
}
