package com.example.bodhakfrontend.ui.overviewButton;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class OverviewView {
    private final VBox root=new VBox(8);
    private final StackPane contentArea=new StackPane();
    private final ToggleButton depsBtn=new ToggleButton("Dependencies");
    private final ToggleButton methodBtn=new ToggleButton("Methods");
    private final ToggleButton healthBtn=new ToggleButton("Health");

    public OverviewView(
            Node dependenciesView,
            Node methodsView,
            Node healthView
    ) {
        buildToggleBar();
        buildContent(dependenciesView,methodsView,healthView);
    }
    public VBox getRoot() {
        return root;
    }

    private void buildToggleBar() {
        ToggleGroup  group=new ToggleGroup();
        depsBtn.setToggleGroup(group);
        methodBtn.setToggleGroup(group);
        healthBtn.setToggleGroup(group);
        depsBtn.setSelected(true);
        HBox hb=new HBox(6,depsBtn,methodBtn,healthBtn);
        hb.setPadding(new Insets(6));
        hb.setStyle("""
            -fx-background-color: #f4f4f4;
            -fx-border-color: #ddd;
            -fx-border-width: 0 0 1 0;
        """);
        root.getChildren().add(hb);
    }
    private void buildContent(
            Node dependenciesView,
            Node methodsView,
            Node healthView
    ){
        contentArea.getChildren().addAll(dependenciesView,methodsView,healthView);
        dependenciesView.setVisible(true);
        methodsView.setVisible(false);
        healthView.setVisible(false);
        depsBtn.setOnAction(e -> show(dependenciesView));
        methodBtn.setOnAction(e -> show(methodsView));
        healthBtn.setOnAction(e -> show(healthView));
        ScrollPane pane=new ScrollPane(contentArea);
        pane.setFitToWidth(true);
        pane.setFitToHeight(true);

        root.getChildren().add(pane);
    }
    private void show(Node node) {
        contentArea.getChildren().forEach(n -> {n.setVisible(false);});
        node.setVisible(true);
    }
}
