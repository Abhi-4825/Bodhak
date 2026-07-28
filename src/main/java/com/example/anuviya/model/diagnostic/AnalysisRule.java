package com.example.anuviya.model.diagnostic;
import com.example.anuviya.context.AnalysisContext;

import java.util.List;

public interface AnalysisRule {
    List<AnalysisIssue> analyze(AnalysisContext context);
}
