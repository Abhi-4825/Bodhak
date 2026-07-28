package com.example.anuviya.ui.analysisReport.classification;

import com.example.anuviya.ui.analysisReport.state.ProjectClassificationState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

/**
 * A simplified visual representation of the classification intelligence graph.
 * 
 * Flow: Evidence -> Technology -> Capabilities -> Archetype
 */
public class IntelligenceGraphView extends VBox {

    public IntelligenceGraphView() {
        setSpacing(16);
        setPadding(new Insets(10));
        getStyleClass().add("ar-candidates-box");
    }

    public void update(ProjectClassificationState state) {
        getChildren().clear();

        Label title = new Label("INTELLIGENCE GRAPH");
        title.getStyleClass().add("ar-label-tiny");
        Label sub = new Label("The causal chain of classification, derived entirely from source code signals.");
        sub.getStyleClass().add("ar-body-muted");
        sub.setWrapText(true);

        HBox graphRow = new HBox(12);
        graphRow.setAlignment(Pos.CENTER);
        graphRow.setPadding(new Insets(20, 0, 20, 0));

        // 1. Evidence
        VBox col1 = buildColumn("EVIDENCE", 
                state.getEvidence().stream()
                        .map(ProjectClassificationState.EvidenceEntry::category)
                        .distinct()
                        .limit(4)
                        .collect(Collectors.toList()));

        // 2. Technologies
        VBox col2 = buildColumn("TECHNOLOGIES", 
                state.getDetectedTechnologies().stream()
                        .map(ProjectClassificationState.TechnologyEntry::name)
                        .limit(4)
                        .collect(Collectors.toList()));

        // 3. Capabilities
        VBox col3 = buildColumn("CAPABILITIES", 
                state.getCapabilities().stream()
                        .map(ProjectClassificationState.CapabilityEntry::name)
                        .limit(4)
                        .collect(Collectors.toList()));

        // 4. Archetype
        VBox col4 = new VBox(8);
        col4.setAlignment(Pos.CENTER);
        Label archLbl = new Label("ARCHETYPE");
        archLbl.getStyleClass().add("ar-label-tiny");
        Label archVal = new Label(state.primaryClassificationProperty().get().toUpperCase());
        archVal.getStyleClass().add("ar-hero-badge-primary");
        col4.getChildren().addAll(archLbl, archVal);

        graphRow.getChildren().addAll(
                col1, buildArrow(),
                col2, buildArrow(),
                col3, buildArrow(),
                col4
        );

        getChildren().addAll(title, sub, graphRow);
    }

    private VBox buildColumn(String title, java.util.List<String> items) {
        VBox col = new VBox(8);
        col.setAlignment(Pos.CENTER);
        Label header = new Label(title);
        header.getStyleClass().add("ar-label-tiny");
        col.getChildren().add(header);

        for (String item : items) {
            Label lbl = new Label(item);
            lbl.getStyleClass().add("ar-graph-item-chip");
            col.getChildren().add(lbl);
        }
        if (items.isEmpty()) {
            Label lbl = new Label("None");
            lbl.getStyleClass().add("ar-body-muted");
            col.getChildren().add(lbl);
        }

        return col;
    }

    private Label buildArrow() {
        Label arrow = new Label("arrow_forward");
        arrow.getStyleClass().add("ar-graph-arrow");
        return arrow;
    }
}
