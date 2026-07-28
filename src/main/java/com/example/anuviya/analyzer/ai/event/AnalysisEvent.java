package com.example.anuviya.analyzer.ai.event;

public record AnalysisEvent(long timestamp, AnalysisEventType type, String title, String details) {
}
