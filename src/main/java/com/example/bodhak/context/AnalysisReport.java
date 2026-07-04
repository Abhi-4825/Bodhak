package com.example.bodhak.context;
import com.example.bodhak.model.diagnostic.AnalysisSeverity;
import com.example.bodhak.model.diagnostic.AnalysisIssue;

import java.util.List;

public class AnalysisReport {

    private final List<AnalysisIssue> issues;

    public AnalysisReport(List<AnalysisIssue> issues) {
        this.issues = issues;
    }

    public List<AnalysisIssue> getIssues() {
        return issues;
    }

    public int getTotalIssues() {
        return issues.size();
    }

    public long getCriticalIssuesCount() {
        return issues.stream()
                .filter(issue ->
                        issue.getSeverity() == AnalysisSeverity.CRITICAL)
                .count();
    }

    public long getHighIssuesCount() {
        return issues.stream()
                .filter(issue ->
                        issue.getSeverity() == AnalysisSeverity.HIGH)
                .count();
    }

    public long getMediumIssuesCount() {
        return issues.stream()
                .filter(issue ->
                        issue.getSeverity() == AnalysisSeverity.MEDIUM)
                .count();
    }

    public long getLowIssuesCount() {
        return issues.stream()
                .filter(issue ->
                        issue.getSeverity() == AnalysisSeverity.LOW)
                .count();
    }
}
