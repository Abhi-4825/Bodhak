package com.example.anuviya.context.state;

import com.example.anuviya.context.AnalysisContext;

@FunctionalInterface
public interface ArchitectureState {
    void update(AnalysisContext context);
}
