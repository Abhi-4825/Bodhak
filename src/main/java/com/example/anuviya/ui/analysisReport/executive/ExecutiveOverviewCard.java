package com.example.anuviya.ui.analysisReport.executive;

import com.example.anuviya.ui.analysisReport.state.ExecutiveSummaryState;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Executive Overview Banner — Row 1 of the Analysis Report dashboard.
 *
 * Renders a horizontal row of glassmorphism metric tiles, one per key metric.
 * Tiles dynamically size to their content and are hidden/shown based on whether
 * their backing data is available. No fixed widths anywhere.
 *
 * Contains NO calculations — all values come from ExecutiveSummaryState.
 */
public final class ExecutiveOverviewCard extends StackPane {

    private final ExecutiveSummaryState state;
    
    // Cache the tiles so we don't recreate them and break bindings on layout mode switch
    private ExecMetricTile unitTile;
    private ExecMetricTile nsTile;
    private ExecMetricTile entityTile;
    private ExecMetricTile memberTile;
    private ExecMetricTile locTile;
    private ExecMetricTile healthTile;
    private ExecMetricTile langTile;

    public ExecutiveOverviewCard(ExecutiveSummaryState state) {
        this.state = state;
        initialise();
        setLayoutMode(com.example.anuviya.ui.analysisReport.DashboardGrid.LayoutMode.WIDE);
    }

    private void initialise() {
        setPadding(new Insets(0));

        // ── Tile 1: Compilation Units ──────────────────────────────
        unitTile = new ExecMetricTile(
                "COMP. UNITS",
                null,
                "ar-accent-bar-cyan",
                "ar-exec-tile-icon"
        );
        unitTile.setImageIcon("/icons/AnalysisReportIcons/CompilationUnit.png",28);
        unitTile.valueProperty().bind(
                Bindings.createStringBinding(
                        () -> formatInt(state.compilationUnitsProperty().get()),
                        state.compilationUnitsProperty()
                )
        );
        bindVisibility(unitTile, state.compilationUnitsProperty());

        // ── Tile 2: Namespaces ─────────────────────────────────────
        nsTile = new ExecMetricTile(
                "NAMESPACES",
                null,
                "ar-accent-bar-blue",
                "ar-exec-tile-icon-blue"
        );
        nsTile.setImageIcon("/icons/AnalysisReportIcons/pkg.png",28);
        nsTile.valueProperty().bind(
                Bindings.createStringBinding(
                        () -> formatInt(state.namespacesProperty().get()),
                        state.namespacesProperty()
                )
        );
        bindVisibility(nsTile, state.namespacesProperty());

        // ── Tile 3: Entities ───────────────────────────────────────
        entityTile = new ExecMetricTile(
                "ENTITIES",
                null,
                "ar-accent-bar-amber",
                "ar-exec-tile-icon-amber"
        );
        entityTile.setImageIcon("/icons/AnalysisReportIcons/entity.png",28);
        entityTile.valueProperty().bind(
                Bindings.createStringBinding(
                        () -> formatInt(state.entitiesProperty().get()),
                        state.entitiesProperty()
                )
        );
        bindVisibility(entityTile, state.entitiesProperty());

        // ── Tile 4: Members ────────────────────────────────────────
        memberTile = new ExecMetricTile(
                "MEMBERS",
                null,
                "ar-accent-bar-teal",
                "ar-exec-tile-icon-teal"
        );
        memberTile.setImageIcon("/icons/AnalysisReportIcons/group.png",28);
        memberTile.valueProperty().bind(
                Bindings.createStringBinding(
                        () -> formatInt(state.membersProperty().get()),
                        state.membersProperty()
                )
        );
        bindVisibility(memberTile, state.membersProperty());

        // ── Tile 5: Lines of Code ──────────────────────────────────
        locTile = new ExecMetricTile(
                "LINES OF CODE",
                null,
                "ar-accent-bar-violet",
                "ar-exec-tile-icon-violet"
        );
        locTile.setImageIcon("/icons/AnalysisReportIcons/data-management.png",28);
        locTile.valueProperty().bind(
                Bindings.createStringBinding(
                        () -> formatLoc(state.linesOfCodeProperty().get()),
                        state.linesOfCodeProperty()
                )
        );
        bindVisibility(locTile, state.linesOfCodeProperty());

        // ── Tile 6: Health Index ───────────────────────────────────
        healthTile = buildHealthTile();

        // ── Tile 7: Language ───────────────────────────────────────
        langTile = buildLanguageTile();
    }

    /**
     * Rebuilds the layout container dynamically based on the layout mode.
     */
    public void setLayoutMode(com.example.anuviya.ui.analysisReport.DashboardGrid.LayoutMode mode) {
        getChildren().clear();

        if (mode == com.example.anuviya.ui.analysisReport.DashboardGrid.LayoutMode.WIDE) {
            HBox hbox = new HBox(10);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setFillHeight(true);
            hbox.getChildren().addAll(unitTile, nsTile, entityTile, memberTile, locTile, healthTile, langTile);
            for (Node child : hbox.getChildren()) {
                HBox.setHgrow(child, Priority.SOMETIMES);
            }
            getChildren().add(hbox);

        } else if (mode == com.example.anuviya.ui.analysisReport.DashboardGrid.LayoutMode.MEDIUM) {
            // 2-row layout: 4 tiles on top, 3 on bottom
            VBox vbox = new VBox(10);
            vbox.setFillWidth(true);

            HBox row1 = new HBox(10);
            row1.setAlignment(Pos.CENTER_LEFT);
            row1.getChildren().addAll(unitTile, nsTile, entityTile, memberTile);
            for (Node child : row1.getChildren()) {
                HBox.setHgrow(child, Priority.ALWAYS);
            }

            HBox row2 = new HBox(10);
            row2.setAlignment(Pos.CENTER_LEFT);
            row2.getChildren().addAll(locTile, healthTile, langTile);
            for (Node child : row2.getChildren()) {
                HBox.setHgrow(child, Priority.ALWAYS);
            }

            vbox.getChildren().addAll(row1, row2);
            getChildren().add(vbox);

        } else {
            // NARROW layout: 2-column grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setMaxWidth(Double.MAX_VALUE);

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHgrow(Priority.ALWAYS);
            col1.setPercentWidth(50);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);
            col2.setPercentWidth(50);
            grid.getColumnConstraints().addAll(col1, col2);

            grid.add(unitTile, 0, 0);
            grid.add(nsTile, 1, 0);
            grid.add(entityTile, 0, 1);
            grid.add(memberTile, 1, 1);
            grid.add(locTile, 0, 2);
            grid.add(healthTile, 1, 2);
            
            // Language tile spans full width on row 3 if visible
            grid.add(langTile, 0, 3, 2, 1);

            getChildren().add(grid);
        }
    }

    // ── Health tile with pulsing dot ───────────────────────────────
    private ExecMetricTile buildHealthTile() {
        ExecMetricTile tile = new ExecMetricTile(
                "HEALTH INDEX",
                null,   // icon provided manually below
                "ar-accent-bar-green",
                null
        );
        tile.setImageIcon("/icons/AnalysisReportIcons/cardiogram.png",28);

        tile.valueProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            double h = state.healthProperty().get();
                            return h < 0 ? "—" : String.format("%.1f%%", h);
                        },
                        state.healthProperty()
                )
        );
        tile.getValueLabel().getStyleClass().add("ar-value-primary");

        // Pulsing dot replaces the icon
        Region pulse = new Region();
        pulse.setPrefSize(8, 8);
        pulse.setMaxSize(8, 8);
        pulse.setStyle(
                "-fx-background-color: #00daf3; " +
                "-fx-background-radius: 999; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,218,243,0.8), 8, 0, 0, 0);"
        );
        animatePulse(pulse);
        tile.setCustomIcon(pulse);

        return tile;
    }

    // ── Language tile with subtitle ────────────────────────────────
    private ExecMetricTile buildLanguageTile() {
        ExecMetricTile tile = new ExecMetricTile(
                "LANGUAGE",
                null,
                "ar-accent-bar-cyan",
                "ar-exec-tile-icon"
        );
        tile.setImageIcon("/icons/AnalysisReportIcons/code.png",28);
        tile.valueProperty().bind(state.primaryLanguageProperty());

        // Subtitle: project size
        Label sizeLabel = new Label();
        sizeLabel.getStyleClass().add("ar-body-muted");
        sizeLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> state.projectSizeProperty().get() != null
                                ? state.projectSizeProperty().get().name()
                                : "",
                        state.projectSizeProperty()
                )
        );
        tile.setSubtitleNode(sizeLabel);

        // Hide if language unknown
        tile.visibleProperty().bind(
                state.primaryLanguageProperty().isNotEmpty()
                        .and(state.primaryLanguageProperty().isNotEqualTo("Unknown"))
        );
        tile.managedProperty().bind(tile.visibleProperty());

        return tile;
    }

    // ── Binding helpers ────────────────────────────────────────────
    private void bindVisibility(ExecMetricTile tile, javafx.beans.property.IntegerProperty prop) {
        tile.visibleProperty().bind(prop.greaterThan(0));
        tile.managedProperty().bind(tile.visibleProperty());
    }

    private void bindVisibility(ExecMetricTile tile, javafx.beans.property.LongProperty prop) {
        tile.visibleProperty().bind(prop.greaterThan(0));
        tile.managedProperty().bind(tile.visibleProperty());
    }

    // ── Formatters ─────────────────────────────────────────────────
    private String formatInt(int v) {
        if (v == 0) return "—";
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000)    return String.format("%,d", v);
        return String.valueOf(v);
    }

    private String formatLoc(long v) {
        if (v == 0) return "—";
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000)    return String.format("%.1fK", v / 1_000.0);
        return String.valueOf(v);
    }

    // ── Pulse animation ────────────────────────────────────────────
    private void animatePulse(Region dot) {
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(dot.opacityProperty(), 1.0),
                        new KeyValue(dot.scaleXProperty(), 0.95),
                        new KeyValue(dot.scaleYProperty(), 0.95)
                ),
                new KeyFrame(Duration.millis(700),
                        new KeyValue(dot.opacityProperty(), 0.4),
                        new KeyValue(dot.scaleXProperty(), 1.2),
                        new KeyValue(dot.scaleYProperty(), 1.2)
                ),
                new KeyFrame(Duration.millis(1400),
                        new KeyValue(dot.opacityProperty(), 1.0),
                        new KeyValue(dot.scaleXProperty(), 0.95),
                        new KeyValue(dot.scaleYProperty(), 0.95)
                )
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }
}
