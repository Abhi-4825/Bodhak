package com.example.bodhakfrontend.languages.python.analyzer;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.warning.Severity;
import com.example.bodhakfrontend.core.model.warning.WarningRule;
import com.example.bodhakfrontend.core.plugin.WarningRuleProvider;

import java.util.ArrayList;
import java.util.List;

public class PythonWarningProvider implements WarningRuleProvider {

    private final List<WarningRule> rules = List.of(
            new WarningRule("PY-001", "God Module", "Module has over 800 lines.", Severity.MEDIUM, "python"),
            new WarningRule("PY-002", "Too Many Top-Level Functions", "Module contains more than 15 top-level functions.", Severity.LOW, "python"),
            new WarningRule("PY-003", "Flask Route Unused", "Flask route defined but unused (check decorators).", Severity.LOW, "python")
    );

    @Override
    public List<WarningRule> getRules() {
        return rules;
    }

    @Override
    public List<WarningRule> evaluate(EntityInfo entity) {
        List<WarningRule> triggered = new ArrayList<>();
        
        if (entity.getKind().name().equals("MODULE") && entity.getLinesOfCode() > 800) {
            triggered.add(rules.get(0));
        }
        
        // This is simplified, count logic would go here.
        
        return triggered;
    }
}
