package com.example.bodhakfrontend.Models;


import com.example.bodhakfrontend.Backend.models.Class.IssueType;

import java.util.Set;
public class WarningRule {



    private Set<com.example.bodhakfrontend.Backend.models.Class.IssueType> requiredIssues;
    private Set<com.example.bodhakfrontend.Backend.models.Class.IssueType> forbiddenIssues;
    private Severity severity;
    private String message;

    public WarningRule() {}

    public Set<com.example.bodhakfrontend.Backend.models.Class.IssueType> getRequiredIssues() {
        return requiredIssues;
    }

    public void setRequiredIssues(Set<com.example.bodhakfrontend.Backend.models.Class.IssueType> requiredIssues) {
        this.requiredIssues = requiredIssues;
    }

    public Set<com.example.bodhakfrontend.Backend.models.Class.IssueType> getForbiddenIssues() {
        return forbiddenIssues;
    }

    public void setForbiddenIssues(Set<IssueType> forbiddenIssues) {
        this.forbiddenIssues = forbiddenIssues;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
