package com.example.bodhak.analyzer.performance.engine;



import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.AnalysisReport;

import com.example.bodhak.analyzer.performance.rule.DeepDependencyChainRule;

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

    public AnalysisReport analyzeProject(AnalysisContext context) {

        return engine.analyze(context);
    }
}
