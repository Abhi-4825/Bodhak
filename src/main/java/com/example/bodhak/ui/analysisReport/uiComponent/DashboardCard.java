package com.example.bodhak.ui.analysisReport.uiComponent;


import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DashboardCard extends VBox {

    private final DashboardHeader header;

    private final StackPane content =
            new StackPane();

    public DashboardCard(
            String title,
            Node icon
    ) {

        getStyleClass().add("dashboard-card");

        header = new DashboardHeader(title, icon);

        content.getStyleClass().add("dashboard-card-content");

        getChildren().addAll(
                header,
                content
        );
    }

    public void setContent(Node node) {

        content.getChildren().setAll(node);

    }

    public StackPane getContentContainer() {
        return content;
    }

    public DashboardHeader getHeader() {
        return header;
    }

}