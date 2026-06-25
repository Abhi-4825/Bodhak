package com.example.bodhakfrontend.engine.Performance.core;



import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.analysis.AnalysisReport;

import com.example.bodhakfrontend.engine.Performance.Rules.DeepDependencyChainRule;

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
