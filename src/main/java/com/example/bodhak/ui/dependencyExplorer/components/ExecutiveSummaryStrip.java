package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.ExecutiveSummaryState;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ExecutiveSummaryStrip extends HBox {

    public ExecutiveSummaryStrip(ExecutiveSummaryState state) {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        
        // 6 tiles in the horizontal strip
        getChildren().addAll(
                buildTile("ENTITIES", state.entityCountProperty().asString(), "Classes, interfaces, enums", "dd-stat-tile"),
                buildTile("SEMANTIC REFERENCES", state.semanticReferenceCountProperty().asString(), "All reference kinds", "dd-stat-tile"),
                buildTile("CIRCULAR DEPENDENCIES", state.circularDependenciesCountProperty().asString(), "In strongly connected components", "dd-stat-tile"),
                buildTile("AVG FAN-OUT", Bindings.format("%.1f", state.averageFanOutProperty()), "Per entity", "dd-stat-tile"),
                buildTile("MAX DEPTH", state.maxDepthProperty().asString(), "Dependency chain depth", "dd-stat-tile"),
                buildTile("HEALTH", state.healthRatingProperty(), "No critical issues", "dd-stat-tile")
        );

        // Make all tiles grow equally
        for (javafx.scene.Node node : getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
    }

    private VBox buildTile(String labelStr, javafx.beans.value.ObservableValue<String> valProp, String subtextStr, String styleClass) {
        VBox tile = new VBox(6);
        tile.getStyleClass().add(styleClass);
        tile.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelStr);
        label.getStyleClass().add("dd-stat-label");

        Label value = new Label();
        value.textProperty().bind(valProp);
        value.getStyleClass().add("dd-stat-value");
        
        // Custom color rules for status indicators
        if ("HEALTH".equals(labelStr)) {
            valProp.addListener((obs, old, val) -> {
                if ("Critical".equalsIgnoreCase(val)) {
                    value.setStyle("-fx-text-fill: #ff4b4b;");
                } else if ("Warning".equalsIgnoreCase(val)) {
                    value.setStyle("-fx-text-fill: #fec931;");
                } else {
                    value.setStyle("-fx-text-fill: #00e676;");
                }
            });
            // initial style
            if ("Critical".equalsIgnoreCase(valProp.getValue())) {
                value.setStyle("-fx-text-fill: #ff4b4b;");
            } else if ("Warning".equalsIgnoreCase(valProp.getValue())) {
                value.setStyle("-fx-text-fill: #fec931;");
            } else {
                value.setStyle("-fx-text-fill: #00e676;");
            }
        }

        Label subtext = new Label(subtextStr);
        subtext.setStyle("-fx-font-size: 10px; -fx-text-fill: #849396;");

        tile.getChildren().addAll(label, value, subtext);
        return tile;
    }
}
