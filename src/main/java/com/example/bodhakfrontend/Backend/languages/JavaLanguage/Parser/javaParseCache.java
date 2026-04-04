package com.example.bodhakfrontend.Backend.languages.JavaLanguage.Parser;
import com.example.bodhakfrontend.Backend.interfaces.Parser;
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
public class javaParseCache implements Parser<CompilationUnit> {

    private final Map<Path, CompilationUnit> cache = new ConcurrentHashMap<>();
    private final CombinedTypeSolver typeSolver = new CombinedTypeSolver();
    private final JavaParser parser;
    public javaParseCache(List<Path> sourceRoot) {

        // Project source code
        for(Path src : sourceRoot){
        typeSolver.add(new JavaParserTypeSolver(src));}
        // JDK classes
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
    public void invalidate(Path path){
        Path normalized = path.toAbsolutePath().normalize();
        if(cache.containsKey(normalized)){cache.remove(normalized);}
        else return;

    }
    public Collection<CompilationUnit> getAll() {
        return cache.values();
    }



}