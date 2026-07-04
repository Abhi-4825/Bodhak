package com.example.bodhak.ui.analysisReport.uiComponent;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;

/**
 * Responsive container used for arranging MetricTiles.
 *
 * Automatically wraps tiles as the window resizes.
 */
public final class MetricGrid extends FlowPane {

    public MetricGrid() {

        initialise();

    }

    private void initialise() {

        getStyleClass().add("metric-grid");

        setHgap(16);

        setVgap(16);

        setPadding(new Insets(20));

        setAlignment(Pos.TOP_LEFT);

        setPrefWrapLength(1200);

    }

    // ------------------------------------------------------------------------
    // Convenience API
    // ------------------------------------------------------------------------

    public void addTile(MetricTile tile) {

        getChildren().add(tile);

    }

    public void removeTile(MetricTile tile) {

        getChildren().remove(tile);

    }

    public void clearTiles() {

        getChildren().clear();

    }

    public void addTiles(MetricTile... tiles) {

        getChildren().addAll(tiles);

    }

}