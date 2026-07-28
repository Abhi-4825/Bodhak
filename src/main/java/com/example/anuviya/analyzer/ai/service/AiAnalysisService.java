package com.example.anuviya.analyzer.ai.service;

import com.example.anuviya.context.AnalysisContext;

import com.example.anuviya.analyzer.ai.event.AnalysisProgressListener;

public interface AiAnalysisService {
    String analyze(AnalysisContext context, String model, AnalysisProgressListener eventLogger) throws Exception;
}
