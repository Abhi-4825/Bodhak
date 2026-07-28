package com.example.anuviya.ui.workspace;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class EditorWorkspace extends BorderPane {

    private final TabPane codeTabPane;
    private final HBox editorBottom;
    private final Button astBtn;
    private final Button overviewBtn;

    public EditorWorkspace(TabPane codeTabPane, Node centerPlaceholder) {
        this.codeTabPane = codeTabPane;

        // Editor bottom buttons
        astBtn = new Button("AST");
        astBtn.getStyleClass().addAll("btn-secondary", "editor-bottom-btn");
        
        overviewBtn = new Button("OVERVIEW");
        overviewBtn.getStyleClass().addAll("btn-primary", "editor-bottom-btn");

        editorBottom = new HBox(8);
        editorBottom.getStyleClass().add("editor-bottom-bar");
        editorBottom.getChildren().addAll(astBtn, overviewBtn);
        editorBottom.setPadding(new Insets(6));
        editorBottom.setAlignment(Pos.CENTER_RIGHT);

        // Center stack
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(centerPlaceholder, codeTabPane);
        
        this.setCenter(centerStack);
        this.setBottom(editorBottom);
    }

    public Button getAstBtn() {
        return astBtn;
    }

    public Button getOverviewBtn() {
        return overviewBtn;
    }

    public TabPane getCodeTabPane() {
        return codeTabPane;
    }
}
