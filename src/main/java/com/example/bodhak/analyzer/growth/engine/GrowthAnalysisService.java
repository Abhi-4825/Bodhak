package com.example.bodhak.analyzer.growth.engine;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.diagnostic.AnalysisIssue;
import com.example.bodhak.analyzer.growth.rule.ArchitecturalChokePointGrowthRule;
import com.example.bodhak.analyzer.growth.rule.DeepDependencyPropagationGrowthRule;
import com.example.bodhak.analyzer.growth.rule.DependencyHubGrowthRule;
import com.example.bodhak.analyzer.growth.rule.MonolithicClusterGrowthRule;

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
