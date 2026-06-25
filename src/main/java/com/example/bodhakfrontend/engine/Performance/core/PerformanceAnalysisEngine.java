package com.example.bodhakfrontend.engine.Performance.core;



import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.analysis.AnalysisIssue;
import com.example.bodhakfrontend.core.analysis.AnalysisReport;
import com.example.bodhakfrontend.engine.Performance.Rules.PerformanceRule;

import java.util.ArrayList;
import java.util.List;

public class PerformanceAnalysisEngine {

    private final List<PerformanceRule> rules =
            new ArrayList<>();

    public void registerRule(
            PerformanceRule rule
    ) {

        rules.add(rule);
    }

    public AnalysisReport analyze(
            AnalysisContext context
    ) {

        List<AnalysisIssue> allIssues =
                new ArrayList<>();

        for (PerformanceRule rule : rules) {

            try {

                List<AnalysisIssue> issues =
                        rule.analyze(context);

                allIssues.addAll(issues);

            } catch (Exception ex) {

                System.err.println(
                        "Failed to execute rule: "
                                + rule.getClass().getSimpleName()
                );

                ex.printStackTrace();
            }
        }

        return new AnalysisReport(allIssues);
    }
}
