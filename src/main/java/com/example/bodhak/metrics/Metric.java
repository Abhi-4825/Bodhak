package com.example.bodhak.metrics;

import com.example.bodhak.context.AnalysisContext;

public interface Metric<T> {

    double calculate(T target, AnalysisContext context);

}
