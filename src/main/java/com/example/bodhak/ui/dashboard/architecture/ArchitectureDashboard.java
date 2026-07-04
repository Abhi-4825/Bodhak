package com.example.bodhak.ui.dashboard.architecture;
import com.example.bodhak.model.namespace.NamespaceInfo;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.orchestration.AnalysisEngine;
import com.example.bodhak.context.GraphSnapshot;
import com.example.bodhak.ui.dashboard.architecture.analysis.ArchitectureDashboardDataBuilder;
import com.example.bodhak.ui.dashboard.architecture.components.*;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureDashboardData;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureGraphState;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureWorkspaceState;


import com.example.bodhak.ui.dashboard.common.component.DashboardHeader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;

public class ArchitectureDashboard {

    private final AnalysisEngine engine;
    private final ArchitectureGraphState state;
    private final ArchitectureWorkspaceState workspaceState;

    public ArchitectureDashboard(
            AnalysisEngine engine,
            ArchitectureGraphState state,
            ArchitectureWorkspaceState workspaceState
    ) {
        this.engine = engine;
        this.state = state;
        this.workspaceState = workspaceState;
    }

    public Node build() {

        if (engine == null) {
            return new VBox();
        }

        AnalysisContext context = engine.getAnalysisContextManager().getCurrentContext();

        ArchitectureDashboardDataBuilder builder =
                new ArchitectureDashboardDataBuilder();

        ArchitectureDashboardData data =
                builder.build(context);

        GraphSnapshot snapshot =
                engine.getGraphSnapshot();

        NodeInspectorPanel inspector =
                new NodeInspectorPanel();

        if (state.getNamespaceMap().isEmpty()) {
            state.setNamespaceMap(context.getNamespaces());
        }

        if (workspaceState.getArchitectureView() == null) {
            workspaceState.initialize(snapshot, engine.getProjectInfo(), state, null);
        }

        workspaceState.setNodeSelectedListener(entity -> {
            NamespaceInfo info = state.getNamespaceMap().get(entity);
            inspector.update(info);
        });

        ArchitectureGraphPanel graphPanel =
                new ArchitectureGraphPanel(workspaceState);

        VBox dashboard =
                new VBox(20);

        dashboard.setPadding(
                new Insets(20)
        );

        HBox splitLayout = new HBox(20);
        HBox.setHgrow(graphPanel, Priority.ALWAYS);
        graphPanel.setMaxWidth(Double.MAX_VALUE);
        inspector.setPrefWidth(350);
        inspector.setMinWidth(350);
        splitLayout.getChildren().addAll(graphPanel, inspector);

        dashboard.getChildren().addAll(
                new DashboardHeader(
                        "Architecture Dashboard",
                        "Structural dependency analysis and architectural intelligence"
                ),
                new ArchitectureMetricRow(data),
                splitLayout
        );

        BorderPane root = new BorderPane();
        root.setCenter(dashboard);
        return root;
    }
}
