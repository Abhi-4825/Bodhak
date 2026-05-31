package com.example.bodhakfrontend.languages.java.extractor;

import com.example.bodhakfrontend.core.plugin.EntityNameExtractor;
import com.example.bodhakfrontend.core.plugin.Parser;
import com.example.bodhakfrontend.languages.java.util.JavaClassNameResolver;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class JavaEntityNameExtractor implements EntityNameExtractor {

    private final Parser<CompilationUnit> parser;

    public JavaEntityNameExtractor(Parser<CompilationUnit> parser) {
        this.parser = parser;
    }

    @Override
    public Set<String> extractNames(Path filePath) {
        Path normalizedPath = filePath.toAbsolutePath().normalize();
        Set<String> classNames = new HashSet<>();
        CompilationUnit cu = parser.parse(normalizedPath);
        if (cu == null) {
            System.out.println("CompilationUnit not found " + filePath);
            return classNames;
        }

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classOrI -> {
            String className = JavaClassNameResolver.resolveFqn(cu, classOrI);
            classNames.add(className);
        });
        cu.findAll(EnumDeclaration.class).forEach(enumDeclaration -> {
            String enumName = JavaClassNameResolver.resolveFqn(cu, enumDeclaration);
            classNames.add(enumName);
        });
        cu.findAll(RecordDeclaration.class).forEach(recordDeclaration -> {
            String recordName = JavaClassNameResolver.resolveFqn(cu, recordDeclaration);
            classNames.add(recordName);
        });

        return classNames;
    }
}
