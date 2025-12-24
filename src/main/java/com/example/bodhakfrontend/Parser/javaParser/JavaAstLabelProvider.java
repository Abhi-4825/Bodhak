package com.example.bodhakfrontend.Parser.javaParser;
import com.example.bodhakfrontend.Parser.AstLabelProvider;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;



public class JavaAstLabelProvider implements AstLabelProvider<Node> {

    @Override
    public String getLabel(Node node) {
        // ----- PACKAGE -----
        if (node instanceof PackageDeclaration p) {
            return "Package : " + p.getNameAsString();
        }


        // ----- SIMPLE NAME -----
        if (node instanceof Name n) {
            return "Name : " + n.asString();
        }

        // ----- CLASS -----
        if (node instanceof ClassOrInterfaceDeclaration c) {
            return "Class : " + c.getNameAsString();
        }

        // ----- METHOD -----
        if (node instanceof MethodDeclaration m) {
            return "Method : " + m.getNameAsString() + "()";
        }

        // ----- FIELD -----
        if (node instanceof FieldDeclaration f) {
            return "Field : " + f.getVariables();
        }

        // ----- VARIABLE -----
        if (node instanceof VariableDeclarator v) {
            return "Variable : " + v.getNameAsString();
        }

        // ----- IMPORT -----
        if (node instanceof ImportDeclaration i) {
            return "Import : " + i.getNameAsString();
        }

        return node.getClass().getSimpleName();
    }
}

