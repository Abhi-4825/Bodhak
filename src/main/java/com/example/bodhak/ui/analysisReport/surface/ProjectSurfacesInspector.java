package com.example.bodhak.ui.analysisReport.surface;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.state.ProjectSurfacesState;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Concrete inspector for Project Surfaces.
 */
public class ProjectSurfacesInspector extends AnalysisInspectorContent {

    private final GridPane grid = new GridPane();
    private ProjectSurfacesState state;

    public ProjectSurfacesInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        Label label = new Label("ALL PROJECT SURFACES & CAPABILITIES");
        label.getStyleClass().add("ar-label-tiny");

        grid.setHgap(20);
        grid.setVgap(20);
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        getChildren().addAll(label, grid);
    }

    @Override
    public String getTitle() {
        return "Project Surfaces";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("🎯");
        icon.setStyle("-fx-font-size: 24px;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getProjectSurfacesState();
        this.state.getSurfaces().addListener((ListChangeListener<ProjectSurfacesState.SurfaceEntry>) c -> rebuild());
        rebuild();
    }

    private void rebuild() {
        grid.getChildren().clear();
        var surfaces = state.getSurfaces();
        for (int i = 0; i < surfaces.size(); i++) {
            var s = surfaces.get(i);
            int col = i % 2;
            int row = i / 2;

            HBox cell = new HBox(16);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-border-color: rgba(0, 218, 243, 0.1); " +
                          "-fx-border-radius: 8; -fx-padding: 16;");

            Label icon = new Label(s.icon());
            icon.setStyle("-fx-font-size: 28px; -fx-text-fill: #00daf3;");

            VBox textCol = new VBox(4);
            Label name = new Label(s.name());
            name.setStyle("-fx-font-family: 'Epilogue'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e6f1f3;");

            Label detail = new Label(s.detail().equals("Placeholder") ? "Static analyzer resolved capability matching." : s.detail());
            detail.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #849494;");

            textCol.getChildren().addAll(name, detail);
            cell.getChildren().addAll(icon, textCol);
            HBox.setHgrow(textCol, Priority.ALWAYS);

            grid.add(cell, col, row);
        }
    }
}
