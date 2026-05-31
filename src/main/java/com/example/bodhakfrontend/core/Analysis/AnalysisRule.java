package com.example.bodhakfrontend.core.Analysis;

import java.util.List;

public interface AnalysisRule {
    List<AnalysisIssue> analyze(AnalysisContext context);
}
