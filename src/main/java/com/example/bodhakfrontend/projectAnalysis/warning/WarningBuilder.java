package com.example.bodhakfrontend.projectAnalysis.warning;


import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.Models.IssueType;
import com.example.bodhakfrontend.Models.WarningRule;

import java.util.List;
import java.util.Set;

public class WarningBuilder {

    private final WarningRuleStore store = new WarningRuleStore();

    public List<WarningRule> buildWarnings(ClassInfo health) {

        Set<IssueType> issues = health.getIssueType();

        return store.getRules()
                .stream()
                .filter(rule -> WarningMatcher.matches(rule, issues))
                .toList();
    }
}

