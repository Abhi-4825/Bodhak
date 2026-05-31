package com.example.bodhakfrontend.core.model.warning;

import com.example.bodhakfrontend.core.model.entity.IssueType;
import java.util.List;

/**
 * A fix suggestion for a specific set of architectural issues.
 */
public class FixSuggestion {
    private List<IssueType> requiredIssues;
    private List<IssueType> forbiddenIssues;
    private Severity severity;
    private String title;
    private String description;
    private String example;

    public FixSuggestion() {} // For Jackson

    public FixSuggestion(List<IssueType> requiredIssues, List<IssueType> forbiddenIssues, 
                         Severity severity, String title, String description, String example) {
        this.requiredIssues = requiredIssues;
        this.forbiddenIssues = forbiddenIssues;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.example = example;
    }

    public List<IssueType> getRequiredIssues() { return requiredIssues; }
    public List<IssueType> getForbiddenIssues() { return forbiddenIssues; }
    public Severity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getExample() { return example; }

    public void setRequiredIssues(List<IssueType> requiredIssues) { this.requiredIssues = requiredIssues; }
    public void setForbiddenIssues(List<IssueType> forbiddenIssues) { this.forbiddenIssues = forbiddenIssues; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setExample(String example) { this.example = example; }
}
