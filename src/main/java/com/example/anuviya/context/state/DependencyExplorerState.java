package com.example.anuviya.context.state;

import com.example.anuviya.context.AnalysisContext;

@FunctionalInterface
public interface DependencyExplorerState {
    void update(AnalysisContext context);
}
