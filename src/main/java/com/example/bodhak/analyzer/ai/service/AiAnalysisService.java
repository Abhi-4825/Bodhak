package com.example.bodhak.analyzer.ai.service;

import com.example.bodhak.context.AnalysisContext;

import com.example.bodhak.analyzer.ai.event.AnalysisProgressListener;

public interface AiAnalysisService {
    String analyze(AnalysisContext context, String model, AnalysisProgressListener eventLogger) throws Exception;
}
