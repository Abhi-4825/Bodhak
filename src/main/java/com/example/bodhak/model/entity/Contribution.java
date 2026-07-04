package com.example.bodhak.model.entity;

import java.util.Set;

/**
 * Metadata tags and git contribution stats.
 */
public record Contribution(
    Set<String> tags,
    EntityContribution legacyContribution
) {
    public boolean hasTag(String tag) {
        return tags.contains(tag) || (legacyContribution != null && legacyContribution.hasTag(tag));
    }
    
    public boolean hasMain() {
        return hasTag("has_main");
    }
    
    public boolean isTest() {
        return hasTag("test_class") || hasTag("pytest_test") || (legacyContribution != null && legacyContribution.isTest());
    }
    
    public boolean isFrameworkRoot() {
        return hasTag("framework_root") || (legacyContribution != null && legacyContribution.isFrameworkRoot());
    }
}
