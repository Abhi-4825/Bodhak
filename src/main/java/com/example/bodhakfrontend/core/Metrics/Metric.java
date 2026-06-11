package com.example.bodhakfrontend.core.Metrics;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

public interface Metric<T> {

    double calculate(T target, AnalysisContext context);

}