package com.example.bodhak.model.diagnostic;
import com.example.bodhak.context.AnalysisContext;

import java.util.List;

public interface AnalysisRule {
    List<AnalysisIssue> analyze(AnalysisContext context);
}
