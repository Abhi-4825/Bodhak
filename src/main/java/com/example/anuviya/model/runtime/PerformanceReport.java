package com.example.anuviya.model.runtime;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable report representing the parsed performance metrics for the execution run.
 */
public record PerformanceReport(
        String executionId,
        Instant testedAt,
        Map<String, LatencyMetrics> latencyByEndpoint,
        Map<String, ThroughputMetrics> throughputByEndpoint,
        Map<String, ErrorMetrics> errorsByEndpoint,
        String reportPath
) {
    public record LatencyMetrics(double p50, double p95, double p99, double mean) {}
    public record ThroughputMetrics(double requestsPerSecond) {}
    public record ErrorMetrics(double ratePercentage, Map<Integer, Integer> errorCountsByStatus) {}
}
