package com.example.bodhak.ui.analysisReport.reference;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.state.DiagnosticsSummaryState;
import com.example.bodhak.ui.analysisReport.state.ExecutiveSummaryState;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * Concrete inspector for Project Health.
 */
public class ProjectHealthInspector extends AnalysisInspectorContent {

    private ExecutiveSummaryState execState;
    private DiagnosticsSummaryState diagState;

    private final Canvas canvas = new Canvas(200, 120);
    private final Label healthPctLabel = new Label("—");
    private final Label healthStatus = new Label("Calculating...");

    private final Label lblFilesVal = new Label("0");
    private final Label lblLocVal = new Label("0");
    private final Label lblEntVal = new Label("0");

    private final Label lblErrorsVal = new Label("0");
    private final Label lblWarningsVal = new Label("0");
    private final Label lblFilesIssuesVal = new Label("0");

    public ProjectHealthInspector() {
        setSpacing(24);
        setPadding(new Insets(10));

        HBox topRow = new HBox(40);
        topRow.setAlignment(Pos.CENTER);
        topRow.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-border-color: rgba(0, 218, 243, 0.1); " +
                        "-fx-border-radius: 12; -fx-padding: 24;");

        // Gauge
        StackPane gaugePane = new StackPane();
        gaugePane.setMinSize(200, 130);
        gaugePane.setPrefSize(200, 130);

        healthPctLabel.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #dce3ec;");
        healthPctLabel.setTranslateY(16);
        gaugePane.getChildren().addAll(canvas, healthPctLabel);

        // Status VBox
        VBox statusBox = new VBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label healthLabel = new Label("PROJECT HEALTH INDEX");
        healthLabel.getStyleClass().add("ar-label-tiny");

        healthStatus.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #56d69b;");

        Label desc = new Label("Index represents the relative health of compiler entities. Higher ratios indicate clean compilation without warnings or errors.");
        desc.setWrapText(true);
        desc.setMaxWidth(300);
        desc.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #849494;");

        statusBox.getChildren().addAll(healthLabel, healthStatus, desc);
        topRow.getChildren().addAll(gaugePane, statusBox);

        // Details Row
        HBox detailsRow = new HBox(20);
        detailsRow.setAlignment(Pos.CENTER);

        VBox metricsBox = buildMetricGroup("CODEBASE SCOPE", "Files", lblFilesVal, "LOC", lblLocVal, "Entities", lblEntVal);
        VBox compilerBox = buildMetricGroup("DIAGNOSTICS & COMPILATION", "Errors", lblErrorsVal, "Warnings", lblWarningsVal, "Files w/ Issues", lblFilesIssuesVal);
        HBox.setHgrow(metricsBox, Priority.ALWAYS);
        HBox.setHgrow(compilerBox, Priority.ALWAYS);

        detailsRow.getChildren().addAll(metricsBox, compilerBox);

        getChildren().addAll(topRow, detailsRow);
    }

    private VBox buildMetricGroup(String title, String m1, Label l1, String m2, Label l2, String m3, Label l3) {
        VBox group = new VBox(12);
        group.setStyle("-fx-background-color: rgba(8,16,24,0.6); -fx-border-color: rgba(59,73,76,0.3); " +
                       "-fx-border-radius: 8; -fx-padding: 16;");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("ar-label-tiny");

        HBox metrics = new HBox(24);
        metrics.setAlignment(Pos.CENTER);

        metrics.getChildren().addAll(
                buildTile(m1, l1),
                buildDivider(),
                buildTile(m2, l2),
                buildDivider(),
                buildTile(m3, l3)
        );

        group.getChildren().addAll(titleLbl, metrics);
        return group;
    }

    private VBox buildTile(String label, Label value) {
        VBox tile = new VBox(4);
        tile.setAlignment(Pos.CENTER);
        HBox.setHgrow(tile, Priority.ALWAYS);

        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; -fx-text-fill: #849494;");

        value.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dce3ec;");

        tile.getChildren().addAll(lbl, value);
        return tile;
    }

    private Region buildDivider() {
        Region r = new Region();
        r.setPrefWidth(1);
        r.setPrefHeight(30);
        r.setStyle("-fx-background-color: rgba(59,73,76,0.4);");
        return r;
    }

    @Override
    public String getTitle() {
        return "Project Health";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("favorite");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #56d69b;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.execState = state.getExecutiveSummaryState();
        this.diagState = state.getDiagnosticsSummaryState();

        healthPctLabel.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    double h = computeHealth();
                    return h < 0 ? "—" : String.format("%.1f%%", h);
                },
                execState.entitiesProperty(),
                diagState.totalErrorsProperty(),
                diagState.totalWarningsProperty()
        ));

        healthStatus.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    double h = computeHealth();
                    if (h < 0)   return "No data";
                    if (h >= 95) return "Excellent Status";
                    if (h >= 80) return "Good Status";
                    if (h >= 60) return "Moderate Issues";
                    return "Needs Attention";
                },
                execState.entitiesProperty(), diagState.totalErrorsProperty()
        ));

        lblFilesVal.textProperty().bind(execState.compilationUnitsProperty().asString());
        lblLocVal.textProperty().bind(execState.linesOfCodeProperty().asString());
        lblEntVal.textProperty().bind(execState.entitiesProperty().asString());

        lblErrorsVal.textProperty().bind(diagState.totalErrorsProperty().asString());
        lblWarningsVal.textProperty().bind(diagState.totalWarningsProperty().asString());
        lblFilesIssuesVal.textProperty().bind(diagState.filesWithIssuesProperty().asString());

        execState.entitiesProperty().addListener((obs, o, n) -> drawGauge(computeHealth() / 100.0));
        diagState.totalErrorsProperty().addListener((obs, o, n) -> drawGauge(computeHealth() / 100.0));

        drawGauge(computeHealth() / 100.0);
    }

    private void drawGauge(double fraction) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 200, 120);

        double cx     = 100;
        double cy     = 105;
        double r      = 85;
        double sw     = 12;
        double clamp  = Math.max(0, Math.min(1, fraction));

        gc.setStroke(Color.web("#3b494c", 0.3));
        gc.setLineWidth(sw);
        gc.strokeArc(cx - r + sw / 2, cy - r + sw / 2,
                (r - sw / 2) * 2, (r - sw / 2) * 2,
                180, -180, ArcType.OPEN);

        if (clamp > 0) {
            gc.setEffect(new javafx.scene.effect.DropShadow(10, Color.web("#56d69b", 0.5)));
            gc.setStroke(Color.web("#56d69b"));
            gc.setLineWidth(sw);
            gc.strokeArc(cx - r + sw / 2, cy - r + sw / 2,
                    (r - sw / 2) * 2, (r - sw / 2) * 2,
                    180, -(180 * clamp), ArcType.OPEN);
            gc.setEffect(null);
        }
    }

    private double computeHealth() {
        if (execState == null || diagState == null) return -1;
        int total  = execState.entitiesProperty().get();
        if (total <= 0) return -1;
        int errors = diagState.totalErrorsProperty().get();
        int warns  = diagState.totalWarningsProperty().get();
        int issues = errors + warns;
        double ratio = (double) issues / total;
        return Math.max(0, 100.0 - ratio * 100.0);
    }
}
