package com.example.bodhakfrontend.ui.dashboard.architecture.graph;

import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.GraphSnapshot;
import com.example.bodhakfrontend.ui.dashboard.architecture.model.ArchitectureGraphState;
import java.util.function.Consumer;

public class ArchitectureGraphView extends CanvasArchitectureGraphView {
    public ArchitectureGraphView(
            GraphSnapshot snapshot,
            ProjectInfo projectInfo,
            ArchitectureGraphState state,
            Consumer<String> nodeSelected
    ) {
        super(snapshot, projectInfo, state, GraphViewMode.ARCHITECTURE, nodeSelected);
    }
}
