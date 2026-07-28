package com.example.anuviya.context.state;

import com.example.anuviya.context.AnalysisContext;

@FunctionalInterface
public interface WorkspaceState {
    void update(AnalysisContext context);
}
