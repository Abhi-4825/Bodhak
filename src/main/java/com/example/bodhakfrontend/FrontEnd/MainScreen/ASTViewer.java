package com.example.bodhakfrontend.FrontEnd.MainScreen;

import com.example.bodhakfrontend.Parser.AstLabelProvider;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.github.javaparser.ast.stmt.ReturnStmt;           // Usually shortened to 'Stmt'
import java.io.File;
import java.nio.file.Files;

public class ASTViewer {
    private final AstLabelProvider<Node> labelProvider;

    public ASTViewer(AstLabelProvider<Node> labelProvider) {
        this.labelProvider = labelProvider;
    }

    public void show(File file, CompilationUnit cu) {
        TreeItem<String> root = buildASTTree(cu);
        root.setExpanded(true); // Expand the root by default

        TreeView<String> tree = new TreeView<>(root);
        VBox.setVgrow(tree, Priority.ALWAYS);

        TextArea codeArea = new TextArea();
        codeArea.setEditable(false);
        codeArea.getStyleClass().add("code-area-preview");

        try {
            codeArea.setText(Files.readString(file.toPath()));
        } catch (Exception ignored) {}

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
    private TreeItem<String> buildASTTree(Node node) {
        // 1. Filter out absolute noise (same as before)
        if (
                node instanceof com.github.javaparser.ast.Modifier ||
                node instanceof com.github.javaparser.ast.type.Type) {
            return null;
        }

        // 2. FLATTENING LOGIC: If it's a BlockStmt, don't create a node for it.
        // Instead, return its children directly to the caller.
        if (node instanceof com.github.javaparser.ast.stmt.BlockStmt) {
            // We return a "Dummy" item or handle it in the loop below.
            // For simplicity, let's just make the label more descriptive
            // OR skip it entirely as shown in step 4.
        }

        String label = formatNodeLabel(node);
        TreeItem<String> item = new TreeItem<>(label);

        // 3. Specialized Handling for CompilationUnit (Root)
        if (node instanceof CompilationUnit cu) {
            item.setExpanded(true);
            cu.getPackageDeclaration().ifPresent(p ->
                    item.getChildren().add(new TreeItem<>("Package: " + p.getNameAsString())));

            if (!cu.getImports().isEmpty()) {
                TreeItem<String> importGroup = new TreeItem<>("Imports [" + cu.getImports().size() + "]");
                cu.getImports().forEach(imp ->
                        importGroup.getChildren().add(new TreeItem<>(imp.getNameAsString())));
                item.getChildren().add(importGroup);
            }
        }

        // 4. IMPROVED RECURSION: The "Magic" flattening
        for (Node child : node.getChildNodes()) {
            if (node instanceof CompilationUnit &&
                    (child instanceof com.github.javaparser.ast.PackageDeclaration ||
                            child instanceof com.github.javaparser.ast.ImportDeclaration)) continue;

            // If the child is a BlockStmt, we don't want a "BlockStmt" folder.
            // We want the things INSIDE the block to be added to THIS item.
            if (child instanceof com.github.javaparser.ast.stmt.BlockStmt) {
                for (Node subChild : child.getChildNodes()) {
                    TreeItem<String> subChildItem = buildASTTree(subChild);
                    if (subChildItem != null) item.getChildren().add(subChildItem);
                }
            } else {
                TreeItem<String> childItem = buildASTTree(child);
                if (childItem != null) item.getChildren().add(childItem);
            }
        }

        return item;
    }

    /**
     * Translates a raw JavaParser Node into a readable "Compiler-style" string.
     */
    private String formatNodeLabel(Node node) {
        // 1. Fix: Parameter with Type
        if (node instanceof com.github.javaparser.ast.body.Parameter p) {
            // This will show "Parameter: username [String]"
            return "Parameter: " + p.getNameAsString() + " [" + p.getType() + "]";
        }

        // 2. Fix: Assignment Operator
        else if (node instanceof com.github.javaparser.ast.expr.AssignExpr ae) {
            // This will show "Assignment (=)" or "Assignment (+=)", etc.
            return "Assignment (" + ae.getOperator().asString() + ")";
        }

        // --- Keep your existing logic for the rest ---

        if (node instanceof com.github.javaparser.ast.expr.Name ||
                node instanceof com.github.javaparser.ast.expr.SimpleName) {
            return "Name : " + node.toString();
        }

        if (node instanceof ClassOrInterfaceDeclaration cid) {
            return "Class: " + cid.getNameAsString();
        } else if (node instanceof MethodDeclaration md) {
            return "Method: " + md.getNameAsString() + "() : " + md.getType();
        } else if (node instanceof com.github.javaparser.ast.body.FieldDeclaration fd) {
            return "Field : [" + fd.getVariables().get(0).getNameAsString() + "]";
        } else if (node instanceof VariableDeclarator vd) {
            return "Variable: " + vd.getNameAsString() + " [" + vd.getType() + "]";
        }

        return node.getClass().getSimpleName();
    }
}