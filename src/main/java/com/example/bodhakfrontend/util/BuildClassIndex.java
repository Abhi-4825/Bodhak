package com.example.bodhakfrontend.util;

import com.example.bodhakfrontend.Models.DependencyNode;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class BuildClassIndex {

    private Map<String, DependencyNode> classIndex;
    public BuildClassIndex(Map<String, DependencyNode> classIndex) {
        this.classIndex = classIndex;
    }
    public void buildClassIndex(File  sourceFile) {
        classIndex.clear();
        try{
            Files.walk(sourceFile.toPath()).filter(path ->
                    path.toString().endsWith(".java"))
                    .forEach(path -> indexJavaFile(path.toFile()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void indexJavaFile(File file) {
        try{
            CompilationUnit cu= StaticJavaParser.parse(file);
            String packageName = cu.getPackageDeclaration()
                    .map(p -> p.getNameAsString())
                    .orElse("");
            cu.findAll(ClassOrInterfaceDeclaration.class)
                    .forEach(classNode -> {
                        String className = classNode.getNameAsString();

                        int beginLine=classNode.getBegin().map(p-> p.line).orElse(1);
                        DependencyNode node=new DependencyNode(className,packageName,file,beginLine);
                        classIndex.put(className,node);
                    });
            cu.findAll(EnumDeclaration.class)
                    .forEach(enumNode -> {
                        String className = enumNode.getNameAsString();
                        int beginLine=enumNode.getBegin().map(p-> p.line).orElse(1);
                        DependencyNode node=new DependencyNode(className,packageName,file,beginLine);
                        classIndex.put(className,node);
                    });
            cu.findAll(RecordDeclaration.class)
                    .forEach(recordNode -> {
                        String className = recordNode.getNameAsString();
                        int beginLine=recordNode.getBegin().map(p-> p.line).orElse(1);
                        DependencyNode node=new DependencyNode(className,packageName,file,beginLine);
                        classIndex.put(className,node);

                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
