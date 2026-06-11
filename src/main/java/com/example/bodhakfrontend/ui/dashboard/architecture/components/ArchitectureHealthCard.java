package com.example.bodhakfrontend.ui.dashboard.architecture.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ArchitectureHealthCard extends VBox {

    private final Label scoreLabel;
    private final Label riskLabel;

    public ArchitectureHealthCard(
            double score,
            String risk
    ) {

        getStyleClass().add("hero-card");

        setSpacing(10);
        setPadding(new Insets(24));

        Label title =
                new Label("Architecture Health");

        title.getStyleClass()
                .add("section-title");

        scoreLabel =
                new Label(
                        String.valueOf((int) score)
                );

        scoreLabel.getStyleClass()
                .add("hero-title");

        riskLabel =
                new Label(
                        "Risk Level: " + risk
                );

        riskLabel.getStyleClass()
                .add("hero-subtitle");

        getChildren().addAll(
                title,
                scoreLabel,
                riskLabel
        );
    }

    public void update(
            double score,
            String risk
    ) {

        scoreLabel.setText(
                String.valueOf((int) score)
        );

        riskLabel.setText(
                "Risk Level: " + risk
        );
    }
}