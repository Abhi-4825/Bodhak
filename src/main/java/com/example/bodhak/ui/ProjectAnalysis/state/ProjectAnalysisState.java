package com.example.bodhak.ui.ProjectAnalysis.state;

import com.example.bodhak.context.AnalysisContext;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

public class ProjectAnalysisState implements com.example.bodhak.context.state.WorkspaceState {
    private final ObjectProperty<AnalysisContext> analysisContext =
            new SimpleObjectProperty<>();
    public ReadOnlyObjectProperty<AnalysisContext> analysisContextProperty() {
        return analysisContext;
    }
    public AnalysisContext getAnalysisContext() {
        return analysisContext.get();
    }
    private final ProjectSummaryState projectSummary =
            new ProjectSummaryState();
    private final EntryPointState entryPointState=new EntryPointState();
    public ProjectSummaryState getProjectSummary() {
        return projectSummary;
    }
    public EntryPointState getEntryPoint() {
        return entryPointState;
    }
    private final NamespaceOverviewState namespaceOverview =
            new NamespaceOverviewState();

    public NamespaceOverviewState getNamespaceOverview() {
        return namespaceOverview;
    }
    private final LargestFilesState largestFiles =
            new LargestFilesState();

    public LargestFilesState getLargestFiles() {
        return largestFiles;
    }
  private EntityMetricsState entityMetricsState=new EntityMetricsState();

    public EntityMetricsState getEntityMetricsState() {
        return entityMetricsState;
    }
    private final HotspotState hotspot =
            new HotspotState();

    public HotspotState getHotspot() {
        return hotspot;
    }

    private final ProjectTypeState projectTypeState = new ProjectTypeState();
    public ProjectTypeState getProjectTypeState() { return projectTypeState; }
    public void update(AnalysisContext context) {
        analysisContext.set(context);
        projectSummary.update(context);
        entryPointState.update(context);
        namespaceOverview.update(context);
        largestFiles.update(context);
        entityMetricsState.update(context);
        hotspot.update(context);
        projectTypeState.update(context);
    }
}
