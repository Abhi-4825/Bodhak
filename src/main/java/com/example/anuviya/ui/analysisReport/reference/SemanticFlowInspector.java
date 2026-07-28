package com.example.anuviya.ui.analysisReport.reference;

import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import com.example.anuviya.ui.analysisReport.state.SemanticFlowState;
import com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Concrete inspector for Semantic References.
 */
public class SemanticFlowInspector extends AnalysisInspectorContent {

    private final VBox listContainer = new VBox(12);
    private final Label totalRefsLabel = new Label();
    private SemanticFlowState state;

    public SemanticFlowInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("ALL SEMANTIC REFERENCES");
        label.getStyleClass().add("ar-label-tiny");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalRefsLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #00daf3; -fx-font-weight: bold;");

        header.getChildren().addAll(label, spacer, totalRefsLabel);

        getChildren().addAll(header, listContainer);
    }

    @Override
    public String getTitle() {
        return "Semantic References";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("🔗");
        icon.setStyle("-fx-font-size: 24px;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getSemanticFlowState();

        totalRefsLabel.textProperty().bind(
                Bindings.concat("Total References: ", this.state.totalRefsProperty().asString())
        );

        this.state.getSegments().addListener((ListChangeListener<SemanticFlowState.FlowSegment>) c -> rebuild());
        rebuild();
    }

    private void rebuild() {
        listContainer.getChildren().clear();
        double total = state.getTotalRefs();
        if (total == 0) return;

        for (var segment : state.getSegments()) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-border-color: rgba(59,73,76,0.15); " +
                          "-fx-border-radius: 8; -fx-padding: 16;");

            Circle colorDot = new Circle(6, Color.web(segment.color()));

            Label label = new Label(segment.label());
            label.setStyle("-fx-font-family: 'Epilogue'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e6f1f3;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label count = new Label(String.format("%,d references", segment.count()));
            count.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-text-fill: #bac9cc;");

            double pctVal = segment.count() / total;
            Label pct = new Label(String.format("%.1f%%", pctVal * 100));
            pct.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + segment.color() + ";");

            row.getChildren().addAll(colorDot, label, spacer, count, pct);
            listContainer.getChildren().add(row);
        }
    }
}
