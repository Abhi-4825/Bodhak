package com.example.bodhakfrontend.sync.events;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.sync.api.UiUpdateEvent;

public record ProjectSummaryChangedEvent(AnalysisContext context) implements UiUpdateEvent {}
