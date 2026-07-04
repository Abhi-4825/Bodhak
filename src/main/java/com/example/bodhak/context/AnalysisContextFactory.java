package com.example.bodhak.context;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.model.project.ProjectSnapshot;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import com.example.bodhak.context.DependencyGraph;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.symbol.SymbolTable;
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
        return create(snapshot, dependencyGraph, classificationResult, compilationUnits, new ReferenceDatabase(), new SymbolTable(), com.example.bodhak.model.project.ProjectModel.empty());
    }

    AnalysisContext create(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectClassificationResult classificationResult,
            List<CompilationUnit> compilationUnits,
            ReferenceDatabase referenceDatabase,
            SymbolTable symbolTable,
            com.example.bodhak.model.project.ProjectModel projectModel
    );
}
