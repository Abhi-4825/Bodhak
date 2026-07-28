package com.example.anuviya.ui.analysisReport.ai;

import com.example.anuviya.ui.analysisReport.state.AiReadinessSummaryState;
import com.example.anuviya.ui.analysisReport.uiComponent.DashboardCard;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AiReadinessSummaryCard extends VBox {

    private final AiReadinessSummaryState state;
    private final DashboardCard card;

    public AiReadinessSummaryCard(AiReadinessSummaryState state) {
        this.state = state;
        this.card = new DashboardCard("AI Readiness Summary", new Label("🤖"));
        initialise();
    }

    private void initialise() {
        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-padding: 30;");

        VBox completeBox = createStatBox("Completeness", "#00e5ff");
        state.completenessProperty().addListener((obs, old, val) -> ((Label) completeBox.getChildren().get(0)).setText(String.format("%.0f%%", val.doubleValue() * 100)));

        VBox symbolBox = createStatBox("Resolvable Symbols", "#e6f1f3");
        state.resolvableSymbolsProperty().addListener((obs, old, val) -> ((Label) symbolBox.getChildren().get(0)).setText(val.toString()));

        VBox missingBox = createStatBox("Missing Evidence", "#ff5252");
        state.missingEvidenceProperty().addListener((obs, old, val) -> ((Label) missingBox.getChildren().get(0)).setText(val.toString()));

        // Initial values
        ((Label) completeBox.getChildren().get(0)).setText(String.format("%.0f%%", state.completenessProperty().get() * 100));
        ((Label) symbolBox.getChildren().get(0)).setText(String.valueOf(state.resolvableSymbolsProperty().get()));
        ((Label) missingBox.getChildren().get(0)).setText(String.valueOf(state.missingEvidenceProperty().get()));

        statsBox.getChildren().addAll(completeBox, symbolBox, missingBox);
        card.setContent(statsBox);
    }

    private VBox createStatBox(String label, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        
        Label valLabel = new Label("0");
        valLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8ea2a8;");
        
        box.getChildren().addAll(valLabel, nameLabel);
        return box;
    }
}
