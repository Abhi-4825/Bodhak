package com.example.bodhakfrontend.util;

import com.example.bodhakfrontend.Models.DependencyNode;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BuildClassIndex {
    private final JavaFileParser parser;
    private final ParseCache cache;
    public BuildClassIndex(JavaFileParser parser, ParseCache cache) {
        this.parser = parser;
        this.cache = cache;
    }
    public  Map<String,DependencyNode> build(Path sourceFile) {
        Map<String,DependencyNode> dependencyNodeMap=new HashMap<>();
        try{
            Files.walk(sourceFile).filter(path ->
                    path.toString().endsWith(".java"))
                    .forEach(path -> indexJavaFile(path, dependencyNodeMap));
        }catch (Exception e){
            e.printStackTrace();
        }
        return dependencyNodeMap;
    }

    private void indexJavaFile(Path file, Map<String,DependencyNode> dependencyNodeMap) {
        try{
            CompilationUnit cu=cache.get(file);
            if(cu!=null){
            String packageName = cu.getPackageDeclaration()
                    .map(p -> p.getNameAsString())
                    .orElse("");
            cu.findAll(ClassOrInterfaceDeclaration.class)
                    .forEach(classNode -> {
                        String className = classNode.getNameAsString();
                        var range=classNode.getName().getRange().orElse(null);
                        if(range==null)return;
                        int line=range.begin.line;
                        int column=range.begin.column;

                        DependencyNode node=new DependencyNode(className,packageName,file.toFile(),line,column);
                        dependencyNodeMap.put(className,node);

                    });
            cu.findAll(EnumDeclaration.class)
                    .forEach(enumNode -> {
                        String className = enumNode.getNameAsString();
                        var range=enumNode.getName().getRange().orElse(null);
                        if(range==null)return;
                        int line=range.begin.line;
                        int column=range.begin.column;

                        DependencyNode node=new DependencyNode(className,packageName,file.toFile(),line,column);
                        dependencyNodeMap.put(className,node);

                    });
            cu.findAll(RecordDeclaration.class)
                    .forEach(recordNode -> {
                        String className = recordNode.getNameAsString();
                        var range=recordNode.getName().getRange().orElse(null);
                        if(range==null)return;
                        int line=range.begin.line;
                        int column=range.begin.column;
                        DependencyNode node=new DependencyNode(className,packageName,file.toFile(),line,column);
                        dependencyNodeMap.put(className,node);


                    });}


        } catch (Exception e) {
            System.out.println( "error occured while trying to index java file " + file.toString() );
            e.printStackTrace();
        }
    }
}
