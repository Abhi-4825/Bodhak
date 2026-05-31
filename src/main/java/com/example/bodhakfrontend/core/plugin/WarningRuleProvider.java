package com.example.bodhakfrontend.core.plugin;

import com.example.bodhakfrontend.core.model.warning.WarningRule;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;

import java.util.List;

public interface WarningRuleProvider {
    List<WarningRule> getRules();

    List<WarningRule> evaluate(EntityInfo entity);
}
