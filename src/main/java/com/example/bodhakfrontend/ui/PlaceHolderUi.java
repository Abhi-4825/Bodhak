package com.example.bodhakfrontend.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;


public class PlaceHolderUi {
    public Node createCenterPlaceholder() {

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("placeholder-center");

        Label icon = new Label("</>");
        icon.getStyleClass().add("placeholder-icon");

        Label title = new Label("No file open");
        title.getStyleClass().add("placeholder-title");

        Label subtitle = new Label("Select a file from explorer to start");
        subtitle.getStyleClass().add("placeholder-subtitle");

        root.getChildren().addAll(icon, title, subtitle);

        return root;
    }

    public VBox createRightPlaceholder() {

        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("placeholder-right");

        Label icon = new Label("📊");
        icon.getStyleClass().add("placeholder-icon");

        Label title = new Label("No analysis yet");
        title.getStyleClass().add("placeholder-title");

        Label subtitle = new Label("Click 'Analyze' to view insights");
        subtitle.getStyleClass().add("placeholder-subtitle");

        root.getChildren().addAll(icon, title, subtitle);

        return root;
    }
}
