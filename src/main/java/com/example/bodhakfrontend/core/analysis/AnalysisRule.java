package com.example.bodhakfrontend.core.analysis;

import java.util.List;

public interface AnalysisRule {
    List<AnalysisIssue> analyze(AnalysisContext context);
}
