package com.example.bodhakfrontend.core.model.warning;

import com.example.bodhakfrontend.core.analysis.entityflag.EntityFlag;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * A fix suggestion for a specific set of architectural issues.
 */
public class FixSuggestion {
    @JsonProperty("requiredIssues")
    private List<EntityFlag> requiredIssues;
    @JsonProperty("forbiddenIssues")
    private List<EntityFlag> forbiddenIssues;
    private Severity severity;
    private String title;
    private String description;
    private String example;

    public FixSuggestion() {} // For Jackson

    public FixSuggestion(List<EntityFlag> requiredIssues, List<EntityFlag> forbiddenIssues, 
                         Severity severity, String title, String description, String example) {
        this.requiredIssues = requiredIssues;
        this.forbiddenIssues = forbiddenIssues;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.example = example;
    }

    public List<EntityFlag> getRequiredIssues() { 
        return requiredIssues == null ? List.of() : requiredIssues.stream().filter(java.util.Objects::nonNull).toList(); 
    }
    public List<EntityFlag> getForbiddenIssues() { 
        return forbiddenIssues == null ? List.of() : forbiddenIssues.stream().filter(java.util.Objects::nonNull).toList(); 
    }
    public Severity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getExample() { return example; }

    public void setRequiredIssues(List<EntityFlag> requiredIssues) { this.requiredIssues = requiredIssues; }
    public void setForbiddenIssues(List<EntityFlag> forbiddenIssues) { this.forbiddenIssues = forbiddenIssues; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setExample(String example) { this.example = example; }
}
