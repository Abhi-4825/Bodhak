package com.example.anuviya.ui.dashboard.architecture.graph;

import com.example.anuviya.model.project.ProjectInfo;
import com.example.anuviya.context.GraphSnapshot;
import com.example.anuviya.ui.dashboard.architecture.model.ArchitectureGraphState;
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
