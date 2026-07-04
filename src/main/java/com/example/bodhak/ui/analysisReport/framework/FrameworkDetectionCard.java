package com.example.bodhak.ui.analysisReport.framework;

import com.example.bodhak.ui.analysisReport.state.FrameworkDetectionState;
import com.example.bodhak.ui.analysisReport.state.FrameworkDetectionState.FrameworkEntry;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Framework Detection Card — Row 2, right panel.
 *
 * Displays detected frameworks in a 2-column grid (or 1-column if only 1 detected).
 * Hides itself when no frameworks detected so the Classification card expands.
 *
 * Contains NO calculations — all data comes from FrameworkDetectionState.
 */
public class FrameworkDetectionCard extends VBox {

    private final FrameworkDetectionState state;
    private final GridPane grid = new GridPane();

    // Colors assigned by confidence tier
    private static final String COLOR_HIGH   = "#56d69b"; // green
    private static final String COLOR_MED    = "#00daf3"; // cyan
    private static final String COLOR_LOW    = "#ff9956"; // orange
    private static final String COLOR_OTHER  = "#bbc8d8"; // secondary

    private static final String CSS_HIGH   = "ar-fw-icon-green";
    private static final String CSS_MED    = "ar-fw-icon-cyan";
    private static final String CSS_LOW    = "ar-fw-icon-orange";
    private static final String CSS_OTHER  = "ar-fw-icon-secondary";

    public FrameworkDetectionCard(FrameworkDetectionState state) {
        this.state = state;
        initialise();
        bindState();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setPadding(new Insets(22));
        setSpacing(0);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        // ── Header ─────────────────────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(header, new Insets(0, 0, 20, 0));

        Label title = new Label("Framework Detection");
        title.getStyleClass().add("ar-section-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label viewAll = new Label("VIEW ALL →");
        viewAll.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 10px; " +
                         "-fx-font-weight: 700; -fx-text-fill: #00daf3; " +
                         "-fx-cursor: hand;");
        viewAll.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> "VIEW ALL " + state.getDetectedFrameworks().size() + " →",
                        state.getDetectedFrameworks()
                )
        );

        header.getChildren().addAll(title, viewAll);

        // ── Grid ───────────────────────────────────────────────────
        grid.setHgap(28);
        grid.setVgap(18);
        grid.setMaxWidth(Double.MAX_VALUE);

        // Make both columns share width equally
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        getChildren().addAll(header, grid);
    }

    private void bindState() {
        state.getDetectedFrameworks().addListener(
                (ListChangeListener<FrameworkEntry>) c -> rebuildGrid()
        );
        rebuildGrid();

        // Show/hide the entire card
        visibleProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> !state.getDetectedFrameworks().isEmpty(),
                        state.getDetectedFrameworks()
                )
        );
        managedProperty().bind(visibleProperty());
    }

    private void rebuildGrid() {
        grid.getChildren().clear();
        grid.getRowConstraints().clear();

        List<FrameworkEntry> entries = state.getDetectedFrameworks();
        boolean singleCol = entries.size() == 1;

        // If single column: use full width for col 0 only
        if (singleCol) {
            grid.getColumnConstraints().clear();
            ColumnConstraints full = new ColumnConstraints();
            full.setHgrow(Priority.ALWAYS);
            full.setPercentWidth(100);
            grid.getColumnConstraints().add(full);
        } else if (grid.getColumnConstraints().size() != 2) {
            grid.getColumnConstraints().clear();
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setHgrow(Priority.ALWAYS);
            c1.setPercentWidth(50);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setHgrow(Priority.ALWAYS);
            c2.setPercentWidth(50);
            grid.getColumnConstraints().addAll(c1, c2);
        }

        int cols = singleCol ? 1 : 2;
        for (int i = 0; i < entries.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            grid.add(buildFrameworkRow(entries.get(i)), col, row);
        }
    }

    private HBox buildFrameworkRow(FrameworkEntry entry) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // Colored icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(32, 32);
        iconCircle.setMinSize(32, 32);
        iconCircle.setMaxSize(32, 32);
        iconCircle.getStyleClass().add(iconCssClass(entry.confidence()));

        Label iconLabel = new Label(materialIconFor(entry.name()));
        iconLabel.setStyle("-fx-font-family: 'Material Symbols Outlined'; " +
                           "-fx-font-size: 16px; -fx-text-fill: " + barColor(entry.confidence()) + ";");
        iconCircle.getChildren().add(iconLabel);

        // Name + bar
        VBox details = new VBox(4);
        HBox.setHgrow(details, Priority.ALWAYS);

        HBox nameRow = new HBox();
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(entry.name());
        name.getStyleClass().add("ar-body-sm");
        name.setWrapText(false);
        name.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        name.setMinWidth(0);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);

        String color = barColor(entry.confidence());
        Label pct = new Label(String.format("%.0f%%", entry.confidence() * 100));
        pct.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; " +
                     "-fx-font-weight: 700; -fx-text-fill: " + color + ";");

        nameRow.getChildren().addAll(name, pct);

        // Progress bar — 1px thin
        StackPane track = new StackPane();
        track.setMaxWidth(Double.MAX_VALUE);
        track.setPrefHeight(3);
        track.setMaxHeight(3);
        track.setStyle("-fx-background-color: rgba(59,73,76,0.35); -fx-background-radius: 3;");

        Region fill = new Region();
        fill.setPrefHeight(3);
        fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3;");
        double conf = Math.min(entry.confidence(), 1.0);
        track.widthProperty().addListener((obs, old, w) ->
                fill.setPrefWidth(w.doubleValue() * conf));
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        track.getChildren().add(fill);

        details.getChildren().addAll(nameRow, track);
        row.getChildren().addAll(iconCircle, details);
        return row;
    }

    // ── Helpers ────────────────────────────────────────────────────

    private String barColor(double conf) {
        if (conf >= 0.85) return COLOR_HIGH;
        if (conf >= 0.65) return COLOR_MED;
        if (conf >= 0.45) return COLOR_LOW;
        return COLOR_OTHER;
    }

    private String iconCssClass(double conf) {
        if (conf >= 0.85) return CSS_HIGH;
        if (conf >= 0.65) return CSS_MED;
        if (conf >= 0.45) return CSS_LOW;
        return CSS_OTHER;
    }

    private String materialIconFor(String name) {
        if (name == null) return "extension";
        String lower = name.toLowerCase();
        if (lower.contains("spring") && lower.contains("boot"))   return "bolt";
        if (lower.contains("spring") && lower.contains("mvc"))    return "web";
        if (lower.contains("spring") && lower.contains("data"))   return "database";
        if (lower.contains("spring"))  return "bolt";
        if (lower.contains("hibernate") || lower.contains("jpa")) return "hub";
        if (lower.contains("lombok"))   return "construction";
        if (lower.contains("junit") || lower.contains("test"))    return "bug_report";
        if (lower.contains("react"))    return "electric_bolt";
        if (lower.contains("django") || lower.contains("flask"))  return "code";
        if (lower.contains("maven") || lower.contains("gradle"))  return "build";
        return "extension";
    }
}
