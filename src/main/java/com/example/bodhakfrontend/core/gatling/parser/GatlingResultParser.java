package com.example.bodhakfrontend.core.gatling.parser;

import com.example.bodhakfrontend.core.api.model.ServiceEndpoint;
import com.example.bodhakfrontend.core.gatling.model.PerformanceMetrics;
import com.example.bodhakfrontend.core.gatling.model.PerformanceReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Parses the Gatling global_stats.json output file into a PerformanceReport.
 *
 * Gatling generates this file at:
 *   <results-dir>/js/global_stats.json
 *
 * The JSON structure we rely on (Gatling 3.x):
 * {
 *   "meanResponseTime":  { "total": 120 },
 *   "percentiles3":      { "total": 250 },   // 95th percentile by default
 *   "percentiles4":      { "total": 310 },   // 99th percentile by default
 *   "meanNumberOfRequestsPerSecond": { "total": 45.3 },
 *   "numberOfRequests":  { "total": 5000, "ok": 4950, "ko": 50 }
 * }
 *
 * This class is completely independent of the Gatling Scala DSL,
 * making it reusable if the script generation approach ever changes.
 */
public class GatlingResultParser {

    /**
     * Reads and parses the global_stats.json from a completed Gatling result directory.
     *
     * @param gatlingResultDir The root of the Gatling result directory for this run
     * @param endpoint         The endpoint that was tested (for correlation in the report)
     * @param testRunId        Unique identifier for this run
     * @return A fully populated PerformanceReport
     * @throws IOException              if the result file cannot be read
     * @throws GatlingParseException    if the JSON cannot be parsed
     */
    public PerformanceReport parse(Path gatlingResultDir, ServiceEndpoint endpoint, String testRunId)
            throws IOException, GatlingParseException {

        Path statsFile = gatlingResultDir.resolve("js").resolve("global_stats.json");

        if (!Files.exists(statsFile)) {
            throw new GatlingParseException(
                    "global_stats.json not found at expected location: " + statsFile +
                    ". Ensure Gatling completed successfully and results were not moved.");
        }

        String json = Files.readString(statsFile);
        PerformanceMetrics metrics = extractMetrics(json, statsFile);

        System.out.println("\n=== GATLING PERFORMANCE RESULTS ===");
        System.out.printf("  Endpoint              : %s %s%n", endpoint.httpMethod(), endpoint.fullPath());
        System.out.printf("  Avg Response Time     : %d ms%n",  metrics.avgResponseTime());
        System.out.printf("  P95 Response Time     : %d ms%n",  metrics.p95ResponseTime());
        System.out.printf("  P99 Response Time     : %d ms%n",  metrics.p99ResponseTime());
        System.out.printf("  Requests / Second     : %.2f%n",   metrics.requestsPerSecond());
        System.out.printf("  Error Rate            : %.2f%%%n", metrics.errorRate() * 100);
        System.out.println("=====================================\n");

        return new PerformanceReport(
                endpoint,
                metrics,
                testRunId,
                Instant.now(),
                gatlingResultDir.toAbsolutePath().toString()
        );
    }

    // ── Private Extraction Logic ───────────────────────────────────────────────

    private PerformanceMetrics extractMetrics(String json, Path statsFile) throws GatlingParseException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            long avg = root.path("meanResponseTime").path("total").asLong();
            long p95 = root.path("percentiles3").path("total").asLong();
            long p99 = root.path("percentiles4").path("total").asLong();
            double rps = root.path("meanNumberOfRequestsPerSecond").path("total").asDouble();

            JsonNode requests = root.path("numberOfRequests");
            long total  = requests.path("total").asLong();
            long failed = requests.path("ko").asLong();
            double errorRate = total > 0 ? (double) failed / total : 0.0;

            return new PerformanceMetrics(avg, p95, p99, rps, errorRate);

        } catch (Exception e) {
            throw new GatlingParseException(
                    "Failed to parse Gatling results from: " + statsFile + ". Cause: " + e.getMessage(), e);
        }
    }
}
