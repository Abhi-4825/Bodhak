package com.example.bodhakfrontend.Models;


import java.util.Set;
public class WarningRule {



    private Set<IssueType> requiredIssues;
    private Set<IssueType> forbiddenIssues;
    private Severity severity;
    private String message;

    public WarningRule() {}

    public Set<IssueType> getRequiredIssues() {
        return requiredIssues;
    }

    public void setRequiredIssues(Set<IssueType> requiredIssues) {
        this.requiredIssues = requiredIssues;
    }

    public Set<IssueType> getForbiddenIssues() {
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
