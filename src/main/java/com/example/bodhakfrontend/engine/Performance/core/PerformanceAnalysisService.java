package com.example.bodhakfrontend.engine.Performance.core;



import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Analysis.AnalysisReport;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.DependencyGraph;
import com.example.bodhakfrontend.engine.Performance.Rules.DeepDependencyChainRule;


import java.util.List;

public class PerformanceAnalysisService {

    private final PerformanceAnalysisEngine engine =
            new PerformanceAnalysisEngine();

    public PerformanceAnalysisService() {

        registerRules();
    }

    private void registerRules() {

        engine.registerRule(
                new DeepDependencyChainRule()
        );
    }

    public AnalysisReport analyzeProject(
            ProjectInfo projectInfo,
            DependencyGraph dependencyGraph,
            List<EntityInfo> entities
    ) {

        AnalysisContext context =
                new AnalysisContext(
                        projectInfo,
                        dependencyGraph,
                        entities
                );

        return engine.analyze(context);
    }
}
