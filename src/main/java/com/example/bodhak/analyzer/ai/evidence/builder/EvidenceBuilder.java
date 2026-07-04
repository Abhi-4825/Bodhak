package com.example.bodhak.analyzer.ai.evidence.builder;

import com.example.bodhak.context.AnalysisContext;

import com.example.bodhak.analyzer.ai.event.AnalysisProgressListener;

public interface EvidenceBuilder<T> {
    public T build(AnalysisContext context, AnalysisProgressListener listener);
}
