package com.example.bodhakfrontend.ui.main.MainScreen;

import com.example.bodhakfrontend.core.model.ast.GenericAstNode;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;

/**
 * A completely generic AST viewer. It knows nothing about JavaParser or Tree-sitter.
 * It strictly renders a GenericAstNode tree exactly as provided by the language plugin.
 */
public class ASTViewer {

    public ASTViewer() {}

    public void show(File file, GenericAstNode rootNode) {
        if (rootNode == null) {
            System.err.println("Could not generate AST for " + file.getName());
            return;
        }

        TreeItem<GenericAstNode> root = buildTreeView(rootNode);
        root.setExpanded(true);

        TreeView<GenericAstNode> tree = new TreeView<>(root);
        VBox.setVgrow(tree, Priority.ALWAYS);

        CodeArea codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.getStyleClass().add("code-area-preview");

        try {
            codeArea.replaceText(Files.readString(file.toPath()));
        } catch (Exception ignored) {}

        tree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                GenericAstNode node = newVal.getValue();
                if (node.getBeginLine() >= 0 && node.getEndLine() >= 0) {
                    try {
                        codeArea.selectRange(node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn());
                        codeArea.showParagraphAtCenter(node.getBeginLine());
                    } catch (Exception e) {
                        // Ignore out of bounds errors if parsing was slightly off
                    }
                }
            }
        });

        SplitPane split = new SplitPane(tree, codeArea);
        split.setDividerPositions(0.35);

        Scene scene = new Scene(split, 1000, 700);
        String css = getClass().getResource("/styles/styles.css") != null
                ? getClass().getResource("/styles/styles.css").toExternalForm()
                : "";
        if (!css.isEmpty()) scene.getStylesheets().add(css);

        Stage stage = new Stage();
        stage.setTitle("AST Inspector - " + file.getName());
        stage.setScene(scene);
        stage.show();
    }

    private TreeItem<GenericAstNode> buildTreeView(GenericAstNode node) {
        TreeItem<GenericAstNode> item = new TreeItem<>(node);
        
        // Auto-expand the very root nodes for convenience
        if (node.getKind().equals("file") || node.getKind().equals("class")) {
            item.setExpanded(true);
        }

        for (GenericAstNode child : node.getChildren()) {
            TreeItem<GenericAstNode> childItem = buildTreeView(child);
            item.getChildren().add(childItem);
        }

        return item;
    }
}