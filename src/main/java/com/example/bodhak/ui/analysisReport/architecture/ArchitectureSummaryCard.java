package com.example.bodhak.ui.analysisReport.architecture;

import com.example.bodhak.ui.analysisReport.state.ArchitectureSummaryState;
import com.example.bodhak.ui.analysisReport.uiComponent.DashboardCard;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ArchitectureSummaryCard extends VBox {

    private final ArchitectureSummaryState state;
    private final DashboardCard card;

    public ArchitectureSummaryCard(ArchitectureSummaryState state) {
        this.state = state;
        this.card = new DashboardCard("Architecture Overview", new Label("🏛️"));
        initialise();
    }

    private void initialise() {
        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-padding: 30;");

        statsBox.getChildren().addAll(
                createStatBox("Nodes", String.valueOf(state.getNodeCount())),
                createStatBox("Edges", String.valueOf(state.getEdgeCount())),
                createStatBox("Max Depth", String.valueOf(state.getMaxDepth()))
        );

        card.setContent(statsBox);
    }

    private VBox createStatBox(String label, String value) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        
        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e6f1f3;");
        
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8ea2a8;");
        
        box.getChildren().addAll(valLabel, nameLabel);
        return box;
    }
}
