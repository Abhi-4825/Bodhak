package com.example.anuviya.context;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.quality.flag.*;
import com.example.anuviya.model.project.ProjectSnapshot;
import com.example.anuviya.classification.classifier.ProjectClassificationResult;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.compiler.symbol.SymbolTable;

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
            com.example.anuviya.model.project.ProjectModel projectModel
    ) {
        ProjectBaselines baselines = baselineCalculator.calculate(snapshot.entities());
        ArchitectureThresholds thresholds = buildCustomThresholds(classificationResult);
        EntityFlagAnalyzer flagAnalyzer = new EntityFlagAnalyzer(thresholds, baselines);
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

    private ArchitectureThresholds buildCustomThresholds(
            ProjectClassificationResult classificationResult
    ) {
        int loc = 200;
        int methods = 15;
        int fields = 8;
        int coupling = 10;
        int fanIn = 8;
        int fanOut = 8;

        if (classificationResult != null && classificationResult.detectedTechnologies() != null) {
            for (com.example.anuviya.classification.intelligence.DetectedTechnology tech : classificationResult.detectedTechnologies()) {
                if (tech.metadata() != null) {
                    String locStr = tech.metadata().get("godClassLocThreshold");
                    if (locStr != null) {
                        try {
                            loc = Math.max(loc, Integer.parseInt(locStr));
                        } catch (NumberFormatException ignored) {}
                    }
                    String fanOutStr = tech.metadata().get("highCouplingFanOutThreshold");
                    if (fanOutStr != null) {
                        try {
                            fanOut = Math.max(fanOut, Integer.parseInt(fanOutStr));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return new ArchitectureThresholds(loc, methods, fields, coupling, fanIn, fanOut);
    }
}
