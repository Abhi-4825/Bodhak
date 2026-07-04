package com.example.bodhak.ui.dashboard.architecture.components;

import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureDashboardData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ArchitectureMetricRow extends HBox {

    public ArchitectureMetricRow(ArchitectureDashboardData data) {
        setSpacing(16);
        setPadding(new Insets(0, 0, 20, 0));

        getChildren().addAll(
                createCard("TOTAL NAMESPACES", String.valueOf(data.namespaces()), "#4bf6ff"),
                createCard("TOTAL PACKAGES", String.valueOf(data.namespaces()), "#4bf6ff"), // Assuming namespaces = packages here for display
                createCard("TOTAL CLASSES", String.valueOf(data.classes()), "#4bf6ff"),
                createCard("CIRCULAR CYCLES", String.valueOf(data.cycles()), data.cycles() > 0 ? "#ff8a80" : "#8bfd91"),
                createCard("RISK SCORE", (int) data.healthScore() + " / 100", data.healthScore() < 50 ? "#ffd54f" : "#8bfd91")
        );

        getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
    }

    private VBox createCard(String title, String value, String accentColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(String.format(
            "-fx-background-color: #161d1e; -fx-background-radius: 6; -fx-border-color: %s transparent transparent transparent; -fx-border-width: 0 0 0 3; -fx-border-color: transparent transparent transparent %s; -fx-border-radius: 6;",
            accentColor, accentColor
        ));

        Label t = new Label(title);
        t.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849494;");

        Label v = new Label(value);
        v.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 28px; -fx-text-fill: #dde4e5;");
        
        // Highlight value if risk or cycle
        if (title.contains("CYCLES") || title.contains("RISK")) {
            v.setStyle(v.getStyle() + " -fx-text-fill: " + accentColor + ";");
        }

        card.getChildren().addAll(t, v);
        return card;
    }
}
