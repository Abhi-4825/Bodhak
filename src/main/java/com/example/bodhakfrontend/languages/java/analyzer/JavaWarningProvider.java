package com.example.bodhakfrontend.languages.java.analyzer;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.MemberKind;
import com.example.bodhakfrontend.core.model.warning.Severity;
import com.example.bodhakfrontend.core.model.warning.UsageStatus;
import com.example.bodhakfrontend.core.model.warning.WarningRule;
import com.example.bodhakfrontend.core.plugin.WarningRuleProvider;

import java.util.ArrayList;
import java.util.List;

public class JavaWarningProvider implements WarningRuleProvider {

    private final List<WarningRule> rules = List.of(
            new WarningRule("JAVA-001", "God Class", "Class has over 500 lines or 20 methods.", Severity.MEDIUM, "java"),
            new WarningRule("JAVA-002", "Anemic Domain Model", "Class has data fields but almost no behavior.", Severity.LOW, "java"),
            new WarningRule("JAVA-003", "Spring Components Unused", "Spring Boot Component is unused, though loaded by context.", Severity.LOW, "java")
    );

    @Override
    public List<WarningRule> getRules() {
        return rules;
    }

    @Override
    public List<WarningRule> evaluate(EntityInfo entity) {
        List<WarningRule> triggered = new ArrayList<>();
        
        long methodCount = entity.getMembers().stream().filter(m -> m.getKind() == MemberKind.METHOD).count();
        if (entity.getLinesOfCode() > 500 || methodCount > 20) {
            triggered.add(rules.get(0));
        }
        
        if (entity.getFields().size() >= 3 && methodCount <= 2 && !entity.getContribution().isTest() && !entity.getContribution().hasTag("jpa_entity")) {
            triggered.add(rules.get(1)); // Anemic Domain Model
        }
        
        return triggered;
    }
}
