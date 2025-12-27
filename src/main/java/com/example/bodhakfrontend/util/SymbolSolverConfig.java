package com.example.bodhakfrontend.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.nio.file.Path;
import java.util.List;

public class SymbolSolverConfig {

    public static JavaParser createParser(Path projectRoot) {

        CombinedTypeSolver solver = new CombinedTypeSolver();

        // 1️⃣ JDK classes
        solver.add(new ReflectionTypeSolver());

        // 2️⃣ Project source roots (CRITICAL)
        MultiModuleSourceRootDetector detector =
                new MultiModuleSourceRootDetector();

        List<Path> srcRoots = detector.detectSourceRoots(projectRoot);

        for (Path src : srcRoots) {
            solver.add(new JavaParserTypeSolver(src));
        }

        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(solver));

        return new JavaParser(config);
    }
}

