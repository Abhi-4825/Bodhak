package com.example.bodhak.event.domain;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.event.UiUpdateEvent;

public record ProjectSummaryChangedEvent(AnalysisContext context) implements UiUpdateEvent {}
