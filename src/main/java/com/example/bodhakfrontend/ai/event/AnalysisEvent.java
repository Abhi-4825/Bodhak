package com.example.bodhakfrontend.ai.event;

public record AnalysisEvent(long timestamp, AnalysisEventType type, String title, String details) {
}
