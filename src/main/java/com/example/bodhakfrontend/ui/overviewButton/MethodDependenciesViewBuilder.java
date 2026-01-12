package com.example.bodhakfrontend.ui.overviewButton;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class MethodDependenciesViewBuilder {

    public Node build(
            String methodName,
            List<String> dependencies
    ) {

        VBox root = new VBox(6);
        root.setPadding(new Insets(8));

        if (dependencies == null || dependencies.isEmpty()) {
            root.getChildren().add(
                    new Label("No method dependencies found.")
            );
            return new TitledPane("Method Dependencies", root);
        }

        for (String dep : dependencies) {
            root.getChildren().add(buildRow(dep));
            root.getChildren().add(new Separator());
        }

        TitledPane pane =
                new TitledPane(
                        "Method Dependencies (" + dependencies.size() + ")",
                        root
                );

        pane.setExpanded(true);
        return pane;
    }

    private Node buildRow(String methodName) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(4));

        boolean external = isExternal(methodName);

        Label icon = new Label(external ? "⚠" : "✔");
        Label name = new Label(methodName + "()");

        if (external) {
            name.getStyleClass().add("method-dependency-library");
        } else {
            name.getStyleClass().add("method-dependency-internal");
        }

        row.getChildren().addAll(icon, name);
        return row;
    }

    /**
     * Temporary heuristic.
     * Later this will use SymbolSolver.
     */
    private boolean isExternal(String methodName) {
        return Character.isUpperCase(methodName.charAt(0));
    }
}
