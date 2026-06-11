package com.example.bodhakfrontend.engine.growth.core;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Analysis.AnalysisIssue;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.DependencyGraph;
import com.example.bodhakfrontend.engine.growth.rules.ArchitecturalChokePointGrowthRule;
import com.example.bodhakfrontend.engine.growth.rules.DeepDependencyPropagationGrowthRule;
import com.example.bodhakfrontend.engine.growth.rules.DependencyHubGrowthRule;
import com.example.bodhakfrontend.engine.growth.rules.MonolithicClusterGrowthRule;

import java.util.List;

public class GrowthAnalysisService {

    private final GrowthAnalysisEngine engine =
            new GrowthAnalysisEngine();

    public GrowthAnalysisService() {
        registerRules();
    }

    private void registerRules() {

        engine.registerRule(
                new DependencyHubGrowthRule()
        );
        engine.registerRule(new DeepDependencyPropagationGrowthRule());
        engine.registerRule(new MonolithicClusterGrowthRule());
        engine.registerRule(new ArchitecturalChokePointGrowthRule());
    }

    public List<AnalysisIssue> analyzeProject(
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
