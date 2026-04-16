package com.example.bodhakfrontend.FrontEnd.MainScreen;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.util.function.Consumer;

public class HomeScreen {

    // ================= TOP BAR =================
    public Node createTopBar(Consumer<Button> onSelectFolder) {
        HBox root = new HBox(20);
        root.getStyleClass().add("top-bar");
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(8, 16, 8, 16));
        Label logo = new Label("Bodhak");
        logo.getStyleClass().add("logo");
        Button selectBtn = new Button("SELECT FOLDER");
        selectBtn.getStyleClass().add("btn-primary");
        onSelectFolder.accept(selectBtn);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().addAll(logo, selectBtn, spacer);
        return root;
    }

    // ================= SIDEBAR =================
    public Node createSidebar(TreeView<File> tree) {

        VBox root = new VBox(10);
        root.getStyleClass().add("left-panel");
        root.setPrefWidth(220);
        root.setPadding(new Insets(16));

        Label title = new Label("PROJECT EXPLORER");
        title.getStyleClass().add("section-title");

        VBox.setVgrow(tree, Priority.ALWAYS);
        tree.getStyleClass().add("tree-view");

        root.getChildren().addAll(title, tree);
        return root;
    }

    public Node createBottomBar(ProgressBar progress, Label status) {

        BorderPane root = new BorderPane(); // ✅ NOT StackPane
        root.getStyleClass().add("bottom-bar");

        // ================= PROGRESS =================
        progress.getStyleClass().add("bottom-progress");


        // Set progress bar to half the length of the bottom bar
        progress.prefWidthProperty().bind(root.widthProperty().divide(2));
        progress.setMaxWidth(Region.USE_PREF_SIZE);
        progress.setPrefHeight(3);

        HBox progressBox = new HBox(progress);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setPadding(new Insets(0, 0, 0, 16));
        progress.setPrefHeight(3);

        // ================= STATUS =================
        status.getStyleClass().add("bottom-status");

        HBox statusBox = new HBox(status);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.setPadding(new Insets(0, 12, 0, 0));

        // ================= LAYOUT =================
        root.setCenter(progressBox);
        root.setRight(statusBox);   // text on right

        return root;
    }



}



