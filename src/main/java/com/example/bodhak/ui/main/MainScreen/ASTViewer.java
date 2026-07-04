package com.example.bodhak.ui.main.MainScreen;

import com.example.bodhak.ir.*;
import com.example.bodhak.ir.declaration.*;
import com.example.bodhak.ir.statement.*;
import com.example.bodhak.ir.expression.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * A completely generic AST viewer. It knows nothing about JavaParser or Tree-sitter.
 * It strictly renders a core IRNode tree.
 */
public class ASTViewer {

    public ASTViewer() {}

    public void show(File file, IRNode rootNode) {
        if (rootNode == null) {
            System.err.println("Could not generate AST for " + file.getName());
            return;
        }

        TreeItem<IRNode> root = buildTreeView(rootNode);
        root.setExpanded(true);

        TreeView<IRNode> tree = new TreeView<>(root);
        VBox.setVgrow(tree, Priority.ALWAYS);

        tree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(IRNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(getNodeLabel(item));
                }
            }
        });

        CodeArea codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.getStyleClass().add("code-area-preview");

        try {
            codeArea.replaceText(Files.readString(file.toPath()));
        } catch (Exception ignored) {}

        tree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                IRNode node = newVal.getValue();
                SourceRange sr = node.sourceRange();
                if (sr != null && sr.startLine() >= 0 && sr.endLine() >= 0) {
                    try {
                        codeArea.selectRange(sr.startLine() - 1, sr.startColumn(), sr.endLine() - 1, sr.endColumn());
                        codeArea.showParagraphAtCenter(sr.startLine() - 1);
                    } catch (Exception e) {
                        // Ignore
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

    private TreeItem<IRNode> buildTreeView(IRNode node) {
        TreeItem<IRNode> item = new TreeItem<>(node);
        
        if (node instanceof ModuleDeclaration || node instanceof TypeDeclaration) {
            item.setExpanded(true);
        }

        for (IRNode child : ChildrenCollector.collect(node)) {
            TreeItem<IRNode> childItem = buildTreeView(child);
            item.getChildren().add(childItem);
        }

        return item;
    }

    private String getNodeLabel(IRNode node) {
        String type = node.getClass().getSimpleName();
        if (node instanceof NamespaceDeclaration nd) {
            return type + ": " + nd.name();
        }
        if (node instanceof ModuleDeclaration md) {
            return type + ": " + md.name();
        }
        if (node instanceof TypeDeclaration td) {
            return type + ": " + td.name();
        }
        if (node instanceof CallableDeclaration cd) {
            return type + ": " + cd.name();
        }
        if (node instanceof VariableDeclaration vd) {
            return type + ": " + vd.name();
        }
        if (node instanceof DecoratorNode dn) {
            return type + ": " + dn.name();
        }
        return type;
    }

    private static class ChildrenCollector implements IRVisitor {
        private final List<IRNode> children = new ArrayList<>();

        public static List<IRNode> collect(IRNode node) {
            ChildrenCollector collector = new ChildrenCollector();
            node.accept(collector);
            return collector.children;
        }

        @Override
        public void visit(ModuleDeclaration node) {
            children.addAll(node.declarations());
        }

        @Override
        public void visit(TypeDeclaration node) {
            children.addAll(node.decorators());
            children.addAll(node.members());
        }

        @Override
        public void visit(CallableDeclaration node) {
            children.addAll(node.decorators());
            children.addAll(node.parameters());
            if (node.body() != null) {
                children.add(node.body());
            }
        }

        @Override
        public void visit(VariableDeclaration node) {
            children.addAll(node.decorators());
            if (node.initializer() != null) {
                children.add(node.initializer());
            }
        }

        @Override
        public void visit(DecoratorNode node) {
            children.addAll(node.arguments());
        }

        @Override
        public void visit(BlockStatement node) {
            children.addAll(node.statements());
        }

        @Override
        public void visit(IfStatement node) {
            if (node.condition() != null) children.add(node.condition());
            if (node.thenBranch() != null) children.add(node.thenBranch());
            if (node.elseBranch() != null) children.add(node.elseBranch());
        }

        @Override
        public void visit(LoopStatement node) {
            if (node.condition() != null) children.add(node.condition());
            if (node.body() != null) children.add(node.body());
        }

        @Override
        public void visit(TryStatement node) {
            if (node.tryBlock() != null) children.add(node.tryBlock());
            children.addAll(node.catchBlocks());
            if (node.finallyBlock() != null) children.add(node.finallyBlock());
        }

        @Override
        public void visit(ReturnStatement node) {
            if (node.expression() != null) children.add(node.expression());
        }

        @Override
        public void visit(ExpressionStatement node) {
            if (node.expression() != null) children.add(node.expression());
        }

        @Override
        public void visit(CallExpression node) {
            if (node.receiver() != null) children.add(node.receiver());
            children.addAll(node.arguments());
        }

        @Override
        public void visit(ObjectCreationExpression node) {
            if (node.type() != null) children.add(node.type());
            children.addAll(node.arguments());
        }

        @Override
        public void visit(AssignmentExpression node) {
            if (node.target() != null) children.add(node.target());
            if (node.value() != null) children.add(node.value());
        }

        @Override
        public void visit(FieldAccessExpression node) {
            if (node.receiver() != null) children.add(node.receiver());
        }

        @Override
        public void visit(CastExpression node) {
            if (node.targetType() != null) children.add(node.targetType());
            if (node.expression() != null) children.add(node.expression());
        }

        @Override
        public void visit(BinaryExpression node) {
            if (node.left() != null) children.add(node.left());
            if (node.right() != null) children.add(node.right());
        }

        @Override
        public void visit(UnaryExpression node) {
            if (node.expression() != null) children.add(node.expression());
        }

        @Override
        public void visit(ConditionalExpression node) {
            if (node.condition() != null) children.add(node.condition());
            if (node.thenExpr() != null) children.add(node.thenExpr());
            if (node.elseExpr() != null) children.add(node.elseExpr());
        }

        @Override
        public void visit(ArrayAccessExpression node) {
            if (node.array() != null) children.add(node.array());
            if (node.index() != null) children.add(node.index());
        }

        @Override
        public void visit(ArrayCreationExpression node) {
            if (node.type() != null) children.add(node.type());
            children.addAll(node.dimensions());
            if (node.initializer() != null) children.add(node.initializer());
        }

        @Override
        public void visit(LambdaExpression node) {
            children.addAll(node.parameters());
            if (node.body() != null) children.add(node.body());
        }

        @Override
        public void visit(AnnotationExpression node) {
            children.addAll(node.arguments());
        }

        @Override
        public void visit(LocalVariableStatement node) {
            if (node.declaration() != null) {
                children.add(node.declaration());
            }
        }
    }
}
