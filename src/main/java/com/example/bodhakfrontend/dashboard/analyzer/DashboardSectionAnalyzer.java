package com.example.bodhakfrontend.dashboard.analyzer;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;

public interface DashboardSectionAnalyzer<T> {

    T analyze(AnalysisContext context);

}
