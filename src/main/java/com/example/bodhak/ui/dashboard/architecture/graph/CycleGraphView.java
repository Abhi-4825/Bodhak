package com.example.bodhak.ui.dashboard.architecture.graph;

import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.context.GraphSnapshot;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureGraphState;
import java.util.function.Consumer;

public class CycleGraphView extends CanvasArchitectureGraphView {
    public CycleGraphView(
            GraphSnapshot snapshot,
            ProjectInfo projectInfo,
            ArchitectureGraphState state,
            Consumer<String> nodeSelected
    ) {
        super(snapshot, projectInfo, state, GraphViewMode.CYCLES, nodeSelected);
    }
}
