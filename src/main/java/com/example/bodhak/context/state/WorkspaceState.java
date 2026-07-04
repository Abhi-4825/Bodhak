package com.example.bodhak.context.state;

import com.example.bodhak.context.AnalysisContext;

@FunctionalInterface
public interface WorkspaceState {
    void update(AnalysisContext context);
}
