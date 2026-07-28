package com.example.anuviya.ui.analysisReport.reference;

import com.example.anuviya.ui.analysisReport.state.SemanticFlowState;
import com.example.anuviya.ui.analysisReport.state.SemanticFlowState.FlowSegment;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Semantic Flow Card — Row 3, right column, top card.
 *
 * Shows a Canvas-based donut ring chart with a legend.
 * Canvas redraws whenever the card resizes or state changes.
 * Shows an empty state label when there are no references.
 */
public class SemanticFlowCard extends VBox {

    private final SemanticFlowState state;
    private Canvas canvas;
    private final double CANVAS_SIZE = 120;

    public SemanticFlowCard(SemanticFlowState state) {
        this.state = state;
        initialise();
        bindState();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setPadding(new Insets(20));
        setSpacing(0);
        setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);

        // ── Title ──────────────────────────────────────────────────
        Label title = new Label("Semantic Flow");
        title.getStyleClass().add("ar-section-title");

        Label subtitle = new Label("Internal Reference Distribution");
        subtitle.getStyleClass().add("ar-body-muted");
        VBox.setMargin(subtitle, new Insets(2, 0, 14, 0));

        // ── Main body: legend (left) + donut (right) ───────────────
        HBox body = new HBox(16);
        body.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Legend column
        VBox legend = new VBox(8);
        legend.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(legend, Priority.ALWAYS);
        rebuildLegend(legend);

        state.getSegments().addListener((ListChangeListener<FlowSegment>) c -> rebuildLegend(legend));

        // Donut canvas
        StackPane donutPane = buildDonutPane();

        body.getChildren().addAll(legend, donutPane);
        getChildren().addAll(title, subtitle, body);
    }

    private StackPane buildDonutPane() {
        StackPane pane = new StackPane();
        pane.setMinSize(CANVAS_SIZE, CANVAS_SIZE);
        pane.setPrefSize(CANVAS_SIZE, CANVAS_SIZE);
        pane.setMaxSize(CANVAS_SIZE, CANVAS_SIZE);

        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        drawDonut();

        // Center label showing total
        VBox centerLabel = new VBox(2);
        centerLabel.setAlignment(Pos.CENTER);
        Label countLabel = new Label("0");
        countLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 15px; " +
                            "-fx-font-weight: 700; -fx-text-fill: #dce3ec;");
        countLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> formatCount(state.getTotalRefs()),
                        state.totalRefsProperty()
                )
        );
        Label countLbl2 = new Label("TOTAL REFS");
        countLbl2.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 7px; " +
                           "-fx-font-weight: 700; -fx-text-fill: #849494; -fx-letter-spacing: 0.1em;");

        centerLabel.getChildren().addAll(countLabel, countLbl2);

        pane.getChildren().addAll(canvas, centerLabel);
        state.getSegments().addListener((ListChangeListener<FlowSegment>) c -> drawDonut());
        return pane;
    }

    private void drawDonut() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        double cx = CANVAS_SIZE / 2;
        double cy = CANVAS_SIZE / 2;
        double outerR = CANVAS_SIZE / 2 - 4;
        double innerR = outerR * 0.62;
        double strokeW = outerR - innerR;

        var segs = state.getSegments();
        long total = state.getTotalRefs();

        if (total == 0 || segs.isEmpty()) {
            // Draw empty ring
            gc.setStroke(Color.web("#3b494c", 0.4));
            gc.setLineWidth(strokeW);
            gc.strokeArc(cx - outerR + strokeW / 2, cy - outerR + strokeW / 2,
                    (outerR - strokeW / 2) * 2, (outerR - strokeW / 2) * 2,
                    0, 360, javafx.scene.shape.ArcType.OPEN);
            return;
        }

        double startAngle = 90; // Start from top
        for (FlowSegment seg : segs) {
            double sweep = (seg.count() * 360.0) / total;
            Color color = Color.web(seg.color());

            gc.setStroke(color);
            gc.setLineWidth(strokeW);
            double r = outerR - strokeW / 2;
            gc.strokeArc(cx - r, cy - r, r * 2, r * 2,
                    startAngle, -sweep, javafx.scene.shape.ArcType.OPEN);

            // Tiny gap between segments
            startAngle -= sweep + 1.5;
        }

        // Glow on first segment (cyan)
        if (!segs.isEmpty()) {
            gc.setEffect(new javafx.scene.effect.DropShadow(8, Color.web("#00daf3", 0.5)));
            FlowSegment first = segs.get(0);
            double sweep = (first.count() * 360.0) / total;
            double r = outerR - strokeW / 2;
            gc.setStroke(Color.web(first.color()));
            gc.setLineWidth(strokeW);
            gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 90, -sweep, javafx.scene.shape.ArcType.OPEN);
            gc.setEffect(null);
        }
    }

    private void rebuildLegend(VBox legend) {
        legend.getChildren().clear();
        for (FlowSegment seg : state.getSegments()) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
            dot.setFill(Color.web(seg.color()));

            long total = state.getTotalRefs();
            String pct = total > 0
                    ? String.format("(%.0f%%)", (seg.count() * 100.0) / total)
                    : "";

            Label lbl = new Label(seg.label() + " " + pct);
            lbl.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 11px; -fx-text-fill: #bac9cc;");

            row.getChildren().addAll(dot, lbl);
            legend.getChildren().add(row);
        }

        if (state.getSegments().isEmpty()) {
            Label empty = new Label("No semantic data yet");
            empty.getStyleClass().add("ar-empty-state");
            legend.getChildren().add(empty);
        }
    }

    private void bindState() {
        state.totalRefsProperty().addListener((obs, o, n) -> drawDonut());
    }

    private String formatCount(long v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000)     return String.format("%.1fK", v / 1_000.0);
        return String.valueOf(v);
    }
}
