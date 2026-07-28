package com.example.anuviya.context;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.model.project.ProjectSnapshot;
import com.example.anuviya.classification.classifier.ProjectClassificationResult;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.compiler.symbol.SymbolTable;
import java.util.List;

/**
 * Strategy interface for creating an {@link AnalysisContext}.
 */
public interface AnalysisContextFactory {

    default AnalysisContext create(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectClassificationResult classificationResult,
            List<CompilationUnit> compilationUnits
    ) {
        return create(snapshot, dependencyGraph, classificationResult, compilationUnits, new ReferenceDatabase(), new SymbolTable(), com.example.anuviya.model.project.ProjectModel.empty());
    }

    AnalysisContext create(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectClassificationResult classificationResult,
            List<CompilationUnit> compilationUnits,
            ReferenceDatabase referenceDatabase,
            SymbolTable symbolTable,
            com.example.anuviya.model.project.ProjectModel projectModel
    );
}
