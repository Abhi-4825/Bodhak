package com.example.anuviya.analyzer.gatling;

import com.example.anuviya.model.endpoint.ServiceEndpoint;
import com.example.anuviya.analyzer.gatling.model.LoadTestConfiguration;
import com.example.anuviya.analyzer.gatling.model.LoadTestRequest;
import com.example.anuviya.analyzer.gatling.model.PerformanceReport;

/**
 * Quick manual runner to test the Gatling Integration against a live API.
 *
 * HOW TO USE:
 *   1. Set GATLING_HOME environment variable to your Gatling installation directory.
 *   2. Make sure your target API is running (e.g. http://localhost:8080).
 *   3. Edit the THREE fields below: endpointPath, virtualUsers, durationSeconds.
 *   4. Run this class as a standard Java main method.
 *   5. Watch the console for results.
 */
public class GatlingManualRunner {

    // ─── CONFIGURE THESE THREE VALUES ────────────────────────────────────────

    /** The base URL of your running API. */
    private static final String BASE_URL = "http://localhost:8080";

    /** The endpoint path you want to load test (must be a GET endpoint). */
    private static final String ENDPOINT_PATH = "/api/users";  // <-- change this to your actual path

    /** Load profile: how many virtual users, ramp-up time, and total duration (all in seconds). */
    private static final int VIRTUAL_USERS    = 10;   // start small
    private static final int RAMP_UP_SECONDS  = 5;
    private static final int DURATION_SECONDS = 30;

    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     Bodhak Gatling Load Test Runner  ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
        System.out.println("Target URL  : " + BASE_URL + ENDPOINT_PATH);
        System.out.println("Users       : " + VIRTUAL_USERS);
        System.out.println("Ramp-up     : " + RAMP_UP_SECONDS + "s");
        System.out.println("Duration    : " + DURATION_SECONDS + "s");
        System.out.println();

        try {
            // 1. Build the endpoint and request
            ServiceEndpoint endpoint = new ServiceEndpoint("GET", ENDPOINT_PATH, "ManualTest");
            LoadTestConfiguration config = new LoadTestConfiguration(
                    VIRTUAL_USERS, RAMP_UP_SECONDS, DURATION_SECONDS);
            LoadTestRequest request = new LoadTestRequest(endpoint, config);

            // 2. Run the full pipeline
            GatlingService service = new GatlingService();
            PerformanceReport report = service.runLoadTest(request, BASE_URL);

            // 3. Print the final summary
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║           LOAD TEST COMPLETE                     ║");
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.printf( "║  Endpoint     : %-33s║%n", report.endpoint().httpMethod() + " " + report.endpoint().fullPath());
            System.out.printf( "║  Avg Resp Time: %-30s ms ║%n", report.metrics().avgResponseTime());
            System.out.printf( "║  P95 Resp Time: %-30s ms ║%n", report.metrics().p95ResponseTime());
            System.out.printf( "║  P99 Resp Time: %-30s ms ║%n", report.metrics().p99ResponseTime());
            System.out.printf( "║  Req/Second   : %-33s║%n", String.format("%.2f", report.metrics().requestsPerSecond()));
            System.out.printf( "║  Error Rate   : %-33s║%n", String.format("%.2f%%", report.metrics().errorRate() * 100));
            System.out.printf( "║  Status       : %-33s║%n", report.isHealthy() ? "✅ HEALTHY" : "❌ HIGH ERRORS");
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.printf( "║  Raw Results  : %-33s║%n", "See GATLING_HOME/results/");
            System.out.println("╚══════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ Load test failed: " + e.getMessage());
            System.err.println();
            System.err.println("Common causes:");
            System.err.println("  - GATLING_HOME is not set → set it to your Gatling folder");
            System.err.println("  - API is not running      → start your server on " + BASE_URL);
            System.err.println("  - Wrong endpoint path     → check ENDPOINT_PATH variable");
            e.printStackTrace();
        }
    }
}
