package com.example.bodhak.analyzer.ai.event;

public record AnalysisEvent(long timestamp, AnalysisEventType type, String title, String details) {
}
