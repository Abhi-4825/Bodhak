package com.example.bodhakfrontend.Models;


import com.example.bodhakfrontend.IncrementalPart.model.Class.IssueType;

import java.util.Set;

public class FixSuggestion {
private Set<IssueType> requiredIssues;
private Set<IssueType> forbiddenIssues;
private Severity severity;
private String title;
private String description;
private String example;
public FixSuggestion(){}

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Set<IssueType> getForbiddenIssues() {
        return forbiddenIssues;
    }

    public void setForbiddenIssues(Set<IssueType> forbiddenIssues) {
        this.forbiddenIssues = forbiddenIssues;
    }

    public Set<IssueType> getRequiredIssues() {
        return requiredIssues;
    }

    public void setRequiredIssues(Set<IssueType> requiredIssues) {
        this.requiredIssues = requiredIssues;
    }
}
