package com.example.anuviya.ui.dashboard.architecture.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MetricCard extends VBox {

    public MetricCard(
            String title,
            String value,
            String color
    ) {

        getStyleClass().add("metric-card");

        setSpacing(6);
        setPadding(new Insets(16));

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setStyle("-fx-text-fill:" + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        getChildren().addAll(
                valueLabel,
                titleLabel
        );
    }
}
