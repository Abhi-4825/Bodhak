package com.example.bodhak.ui.analysisReport.reference;

import com.example.bodhak.ui.analysisReport.state.DiagnosticsSummaryState;
import com.example.bodhak.ui.analysisReport.state.ExecutiveSummaryState;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * Project Health Card — Row 3, right column, bottom card.
 *
 * Shows a Canvas-drawn gauge arc (semi-circle) representing health score,
 * derived from the ratio of healthy entities to total entities.
 * Health = 100% - (issues / total entities * 100).
 */
public class ProjectHealthCard extends HBox {

    private final ExecutiveSummaryState execState;
    private final DiagnosticsSummaryState diagState;
    private Canvas canvas;
    private final double GAUGE_W = 100;
    private final double GAUGE_H = 60;

    public ProjectHealthCard(ExecutiveSummaryState execState, DiagnosticsSummaryState diagState) {
        this.execState = execState;
        this.diagState = diagState;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setStyle("-fx-border-left-color: rgba(86,214,155,0.5); -fx-border-left-width: 4;");
        setPadding(new Insets(18, 20, 18, 18));
        setSpacing(16);
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);

        // ── Gauge ──────────────────────────────────────────────────
        StackPane gaugePane = new StackPane();
        gaugePane.setMinSize(GAUGE_W, GAUGE_H + 20);
        gaugePane.setPrefSize(GAUGE_W, GAUGE_H + 20);
        gaugePane.setMaxSize(GAUGE_W, GAUGE_H + 20);

        canvas = new Canvas(GAUGE_W, GAUGE_H + 20);
        drawGauge(0.0);

        Label pctLabel = new Label("—");
        pctLabel.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 18px; " +
                          "-fx-font-weight: 900; -fx-text-fill: #dce3ec;");
        pctLabel.setTranslateY(8); // shift down into lower half of gauge
        pctLabel.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    double h = computeHealth();
                    return h < 0 ? "—" : String.format("%.1f%%", h);
                },
                execState.entitiesProperty(),
                diagState.totalErrorsProperty(),
                diagState.totalWarningsProperty()
        ));

        gaugePane.getChildren().addAll(canvas, pctLabel);

        // Redraw when health changes
        execState.entitiesProperty().addListener((obs, o, n) -> drawGauge(computeHealth() / 100.0));
        diagState.totalErrorsProperty().addListener((obs, o, n) -> drawGauge(computeHealth() / 100.0));

        // ── Right info panel ───────────────────────────────────────
        VBox info = new VBox(4);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titleLabel = new Label("Project Health");
        titleLabel.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 16px; " +
                            "-fx-font-weight: 800; -fx-text-fill: #dce3ec;");

        Label statusLabel = new Label("Calculating...");
        statusLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 12px; " +
                             "-fx-font-weight: 700; -fx-text-fill: #56d69b;");
        statusLabel.textProperty().bind(Bindings.createStringBinding(
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

        // Healthy / Issues split
        HBox countsRow = new HBox(16);
        countsRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(countsRow, new Insets(8, 0, 0, 0));

        VBox healthyBox = new VBox(2);
        Label healthyLbl = new Label("HEALTHY");
        healthyLbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; " +
                            "-fx-text-fill: #849494;");
        Label healthyCount = new Label("0");
        healthyCount.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 15px; " +
                              "-fx-font-weight: 700; -fx-text-fill: #dce3ec;");
        healthyCount.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    int total  = execState.entitiesProperty().get();
                    int issues = diagState.totalErrorsProperty().get()
                               + diagState.totalWarningsProperty().get();
                    return String.valueOf(Math.max(0, total - issues));
                },
                execState.entitiesProperty(),
                diagState.totalErrorsProperty(),
                diagState.totalWarningsProperty()
        ));
        healthyBox.getChildren().addAll(healthyLbl, healthyCount);

        Region sep = new Region();
        sep.setPrefWidth(1);
        sep.setPrefHeight(30);
        sep.setStyle("-fx-background-color: rgba(59,73,76,0.4);");

        VBox issuesBox = new VBox(2);
        Label issuesLbl = new Label("ISSUES");
        issuesLbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; " +
                           "-fx-text-fill: #FF4B4B;");
        Label issuesCount = new Label("0");
        issuesCount.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 15px; " +
                             "-fx-font-weight: 700; -fx-text-fill: #dce3ec;");
        issuesCount.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(diagState.totalErrorsProperty().get()
                                   + diagState.totalWarningsProperty().get()),
                diagState.totalErrorsProperty(), diagState.totalWarningsProperty()
        ));
        issuesBox.getChildren().addAll(issuesLbl, issuesCount);

        countsRow.getChildren().addAll(healthyBox, sep, issuesBox);
        info.getChildren().addAll(titleLabel, statusLabel, countsRow);

        getChildren().addAll(gaugePane, info);
    }

    private void drawGauge(double fraction) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, GAUGE_W, GAUGE_H + 20);

        double cx     = GAUGE_W / 2;
        double cy     = GAUGE_H + 4;
        double r      = GAUGE_H - 4;
        double sw     = 10;
        double clamp  = Math.max(0, Math.min(1, fraction));

        // Track arc (grey)
        gc.setStroke(Color.web("#3b494c", 0.3));
        gc.setLineWidth(sw);
        gc.strokeArc(cx - r + sw / 2, cy - r + sw / 2,
                (r - sw / 2) * 2, (r - sw / 2) * 2,
                180, -180, ArcType.OPEN);

        if (clamp > 0) {
            // Fill arc (green)
            gc.setEffect(new javafx.scene.effect.DropShadow(8, Color.web("#56d69b", 0.5)));
            gc.setStroke(Color.web("#56d69b"));
            gc.setLineWidth(sw);
            gc.strokeArc(cx - r + sw / 2, cy - r + sw / 2,
                    (r - sw / 2) * 2, (r - sw / 2) * 2,
                    180, -(180 * clamp), ArcType.OPEN);
            gc.setEffect(null);
        }
    }

    private double computeHealth() {
        int total  = execState.entitiesProperty().get();
        if (total <= 0) return -1;
        int errors = diagState.totalErrorsProperty().get();
        int warns  = diagState.totalWarningsProperty().get();
        int issues = errors + warns;
        double ratio = (double) issues / total;
        return Math.max(0, 100.0 - ratio * 100.0);
    }
}
