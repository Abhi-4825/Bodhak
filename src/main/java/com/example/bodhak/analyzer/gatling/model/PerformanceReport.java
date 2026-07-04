package com.example.bodhak.analyzer.gatling.model;

import com.example.bodhak.model.endpoint.ServiceEndpoint;

import java.time.Instant;

/**
 * The final output of a completed load test.
 *
 * Pairs the tested endpoint with its measured performance metrics,
 * plus metadata about when the test ran and where raw results are stored.
 *
 * This is the object consumed by the future Performance Dashboard UI.
 *
 * @param endpoint          The endpoint that was tested
 * @param metrics           The measured performance data
 * @param testRunId         Unique identifier for this specific test run
 * @param testedAt          Timestamp of when the run started
 * @param gatlingResultPath Path to the raw Gatling output directory (for drill-down)
 */
public record PerformanceReport(
        ServiceEndpoint endpoint,
        PerformanceMetrics metrics,
        String testRunId,
        Instant testedAt,
        String gatlingResultPath
) {
    /** Convenience: was this test largely successful? (error rate < 5%) */
    public boolean isHealthy() {
        return metrics.errorRate() < 0.05;
    }
}
