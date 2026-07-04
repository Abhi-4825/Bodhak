package com.example.bodhak.context;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.quality.flag.*;
import com.example.bodhak.model.project.ProjectSnapshot;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import com.example.bodhak.context.DependencyGraph;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.symbol.SymbolTable;

import java.util.List;

/**
 * Default {@link AnalysisContextFactory} implementation.
 */
public class DefaultAnalysisContextFactory implements AnalysisContextFactory {

    private final ProjectBaselineCalculator baselineCalculator = new ProjectBaselineCalculator();

    @Override
    public AnalysisContext create(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectClassificationResult classificationResult,
            List<CompilationUnit> compilationUnits,
            ReferenceDatabase referenceDatabase,
            SymbolTable symbolTable,
            com.example.bodhak.model.project.ProjectModel projectModel
    ) {
        ProjectBaselines baselines = baselineCalculator.calculate(snapshot.entities());
        EntityFlagAnalyzer flagAnalyzer = new EntityFlagAnalyzer(ArchitectureThresholds.defaults(), baselines);
        List<EntityCharacteristics> characteristics =
                snapshot.entities().stream().map(entityInfo ->
                    new EntityCharacteristics(entityInfo, flagAnalyzer.analyze(entityInfo))
                ).toList();

        return new AnalysisContext(
                snapshot,
                dependencyGraph,
                baselines,
                classificationResult,
                characteristics,
                compilationUnits,
                referenceDatabase,
                symbolTable,
                projectModel
        );
    }
}
