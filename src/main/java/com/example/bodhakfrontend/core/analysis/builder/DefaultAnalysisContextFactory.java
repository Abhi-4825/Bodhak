package com.example.bodhakfrontend.core.analysis.builder;

import com.example.bodhakfrontend.core.analysis.entityflag.*;
import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.project.ProjectSnapshot;
import com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult;
import com.example.bodhakfrontend.engine.DependencyGraph;

import java.util.List;

/**
 * Default {@link AnalysisContextFactory} implementation.
 *
 * Responsibilities:
 *   1. Calculate {@link ProjectBaselines} from the snapshot's entity list
 *   2. Build and return a fully-populated {@link AnalysisContext}
 *
 * No UI logic. No dashboard logic.
 */
public class DefaultAnalysisContextFactory implements AnalysisContextFactory {

    private final ProjectBaselineCalculator baselineCalculator = new ProjectBaselineCalculator();


    @Override
    public AnalysisContext create(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectClassificationResult classificationResult
    ) {


        ProjectBaselines baselines = baselineCalculator.calculate(snapshot.entities());
        EntityFlagAnalyzer flagAnalyzer=new EntityFlagAnalyzer(ArchitectureThresholds.defaults(),baselines);
        List<EntityCharacteristics> characteristics=
                snapshot.entities().stream().map(entityInfo ->
                    new EntityCharacteristics(entityInfo,flagAnalyzer.analyze(entityInfo))
                ).toList();

        return new AnalysisContext(
                snapshot,
                dependencyGraph,
                baselines,
                classificationResult,
                characteristics
        );
    }
}
