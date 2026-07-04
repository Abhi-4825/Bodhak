package com.example.bodhak.ui.overviewButton;

import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.ui.OverviewContentFactory;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class ClassOverviewContainer {

    private final VBox root = new VBox(6);
    private final StackPane overviewHolder = new StackPane();

    public ClassOverviewContainer(
            File sourceFile,
            List<EntityInfo> classNames,
            OverviewContentFactory factory
    ) {
        ComboBox<EntityInfo> classSelector = new ComboBox<>();
        classSelector.getItems().addAll(classNames);
        classSelector.getSelectionModel().selectFirst();

        classSelector.setOnAction(e -> {
            EntityInfo cls = classSelector.getValue();
            overviewHolder.getChildren().setAll(
                    factory.build(cls)
            );
        });

        // initial load
        overviewHolder.getChildren().add(
                factory.build(classSelector.getValue())
        );
        VBox.setVgrow(overviewHolder, Priority.ALWAYS);

        root.getChildren().addAll(classSelector, overviewHolder);
    }

    public Node getRoot() {
        return root;
    }
}

