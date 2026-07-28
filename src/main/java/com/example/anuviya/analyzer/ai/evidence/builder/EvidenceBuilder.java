package com.example.anuviya.analyzer.ai.evidence.builder;

import com.example.anuviya.context.AnalysisContext;

import com.example.anuviya.analyzer.ai.event.AnalysisProgressListener;

public interface EvidenceBuilder<T> {
    public T build(AnalysisContext context, AnalysisProgressListener listener);
}
