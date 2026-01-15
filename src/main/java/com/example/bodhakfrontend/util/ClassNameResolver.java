package com.example.bodhakfrontend.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class ClassNameResolver {

    public static String resolveFqn(
            CompilationUnit cu,
            TypeDeclaration<?> typeDecl
    ) {
        String astFqn = buildAstFqn(cu, typeDecl);

        try {
            Optional<String> resolved = typeDecl.getFullyQualifiedName();
            return resolved.orElse(astFqn);
        } catch (Exception e) {
            return astFqn;
        }
    }

    private static String buildAstFqn(
            CompilationUnit cu,
            TypeDeclaration<?> typeDecl
    ) {
        String packageName = cu.getPackageDeclaration()
                .map(p -> p.getNameAsString())
                .orElse("");

        Deque<String> names = new ArrayDeque<>();
        names.push(typeDecl.getNameAsString());

        Node parent = typeDecl.getParentNode().orElse(null);
        while (parent != null) {
            if (parent instanceof TypeDeclaration<?> parentType) {
                names.push(parentType.getNameAsString());
            }
            parent = parent.getParentNode().orElse(null);
        }

        String typePath = String.join(".", names);

        return packageName.isEmpty()
                ? typePath
                : packageName + "." + typePath;
    }
    public static String resolveTypeFqn(
            CompilationUnit cu,
            ClassOrInterfaceType type
    ) {
        String simpleName = type.getNameAsString();

        // Fully qualified usage: com.foo.Bar
        if (type.getScope().isPresent()) {
            return type.toString();
        }

        // Explicit imports
        for (ImportDeclaration imp : cu.getImports()) {
            if (!imp.isAsterisk()) {
                String imported = imp.getNameAsString();
                if (imported.endsWith("." + simpleName)) {
                    return imported;
                }
            }
        }

        // Same package
        return cu.getPackageDeclaration()
                .map(p -> p.getNameAsString() + "." + simpleName)
                .orElse(simpleName);
    }

    public static String resolveScopeFqn(
            CompilationUnit cu,
            Expression scope
    ) {
        String text = scope.toString();

        if (text.isEmpty() || !Character.isUpperCase(text.charAt(0))) {
            return null; // variable or expression
        }

        // Try imports
        for (ImportDeclaration imp : cu.getImports()) {
            if (!imp.isAsterisk() && imp.getNameAsString().endsWith("." + text)) {
                return imp.getNameAsString();
            }
        }

        // Same package fallback
        return cu.getPackageDeclaration()
                .map(p -> p.getNameAsString() + "." + text)
                .orElse(text);
    }

}
