package com.example.bodhak.context.state;

import com.example.bodhak.context.AnalysisContext;

@FunctionalInterface
public interface AnalysisReportState {
    void update(AnalysisContext context);
}
