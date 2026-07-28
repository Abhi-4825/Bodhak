package com.example.anuviya.orchestration.incremental.engine;

import com.example.anuviya.context.AnalysisContext;

public record VersionedAnalysisContext(AnalysisContext context, long version) {}
