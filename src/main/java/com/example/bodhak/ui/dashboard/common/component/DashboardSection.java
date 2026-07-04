package com.example.bodhak.ui.dashboard.common.component;



import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardSection
        extends VBox {

    private final VBox content;

    public DashboardSection(
            String title
    ) {

        getStyleClass()
                .add("card");

        setSpacing(12);

        setPadding(
                new Insets(16)
        );

        Label heading =
                new Label(title);

        heading.getStyleClass()
                .add("card-title");

        content = new VBox(10);

        getChildren().addAll(
                heading,
                content
        );
    }

    public VBox content() {
        return content;
    }

    public void add(Node node) {
        content.getChildren().add(node);
    }
}
