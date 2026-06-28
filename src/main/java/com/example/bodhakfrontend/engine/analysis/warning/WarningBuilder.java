package com.example.bodhakfrontend.engine.analysis.warning;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.analysis.entityflag.EntityCharacteristics;
import com.example.bodhakfrontend.core.analysis.entityflag.EntityFlag;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.warning.WarningRule;

import java.util.List;
import java.util.Set;

public class WarningBuilder {

    private final WarningRuleStore store = new WarningRuleStore();

    public List<WarningRule> buildWarnings(EntityInfo entity, AnalysisContext context) {
        Set<EntityFlag> flags = context.findCharacteristics(entity.getEntityName())
                .map(EntityCharacteristics::flags)
                .orElse(Set.of());

        return store.getRules()
                .stream()
                .filter(rule -> WarningMatcher.matches(rule, flags))
                .toList();
    }
}

