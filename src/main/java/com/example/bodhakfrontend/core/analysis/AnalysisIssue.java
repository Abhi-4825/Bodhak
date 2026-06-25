package com.example.bodhakfrontend.core.analysis;

import java.util.List;
import java.util.Map;

public class AnalysisIssue {

    private final String title;

    private final String description;

    private final AnalysisSeverity severity;

    private final AnalysisCategory category;

    private final List<String> affectedEntities;

    private final Map<String, Double> metrics;

    private final List<String> suggestions;
    private final Map<String, Object> attributes;

    public AnalysisIssue(
            String title,
            String description,
            AnalysisSeverity severity,
            AnalysisCategory category,
            List<String> affectedEntities,
            Map<String, Double> metrics,
            List<String> suggestions, Map<String, Object> attributes
    ) {

        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.affectedEntities = affectedEntities;
        this.metrics = metrics;
        this.suggestions = suggestions;
        this.attributes = attributes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public AnalysisSeverity getSeverity() {
        return severity;
    }

    public AnalysisCategory getCategory() {
        return category;
    }

    public List<String> getAffectedEntities() {
        return affectedEntities;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}