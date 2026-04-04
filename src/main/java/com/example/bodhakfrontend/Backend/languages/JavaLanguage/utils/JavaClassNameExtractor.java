package com.example.bodhakfrontend.Backend.languages.JavaLanguage.utils;

import com.example.bodhakfrontend.Backend.Factory.ParserFactory;
import com.example.bodhakfrontend.Backend.interfaces.ClassNameExtractor;
import com.example.bodhakfrontend.Backend.interfaces.Parser;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;

import java.nio.file.Path;
import java.util.HashSet;

import java.util.Set;


public class JavaClassNameExtractor implements ClassNameExtractor {

    private final ParserFactory parserFactory;
    private final Parser<CompilationUnit> parser;
    public JavaClassNameExtractor(ParserFactory parserFactory) {
        this.parserFactory = parserFactory;
        this.parser=parserFactory.getParser("java");
    }
    @Override
    public Set<String> getClassNames(Path filePath) {
        Path normalizedPath = filePath.toAbsolutePath().normalize();
        Set<String> classNames = new HashSet<>();
        CompilationUnit cu=parser.parse(normalizedPath);
        if(cu==null){
            System.out.println("CompilationUnit not found "+filePath);
            return classNames;
        }
        cu.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(classOrI -> {
                    String className = ClassNameResolver.resolveFqn(cu,classOrI);
                    classNames.add(className);
                });
        cu.findAll(EnumDeclaration.class).forEach(enumDeclaration -> {
            String  enumName = ClassNameResolver.resolveFqn(cu,enumDeclaration);
            classNames.add(enumName);
        });
        cu.findAll(RecordDeclaration.class).forEach(recordDeclaration -> {
            String  recordName = ClassNameResolver.resolveFqn(cu,recordDeclaration);
            classNames.add(recordName);
        });

        return classNames;
    }


    }


