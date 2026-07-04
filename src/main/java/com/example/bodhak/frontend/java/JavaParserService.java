package com.example.bodhak.frontend.java;

import com.example.bodhak.frontend.ParserService;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavaParser service wrapper for parsing Java compilation units.
 */
public class JavaParserService implements ParserService<CompilationUnit> {

    private final Map<Path, CompilationUnit> cache = new ConcurrentHashMap<>();
    private final CombinedTypeSolver typeSolver = new CombinedTypeSolver();
    private final JavaParser parser;

    public JavaParserService(List<Path> sourceRoots) {
        for (Path src : sourceRoots) {
            try {
                typeSolver.add(new JavaParserTypeSolver(src));
            } catch (Exception e) {
                System.err.println("JavaParserService source root solver issue: " + src + " -> " + e.getMessage());
            }
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
}
