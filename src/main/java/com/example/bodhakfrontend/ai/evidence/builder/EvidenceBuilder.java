package com.example.bodhakfrontend.ai.evidence.builder;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

import com.example.bodhakfrontend.ai.event.AnalysisProgressListener;

public interface EvidenceBuilder<T> {
    public T build(AnalysisContext context, AnalysisProgressListener listener);
}
