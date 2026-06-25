package com.example.bodhakfrontend.engine.growth.core;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.analysis.AnalysisIssue;
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

    public List<AnalysisIssue> analyzeProject(AnalysisContext context) {

        return engine.analyze(context);
    }
}
