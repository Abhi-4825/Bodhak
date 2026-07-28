package com.example.anuviya.analyzer.growth.engine;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.diagnostic.AnalysisIssue;
import com.example.anuviya.analyzer.growth.rule.ArchitecturalChokePointGrowthRule;
import com.example.anuviya.analyzer.growth.rule.DeepDependencyPropagationGrowthRule;
import com.example.anuviya.analyzer.growth.rule.DependencyHubGrowthRule;
import com.example.anuviya.analyzer.growth.rule.MonolithicClusterGrowthRule;

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
