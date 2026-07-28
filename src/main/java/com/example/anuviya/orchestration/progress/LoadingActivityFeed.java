package com.example.anuviya.orchestration.progress;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LoadingActivityFeed extends VBox {

    public LoadingActivityFeed(LoadingProgressModel model) {
        setSpacing(6);
        setPadding(new Insets(12, 16, 12, 16));
        setAlignment(Pos.TOP_LEFT);
        setStyle("-fx-background-color: rgba(8, 15, 21, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-background-radius: 8;");
        setPrefHeight(180);

        model.getActivityFeed().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String msg : change.getAddedSubList()) {
                        Label row = new Label(msg);
                        row.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
                        getChildren().add(row);

                        FadeTransition fade = new FadeTransition(Duration.millis(250), row);
                        fade.setFromValue(0.0);
                        fade.setToValue(1.0);

                        TranslateTransition translate = new TranslateTransition(Duration.millis(250), row);
                        translate.setFromY(8);
                        translate.setToY(0);

                        fade.play();
                        translate.play();
                    }
                }
                if (change.wasRemoved()) {
                    int numRemoved = change.getRemovedSize();
                    if (getChildren().size() >= numRemoved) {
                        getChildren().remove(0, numRemoved);
                    }
                }
            }
        });
    }
}
