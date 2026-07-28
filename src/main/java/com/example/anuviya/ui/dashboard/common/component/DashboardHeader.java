package com.example.anuviya.ui.dashboard.common.component;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardHeader
        extends VBox {

    public DashboardHeader(
            String title,
            String subtitle
    ) {

        setSpacing(6);

        Label titleLabel =
                new Label(title);

        titleLabel.getStyleClass()
                .add("hero-title");

        Label subtitleLabel =
                new Label(subtitle);

        subtitleLabel.getStyleClass()
                .add("hero-subtitle");

        getChildren().addAll(
                titleLabel,
                subtitleLabel
        );
    }
}
