package com.example.anuviya.context.state;

import com.example.anuviya.context.AnalysisContext;

@FunctionalInterface
public interface AnalysisReportState {
    void update(AnalysisContext context);
}
