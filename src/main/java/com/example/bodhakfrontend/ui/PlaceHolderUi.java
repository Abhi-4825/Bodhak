package com.example.bodhakfrontend.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class PlaceHolderUi {
    public VBox createEmptyState() {
        Label title = new Label("Project Analyser");
        title.getStyleClass().addAll("label", "empty-state-title");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        Label subtitle = new Label("Java Architecture & Dependency Analysis Tool");
        subtitle.getStyleClass().addAll("label", "empty-state-subtitle");
        subtitle.setStyle("-fx-font-size: 14px; -fx-opacity: 0.7; -fx-padding: 0 0 24 0;");

        VBox startSection = createSection("Start", new String[]{
                "Select a Java project folder from the top bar",
                "Click 'Analyze' to scan the project files",
                "Explore architecture and optimizations in the right panel"
        });

        VBox recentSection = createSection("Tips", new String[]{
                "Double-click files in the explorer to open them",
                "Use the 'AST' button to view file syntax trees",
                "Checkout 'Overview' for class complexities and dependencies"
        });

        HBox columns = new HBox(40, startSection, recentSection);
        columns.setAlignment(Pos.CENTER);

        VBox container = new VBox(0, title, subtitle, columns);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));

        return container;
    }

    private VBox createSection(String headerText, String[] items) {
        Label header = new Label(headerText);
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 12 0;");

        VBox section = new VBox(8);
        section.getChildren().add(header);

        for (String itemText : items) {
            Label item = new Label("• " + itemText);
            item.setStyle("-fx-font-size: 13.5px; -fx-opacity: 0.85;");
            section.getChildren().add(item);
        }

        section.setAlignment(Pos.TOP_LEFT);
        return section;
    }
}
