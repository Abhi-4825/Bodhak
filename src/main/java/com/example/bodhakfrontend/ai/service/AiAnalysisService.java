package com.example.bodhakfrontend.ai.service;

import com.example.bodhakfrontend.core.Analysis.AnalysisContext;

import com.example.bodhakfrontend.ai.event.AnalysisProgressListener;

public interface AiAnalysisService {
    String analyze(AnalysisContext context, String model, AnalysisProgressListener eventLogger) throws Exception;
}
