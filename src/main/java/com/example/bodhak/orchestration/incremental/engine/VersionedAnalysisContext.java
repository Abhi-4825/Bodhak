package com.example.bodhak.orchestration.incremental.engine;

import com.example.bodhak.context.AnalysisContext;

public record VersionedAnalysisContext(AnalysisContext context, long version) {}
