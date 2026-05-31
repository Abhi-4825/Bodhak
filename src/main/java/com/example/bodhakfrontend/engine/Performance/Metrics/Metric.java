package com.example.bodhakfrontend.engine.Performance.Metrics;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

public interface Metric<T> {

    double calculate(T target, AnalysisContext context);

}