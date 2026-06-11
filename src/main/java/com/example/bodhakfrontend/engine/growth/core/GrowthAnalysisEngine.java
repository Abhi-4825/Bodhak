package com.example.bodhakfrontend.engine.growth.core;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Analysis.AnalysisIssue;
import com.example.bodhakfrontend.engine.growth.rules.GrowthRule;

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
