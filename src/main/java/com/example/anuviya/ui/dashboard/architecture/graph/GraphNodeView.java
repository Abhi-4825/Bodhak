package com.example.anuviya.ui.dashboard.architecture.graph;



import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class GraphNodeView extends StackPane {
    private Runnable selectionAction;

    private final String entityName;

    public GraphNodeView(
            String entityName
    ) {

        this.entityName = entityName;

        Circle circle =
                new Circle(28);

        circle.setStyle("""
            -fx-fill:#0C151B;
            -fx-stroke:#4bf6ff;
            -fx-stroke-width:1.5;
        """);

        Label label =
                new Label(
                        shorten(entityName)
                );

        label.setStyle("""
            -fx-text-fill:#dde4e5;
            -fx-font-size:10px;
        """);

        setAlignment(Pos.CENTER);

        getChildren().addAll(
                circle,
                label
        );
        setOnMouseClicked(e -> {

            if (selectionAction != null) {
                selectionAction.run();
            }
        });
    }

    public String getEntityName() {
        return entityName;
    }

    private String shorten(
            String fullName
    ) {

        int idx =
                fullName.lastIndexOf('.');

        if (idx < 0) {
            return fullName;
        }

        return fullName.substring(idx + 1);
    }
    public void setSelectionAction(
            Runnable selectionAction
    ) {
        this.selectionAction = selectionAction;
    }
}
