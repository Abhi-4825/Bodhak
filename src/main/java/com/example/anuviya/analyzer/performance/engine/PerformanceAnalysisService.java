package com.example.anuviya.analyzer.performance.engine;



import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.AnalysisReport;

import com.example.anuviya.analyzer.performance.rule.DeepDependencyChainRule;

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
