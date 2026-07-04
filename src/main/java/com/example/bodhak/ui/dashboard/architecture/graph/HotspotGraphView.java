package com.example.bodhak.ui.dashboard.architecture.graph;

import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.context.GraphSnapshot;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureGraphState;
import java.util.function.Consumer;

public class HotspotGraphView extends CanvasArchitectureGraphView {
    public HotspotGraphView(
            GraphSnapshot snapshot,
            ProjectInfo projectInfo,
            ArchitectureGraphState state,
            Consumer<String> nodeSelected
    ) {
        super(snapshot, projectInfo, state, GraphViewMode.HOTSPOTS, nodeSelected);
    }
}
