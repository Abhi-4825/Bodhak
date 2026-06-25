package com.example.bodhakfrontend.core.analysis.builder;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.project.ProjectSnapshot;
import com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult;
import com.example.bodhakfrontend.engine.DependencyGraph;

/**
 * Strategy interface for creating an {@link AnalysisContext}.
 *
 * Implementations are responsible for:
 *   - Computing {@code ProjectBaselines} from the snapshot entities
 *   - Assembling the final {@link AnalysisContext}
 *
 * No UI logic. No dashboard logic.
 */
public interface AnalysisContextFactory {

    AnalysisContext create(
            ProjectSnapshot snapshot,
            DependencyGraph dependencyGraph,
            ProjectClassificationResult classificationResult
    );
}
