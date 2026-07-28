package com.example.anuviya.metrics;

import com.example.anuviya.context.AnalysisContext;

public interface Metric<T> {

    double calculate(T target, AnalysisContext context);

}
