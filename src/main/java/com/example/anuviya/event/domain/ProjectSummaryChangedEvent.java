package com.example.anuviya.event.domain;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.event.UiUpdateEvent;

public record ProjectSummaryChangedEvent(AnalysisContext context) implements UiUpdateEvent {}
