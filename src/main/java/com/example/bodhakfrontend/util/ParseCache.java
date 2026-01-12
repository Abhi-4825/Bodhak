package com.example.bodhakfrontend.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParseCache {

    private final Map<Path, CompilationUnit> cache = new ConcurrentHashMap<>();
    private final JavaParser parser;

    public ParseCache(Path sourceRoot) {

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();

        // Project source code
        typeSolver.add(new JavaParserTypeSolver(sourceRoot));

        // JDK classes
        typeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setSymbolResolver(symbolSolver);

        this.parser = new JavaParser(config);
    }

    public CompilationUnit get(Path path) {

        Path normalized = path.toAbsolutePath().normalize();

        return cache.computeIfAbsent(normalized, p -> {
            try {
                return parser.parse(p).getResult().orElse(null);
            } catch (Exception e) {
                return null;
            }
        });
    }
    public void invalidate(Path path){
        Path normalized = path.toAbsolutePath().normalize();
        cache.remove(normalized);
    }

    public Collection<CompilationUnit> getAll() {
        return cache.values();
    }
}

