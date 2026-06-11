package com.example.bodhakfrontend.ui.dashboard.architecture.model;

import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.GraphSnapshot;
import com.example.bodhakfrontend.ui.dashboard.architecture.graph.*;
import java.util.function.Consumer;

public class ArchitectureWorkspaceState {

    private ArchitectureGraphView architectureView;
    private DependencyGraphView dependencyView;
    private CycleGraphView cycleView;
    private HotspotGraphView hotspotView;

    // References for lazy initialization
    private GraphSnapshot snapshot;
    private ProjectInfo projectInfo;
    private ArchitectureGraphState state;
    private Consumer<String> currentListener;

    public void initialize(
            GraphSnapshot snapshot,
            ProjectInfo projectInfo,
            ArchitectureGraphState state,
            Consumer<String> nodeSelected
    ) {
        this.snapshot = snapshot;
        this.projectInfo = projectInfo;
        this.state = state;
        this.currentListener = nodeSelected;

        // Eagerly initialize the default Architecture view
        getArchitectureView();
    }

    public void clear() {
        this.architectureView = null;
        this.dependencyView = null;
        this.cycleView = null;
        this.hotspotView = null;
        this.snapshot = null;
        this.projectInfo = null;
        this.state = null;
        this.currentListener = null;
    }

    public void setNodeSelectedListener(Consumer<String> listener) {
        this.currentListener = listener;
        if (architectureView != null) {
            architectureView.setNodeSelected(listener);
        }
        if (dependencyView != null) {
            dependencyView.setNodeSelected(listener);
        }
        if (cycleView != null) {
            cycleView.setNodeSelected(listener);
        }
        if (hotspotView != null) {
            hotspotView.setNodeSelected(listener);
        }
    }

    public ArchitectureGraphView getArchitectureView() {
        if (architectureView == null && snapshot != null) {
            architectureView = new ArchitectureGraphView(snapshot, projectInfo, state, currentListener);
        }
        return architectureView;
    }

    public DependencyGraphView getDependencyView() {
        if (dependencyView == null && snapshot != null) {
            dependencyView = new DependencyGraphView(snapshot, projectInfo, state, currentListener);
        }
        return dependencyView;
    }

    public CycleGraphView getCycleView() {
        if (cycleView == null && snapshot != null) {
            cycleView = new CycleGraphView(snapshot, projectInfo, state, currentListener);
        }
        return cycleView;
    }

    public HotspotGraphView getHotspotView() {
        if (hotspotView == null && snapshot != null) {
            hotspotView = new HotspotGraphView(snapshot, projectInfo, state, currentListener);
        }
        return hotspotView;
    }

    public void setArchitectureView(ArchitectureGraphView view) {
        this.architectureView = view;
    }

    public void setDependencyView(DependencyGraphView view) {
        this.dependencyView = view;
    }

    public void setCycleView(CycleGraphView view) {
        this.cycleView = view;
    }

    public void setHotspotView(HotspotGraphView view) {
        this.hotspotView = view;
    }
}
