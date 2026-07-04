package com.example.bodhak.analyzer.performance.engine;



import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.diagnostic.AnalysisIssue;
import com.example.bodhak.context.AnalysisReport;
import com.example.bodhak.analyzer.performance.rule.PerformanceRule;

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
