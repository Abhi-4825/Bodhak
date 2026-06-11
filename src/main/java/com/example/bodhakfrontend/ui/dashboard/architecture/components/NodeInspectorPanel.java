package com.example.bodhakfrontend.ui.dashboard.architecture.components;

import com.example.bodhakfrontend.core.model.namespace.NamespaceInfo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class NodeInspectorPanel extends VBox {

    private final Label idLabel;
    private final Label typeLabel;
    private final Label fanInValue;
    private final Label fanOutValue;
    private final VBox findingsContainer;

    public NodeInspectorPanel() {
        setSpacing(20);
        setPadding(new Insets(20));
        setPrefWidth(320);
        setMinWidth(320);
        setStyle("-fx-background-color: #0e1415; -fx-background-radius: 8; -fx-border-color: #1a2122; -fx-border-radius: 8; -fx-border-width: 1;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Node Inspector");
        title.setStyle("-fx-text-fill: #dde4e5; -fx-font-size: 16px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label icon = new Label("ⓘ");
        icon.setStyle("-fx-text-fill: #4bf6ff; -fx-font-size: 16px;");
        header.getChildren().addAll(title, spacer, icon);

        // ID Row
        HBox idRow = createInfoRow("ID", "—");
        idLabel = (Label) idRow.getChildren().get(2);

        // Type Row
        HBox typeRow = createInfoRow("TYPE", "Namespace");
        typeLabel = (Label) typeRow.getChildren().get(2);

        // Fan-in / Fan-out Cards
        HBox fanRow = new HBox(12);
        VBox fanInCard = createSquareCard("FAN-IN", "0", "Low coupling", "#849494");
        fanInValue = (Label) fanInCard.getChildren().get(1);
        VBox fanOutCard = createSquareCard("FAN-OUT", "0", "High volatility", "#ff8a80");
        fanOutValue = (Label) fanOutCard.getChildren().get(1);
        HBox.setHgrow(fanInCard, Priority.ALWAYS);
        HBox.setHgrow(fanOutCard, Priority.ALWAYS);
        fanRow.getChildren().addAll(fanInCard, fanOutCard);

        // Architectural Findings
        Label findingsTitle = new Label("ARCHITECTURAL FINDINGS");
        findingsTitle.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849494; -fx-padding: 10 0 0 0;");

        findingsContainer = new VBox(10);
        
        // Refactoring Button
        Button refactorBtn = new Button("GENERATE REFACTORING PLAN");
        refactorBtn.setStyle("-fx-background-color: #2f3637; -fx-text-fill: #dde4e5; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6;");
        refactorBtn.setMaxWidth(Double.MAX_VALUE);
        refactorBtn.setOnMouseEntered(e -> refactorBtn.setStyle("-fx-background-color: #4bf6ff; -fx-text-fill: #010409; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6;"));
        refactorBtn.setOnMouseExited(e -> refactorBtn.setStyle("-fx-background-color: #2f3637; -fx-text-fill: #dde4e5; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6;"));

        getChildren().addAll(header, idRow, typeRow, fanRow, findingsTitle, findingsContainer, refactorBtn);
    }

    public void update(NamespaceInfo info) {
        if (info == null) {
            idLabel.setText("None selected");
            fanInValue.setText("0");
            fanOutValue.setText("0");
            findingsContainer.getChildren().clear();
            return;
        }

        idLabel.setText(info.getNamespaceName() != null && !info.getNamespaceName().isBlank() ? info.getNamespaceName() : "None selected");
        fanInValue.setText(String.valueOf(info.getFanIn()));
        fanOutValue.setText(String.valueOf(info.getFanOut()));

        findingsContainer.getChildren().clear();

        for (String finding : info.getFindings()) {
            String color = finding.contains("CYCLE") || finding.contains("UNSTABLE") ? "#ff8a80" : "#ffd54f";
            findingsContainer.getChildren().add(createFindingCard(finding, "Identified during architectural analysis.", color));
        }

        if (info.getInstabilityScore() > 0) {
            findingsContainer.getChildren().add(createFindingCard("Instability Score", String.format("%.2f", info.getInstabilityScore()), "#4bf6ff"));
        }
        
        if (info.getRiskScore() > 0) {
            findingsContainer.getChildren().add(createFindingCard("Risk Score", String.format("%.0f/100", info.getRiskScore()), "#849494"));
        }

        if (findingsContainer.getChildren().isEmpty()) {
            Label noFindings = new Label("No critical findings.");
            noFindings.setStyle("-fx-text-fill: #566465; -fx-font-size: 12px;");
            findingsContainer.getChildren().add(noFindings);
        }
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #1a2122; -fx-background-radius: 6;");

        Label l = new Label(label);
        l.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-text-fill: #849494;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label v = new Label(value);
        v.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private VBox createSquareCard(String title, String value, String subtitle, String subColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #1a2122; -fx-background-radius: 6;");

        Label t = new Label(title);
        t.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849494;");

        Label v = new Label(value);
        v.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 24px; -fx-text-fill: #dde4e5;");

        Label s = new Label("~ " + subtitle);
        s.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 9px; -fx-text-fill: " + subColor + ";");

        card.getChildren().addAll(t, v, s);
        return card;
    }

    private VBox createFindingCard(String title, String desc, String color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 12, 12, 16));
        card.setStyle(String.format("-fx-background-color: #1a2122; -fx-background-radius: 6; -fx-border-color: transparent transparent transparent %s; -fx-border-width: 0 0 0 3; -fx-border-radius: 6;", color));

        Label t = new Label(title);
        t.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 11px; -fx-text-fill: #849494;");
        d.setWrapText(true);

        card.getChildren().addAll(t, d);
        return card;
    }
}
