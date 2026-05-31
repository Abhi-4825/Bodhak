package com.example.bodhakfrontend.engine.analysis.warning;


import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.IssueType;
import com.example.bodhakfrontend.core.model.warning.WarningRule;

import java.util.List;
import java.util.Set;

public class WarningBuilder {

    private final WarningRuleStore store = new WarningRuleStore();

    public List<WarningRule> buildWarnings(EntityInfo health) {

        Set<IssueType> issues = health.getIssueType();

        return store.getRules()
                .stream()
                .filter(rule -> WarningMatcher.matches(rule, issues))
                .toList();
    }
}

