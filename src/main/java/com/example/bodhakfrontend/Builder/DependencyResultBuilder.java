package com.example.bodhakfrontend.Builder;

import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.dependenciesResultmodel.DependencyResult;
import com.example.bodhakfrontend.util.ParseCache;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyResultBuilder {
    private final ParseCache cache;
    private final JavaFileParser parser;
    public DependencyResultBuilder(ParseCache cache, JavaFileParser parser) {
        this.cache = cache;
        this.parser = parser;
    }
    public DependencyResult extractDependencies(File file, Set<String> sourceClasses) throws Exception {
        CompilationUnit cu =cache.get(file.toPath()) ;
        // imports
        List<String> externalDeps=cu.getImports()
                .stream()
                .map(i -> i.getNameAsString())
                .toList();
        // class dependencies
        Set<String> internalDeps=new HashSet<>();
        Set<String> javaLibrariesDependencies=new HashSet<>();
        cu.findAll(ClassOrInterfaceType.class).forEach(classOrI -> {
            {
                String name=classOrI.getNameAsString();
                if(sourceClasses.contains(name)){
                    internalDeps.add(name);
                }
                else
                    javaLibrariesDependencies.add(name);
            }
        });

        return  new DependencyResult(externalDeps,new ArrayList<>(internalDeps),new ArrayList<>(javaLibrariesDependencies));
    }
}
