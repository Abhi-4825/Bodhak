package com.example.anuviya.analyzer.growth.engine;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.diagnostic.AnalysisIssue;
import com.example.anuviya.analyzer.growth.rule.GrowthRule;

import java.util.ArrayList;
import java.util.List;

public class GrowthAnalysisEngine {

    private final List<GrowthRule> rules =
            new ArrayList<>();

    public void registerRule(
            GrowthRule rule
    ) {
        rules.add(rule);
    }

    public List<AnalysisIssue> analyze(
            AnalysisContext context
    ) {

        List<AnalysisIssue> issues =
                new ArrayList<>();

        for (GrowthRule rule : rules) {

            try {

                issues.addAll(
                        rule.analyze(context)
                );

            } catch (Exception ex) {

                System.err.println(
                        "Growth rule failed: "
                                + rule.getClass().getSimpleName()
                );

                ex.printStackTrace();
            }
        }

        return issues;
    }
}
