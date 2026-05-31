package com.example.bodhakfrontend.languages.java.ast;

import com.example.bodhakfrontend.core.model.ast.GenericAstNode;
import com.example.bodhakfrontend.core.plugin.AstProvider;
import com.example.bodhakfrontend.core.plugin.Parser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.nio.file.Path;

/**
 * Builds a GenericAstNode tree from a Java source file using JavaParser.
 * Filters out noise nodes (pure types, modifiers) and collapses BlockStmts
 * to keep the tree readable.
 */
public class JavaAstProvider implements AstProvider {

    private final Parser<CompilationUnit> parser;

    public JavaAstProvider(Parser<CompilationUnit> parser) {
        this.parser = parser;
    }

    @Override
    public GenericAstNode parse(Path file) {
        CompilationUnit cu = parser.parse(file);
        if (cu == null) return null;
        return buildNode(cu);
    }

    private GenericAstNode buildNode(Node node) {
        // Filter pure noise
        if (node instanceof com.github.javaparser.ast.Modifier ||
                node instanceof com.github.javaparser.ast.type.Type) {
            return null;
        }

        String label = label(node);
        String kind  = kind(node);
        GenericAstNode astNode = new GenericAstNode(label, kind);

        node.getRange().ifPresent(range -> {
            // JavaParser lines are 1-indexed, RichTextFX expects 0-indexed
            int beginLine = range.begin.line - 1;
            int beginCol = range.begin.column - 1;
            int endLine = range.end.line - 1;
            int endCol = range.end.column;
            astNode.setPosition(beginLine, beginCol, endLine, endCol);
        });

        // Special: CompilationUnit — add package + grouped imports
        if (node instanceof CompilationUnit cu) {
            cu.getPackageDeclaration().ifPresent(p ->
                    astNode.addChild(new GenericAstNode("Package: " + p.getNameAsString(), "package")));

            if (!cu.getImports().isEmpty()) {
                GenericAstNode importsGroup = new GenericAstNode(
                        "Imports [" + cu.getImports().size() + "]", "imports");
                cu.getImports().forEach(imp ->
                        importsGroup.addChild(new GenericAstNode(imp.getNameAsString(), "import")));
                astNode.addChild(importsGroup);
            }
        }

        // Recurse children — flatten BlockStmt to reduce nesting
        for (Node child : node.getChildNodes()) {
            // Skip already-handled CU children
            if (node instanceof CompilationUnit &&
                    (child instanceof com.github.javaparser.ast.PackageDeclaration ||
                     child instanceof com.github.javaparser.ast.ImportDeclaration)) continue;

            if (child instanceof BlockStmt) {
                // Don't create a BlockStmt node — add its contents directly
                for (Node inner : child.getChildNodes()) {
                    GenericAstNode innerNode = buildNode(inner);
                    if (innerNode != null) astNode.addChild(innerNode);
                }
            } else {
                GenericAstNode childNode = buildNode(child);
                if (childNode != null) astNode.addChild(childNode);
            }
        }
        return astNode;
    }

    private String label(Node node) {
        if (node instanceof CompilationUnit) return "File";
        if (node instanceof ClassOrInterfaceDeclaration c) return "Class: " + c.getNameAsString();
        if (node instanceof MethodDeclaration m) return "Method: " + m.getNameAsString() + "() : " + m.getType();
        if (node instanceof ConstructorDeclaration c) return "Constructor: " + c.getNameAsString();
        if (node instanceof FieldDeclaration f && !f.getVariables().isEmpty())
            return "Field: " + f.getVariables().get(0).getNameAsString();
        if (node instanceof VariableDeclarator v) return "Variable: " + v.getNameAsString() + " [" + v.getType() + "]";
        if (node instanceof Parameter p) return "Param: " + p.getNameAsString() + " [" + p.getType() + "]";
        if (node instanceof MethodCallExpr m) return "Call: " + m.getNameAsString() + "()";
        if (node instanceof AssignExpr a) return "Assign (" + a.getOperator().asString() + ")";
        if (node instanceof ReturnStmt) return "return";
        if (node instanceof IfStmt) return "if";
        if (node instanceof ForStmt || node instanceof ForEachStmt) return "for";
        if (node instanceof WhileStmt) return "while";
        if (node instanceof TryStmt) return "try";
        if (node instanceof com.github.javaparser.ast.expr.Name ||
                node instanceof com.github.javaparser.ast.expr.SimpleName)
            return "Name: " + node;
        return node.getClass().getSimpleName();
    }

    private String kind(Node node) {
        if (node instanceof CompilationUnit) return "file";
        if (node instanceof ClassOrInterfaceDeclaration) return "class";
        if (node instanceof MethodDeclaration) return "method";
        if (node instanceof ConstructorDeclaration) return "constructor";
        if (node instanceof FieldDeclaration) return "field";
        if (node instanceof VariableDeclarator) return "variable";
        if (node instanceof Parameter) return "parameter";
        if (node instanceof MethodCallExpr) return "call";
        if (node instanceof AssignExpr) return "assign";
        if (node instanceof ReturnStmt) return "return";
        if (node instanceof IfStmt) return "if";
        if (node instanceof ForStmt || node instanceof ForEachStmt) return "for";
        if (node instanceof WhileStmt) return "while";
        if (node instanceof TryStmt) return "try";
        return "node";
    }
}
