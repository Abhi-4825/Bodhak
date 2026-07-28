package com.example.anuviya.analyzer.gatling.model;

/**
 * The statistical performance data extracted from a Gatling test run.
 *
 * All time values are in milliseconds.
 * requestsPerSecond is the mean throughput across the test duration.
 * errorRate is a fraction between 0.0 and 1.0 (e.g. 0.05 = 5% error rate).
 *
 * These map directly to the values inside Gatling's global_stats.json:
 *   meanResponseTime.total       → avgResponseTime
 *   percentiles3.total           → p95ResponseTime  (Gatling default = 95th)
 *   percentiles4.total           → p99ResponseTime  (Gatling default = 99th)
 *   meanNumberOfRequestsPerSecond.total → requestsPerSecond
 *   numberOfFailedRequests.total / numberOfRequests.total → errorRate
 */
public record PerformanceMetrics(
        long avgResponseTime,
        long p95ResponseTime,
        long p99ResponseTime,
        double requestsPerSecond,
        double errorRate
) {
}
