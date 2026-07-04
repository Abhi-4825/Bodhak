package com.example.bodhak.ui.dashboard.common.component;



import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MetricCard extends VBox {

    private final Label valueLabel;
    private final Label titleLabel;

    public MetricCard(
            String title,
            String value
    ) {

        getStyleClass().add("metric-box");

        setSpacing(6);
        setPadding(new Insets(12));

        valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");

        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        getChildren().addAll(
                valueLabel,
                titleLabel
        );
    }

    public void updateValue(
            String value
    ) {
        valueLabel.setText(value);
    }
}
