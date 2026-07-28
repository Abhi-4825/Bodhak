package com.example.anuviya.quality.warning;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.quality.flag.EntityCharacteristics;
import com.example.anuviya.quality.flag.EntityFlag;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.diagnostic.WarningRule;

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

