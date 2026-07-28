package com.example.anuviya.ui.analysisReport.classification;

import com.example.anuviya.ui.analysisReport.state.ProjectClassificationState;
import com.example.anuviya.ui.analysisReport.state.ProjectClassificationState.ClassificationEntry;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Project Classification Card — Row 2, left panel.
 *
 * Shows the primary project type with confidence badge and a list of
 * detected type progress bars. Hides itself when no classification data
 * is available so the Framework Detection card expands to fill the row.
 *
 * Contains NO calculations — all data comes from ProjectClassificationState.
 */
public class ProjectClassificationCard extends VBox {

    private final ProjectClassificationState state;
    private final VBox barsContainer = new VBox(14);

    // Accent colors cycled per bar
    private static final String[] BAR_COLORS = {
            "#00daf3", "#bbc8d8", "#fec931", "#56d69b", "#9b7af0"
    };
    private static final String[] BAR_GLOW = {
            "rgba(0,218,243,0.5)", "rgba(187,200,216,0.3)", "rgba(254,201,49,0.4)",
            "rgba(86,214,155,0.4)", "rgba(155,122,240,0.4)"
    };

    // Icons shown at the bottom of the card
    private static final String[][] BOTTOM_ICONS = {
            {"router", "REST"},
            {"lan", "Layered"},
            {"extension", "Plugin"}
    };

    public ProjectClassificationCard(ProjectClassificationState state) {
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
        VBox.setVgrow(this, Priority.ALWAYS);

        // ── Header ─────────────────────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Project Classification");
        title.getStyleClass().add("ar-section-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label badge = new Label("95% CONFIDENCE");
        badge.getStyleClass().add("ar-badge");
        badge.textProperty().bind(
                state.primaryConfidenceProperty().map(c -> c + " CONFIDENCE")
        );
        badge.visibleProperty().bind(
                state.primaryClassificationProperty().isNotEqualTo("Unknown")
        );
        badge.managedProperty().bind(badge.visibleProperty());

        header.getChildren().addAll(title, badge);

        // ── Progress bars area ─────────────────────────────────────
        barsContainer.setPadding(new Insets(22, 0, 0, 0));

        // ── Bottom icon row ─────────────────────────────────────────
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.getStyleClass().add("ar-divider");
        divider.setStyle("-fx-opacity: 0.5;");
        VBox.setMargin(divider, new Insets(18, 0, 14, 0));

        HBox iconRow = buildIconRow();
        iconRow.setOpacity(0.55);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(header, barsContainer, spacer, divider);
    }

    private void bindState() {
        // Rebuild bars whenever the list changes
        state.getDetectedTypes().addListener((ListChangeListener<ClassificationEntry>) c -> rebuildBars());
        rebuildBars();

        // Show/hide this entire card
        visibleProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> !state.getDetectedTypes().isEmpty(),
                        state.getDetectedTypes()
                )
        );
        managedProperty().bind(visibleProperty());
    }

    private void rebuildBars() {
        barsContainer.getChildren().clear();
        var types = state.getDetectedTypes();
        for (int i = 0; i < types.size(); i++) {
            ClassificationEntry entry = types.get(i);
            if (entry.confidence() <= 0) continue;
            String color = BAR_COLORS[i % BAR_COLORS.length];
            String glow  = BAR_GLOW[i % BAR_GLOW.length];
            boolean isTop = (i == 0);
            barsContainer.getChildren().add(buildBarRow(entry, color, glow, isTop));
        }
    }

    private VBox buildBarRow(ClassificationEntry entry, String color, String glow, boolean neonGlow) {
        VBox row = new VBox(6);

        // Label row
        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(entry.name());
        name.getStyleClass().add("ar-body-sm");
        name.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        name.setMinWidth(0);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);

        Label pct = new Label(String.format("%.0f%%", entry.confidence() * 100));
        pct.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; " +
                     "-fx-font-weight: 700; -fx-text-fill: " + color + ";");

        labelRow.getChildren().addAll(name, pct);

        // Progress track + fill
        StackPane track = new StackPane();
        track.setMaxWidth(Double.MAX_VALUE);
        track.setPrefHeight(5);
        track.setMaxHeight(5);
        track.setStyle("-fx-background-color: rgba(59,73,76,0.35); -fx-background-radius: 4;");

        Region fill = new Region();
        fill.setPrefHeight(5);
        String glow_ = neonGlow
                ? "-fx-effect: dropshadow(gaussian, " + glow + ", 6, 0, 0, 0);"
                : "";
        fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4; " + glow_);

        // Bind fill width to track width * confidence
        track.widthProperty().addListener((obs, old, w) ->
                fill.setPrefWidth(w.doubleValue() * Math.min(entry.confidence(), 1.0))
        );
        fill.setPrefWidth(0);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        track.getChildren().addAll(fill);

        row.getChildren().addAll(labelRow, track);
        return row;
    }

    private HBox buildIconRow() {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER);

        for (String[] iconDef : BOTTOM_ICONS) {
            VBox item = new VBox(4);
            item.setAlignment(Pos.CENTER);
            HBox.setHgrow(item, Priority.ALWAYS);

            Label icon = new Label(iconDef[0]);
            icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; " +
                          "-fx-font-size: 20px; -fx-text-fill: #00daf3;");

            Label text = new Label(iconDef[1]);
            text.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; " +
                          "-fx-font-weight: 700; -fx-text-fill: #849494; " +
                          "-fx-letter-spacing: 0.15em;");

            item.getChildren().addAll(icon, text);
            row.getChildren().add(item);
        }
        return row;
    }
}
