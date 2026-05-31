package com.example.bodhakfrontend.languages.java.parser;

import com.example.bodhakfrontend.core.plugin.Parser;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JavaParseCache implements Parser<CompilationUnit> {

    private final Map<Path, CompilationUnit> cache = new ConcurrentHashMap<>();
    private final CombinedTypeSolver typeSolver = new CombinedTypeSolver();
    private final JavaParser parser;

    public JavaParseCache(List<Path> sourceRoots) {
        for (Path src : sourceRoots) {
            typeSolver.add(new JavaParserTypeSolver(src));
        }
        typeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setSymbolResolver(symbolSolver);
        this.parser = new JavaParser(config);
    }

    @Override
    public CompilationUnit parse(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        return cache.computeIfAbsent(normalized, p -> {
            try {
                return parser.parse(p).getResult().orElse(null);
            } catch (Exception e) {
                return null;
            }
        });
    }

    @Override
    public void invalidate(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        cache.remove(normalized);
    }

    public Collection<CompilationUnit> getAll() {
        return cache.values();
    }
}
