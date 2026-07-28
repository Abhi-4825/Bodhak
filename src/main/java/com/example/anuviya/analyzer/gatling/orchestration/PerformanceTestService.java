package com.example.anuviya.analyzer.gatling.orchestration;

import com.example.anuviya.analyzer.gatling.generator.GatlingScenarioGenerator;
import com.example.anuviya.analyzer.gatling.model.GatlingSimulation;
import com.example.anuviya.analyzer.gatling.model.LoadTestRequest;
import com.example.anuviya.analyzer.gatling.model.PerformanceReport;
import com.example.anuviya.analyzer.gatling.parser.GatlingParseException;
import com.example.anuviya.analyzer.gatling.parser.GatlingResultParser;
import com.example.anuviya.analyzer.gatling.runner.GatlingExecutionException;
import com.example.anuviya.analyzer.gatling.runner.MavenGatlingRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Orchestrates the full Gatling load test pipeline for the UI layer.
 *
 * The UI calls runLoadTest() from a JavaFX Task (background thread).
 * All stdout lines are streamed via the outputListener callback
 * so the UI can display them in real-time in a TextArea.
 *
 * Pipeline:
 *   LoadTestRequest + baseUrl
 *       ↓ GatlingScenarioGenerator
 *   GatlingSimulation
 *       ↓ MavenGatlingRunner (writes temp project + mvn gatling:test)
 *   results/ dir
 *       ↓ GatlingResultParser
 *   PerformanceReport
 */
public class PerformanceTestService {

    private final GatlingScenarioGenerator scenarioGenerator;
    private final MavenGatlingRunner mavenRunner;
    private final GatlingResultParser resultParser;

    public PerformanceTestService() {
        this.scenarioGenerator = new GatlingScenarioGenerator();
        this.mavenRunner       = new MavenGatlingRunner();
        this.resultParser      = new GatlingResultParser();
    }

    /**
     * Runs the full load test pipeline. Call this from a JavaFX Task — it blocks
     * for the entire test duration.
     *
     * @param request        Endpoint + load configuration chosen by the user
     * @param baseUrl        e.g. "http://localhost:8080"
     * @param outputListener Line-by-line callback — forward to Platform.runLater in UI
     * @return Completed PerformanceReport with metrics
     */
    public PerformanceReport runLoadTest(
            LoadTestRequest request,
            String baseUrl,
            Consumer<String> outputListener)
            throws GatlingExecutionException, GatlingParseException, IOException {

        outputListener.accept("⚡ Bodhak Performance Test Starting...");
        outputListener.accept("   Endpoint : " + request.endpoint().httpMethod() + " " + request.endpoint().fullPath());
        outputListener.accept("   Base URL : " + baseUrl);
        outputListener.accept("   Users    : " + request.configuration().virtualUsers());
        outputListener.accept("   Ramp-Up  : " + request.configuration().rampUpSeconds() + "s");
        outputListener.accept("   Duration : " + request.configuration().durationSeconds() + "s");
        outputListener.accept("─".repeat(52));

        // Pre-flight reachability check
        try {
            java.net.URL url = new java.net.URL(baseUrl);
            String host = url.getHost();
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 2000);
            }
        } catch (Exception ex) {
            outputListener.accept("⚠️  PRE-FLIGHT DIAGNOSTICS: TARGET SERVER UNREACHABLE");
            outputListener.accept("   Failed to connect to target application at " + baseUrl);
            outputListener.accept("   Reason: " + ex.getMessage());
            outputListener.accept("   ");
            outputListener.accept("   [ACTION REQUIRED] Ensure your target backend application is running");
            outputListener.accept("   in the background before starting the runtime validation.");
            outputListener.accept("─".repeat(52));
        }

        // Stage 1: Build internal model
        GatlingSimulation simulation = scenarioGenerator.generate(request, baseUrl);

        // Stage 2: Run via Maven (streams output live)
        Path resultDir = mavenRunner.run(simulation, outputListener);

        // Stage 3: Parse results
        outputListener.accept("─".repeat(52));
        outputListener.accept("📊 Parsing results...");
        PerformanceReport report = resultParser.parse(resultDir, request.endpoint(), simulation.scenarioName());

        outputListener.accept("─".repeat(52));
        outputListener.accept("📈 RESULTS:");
        outputListener.accept("   Avg Response Time : " + report.metrics().avgResponseTime() + " ms");
        outputListener.accept("   P95 Response Time : " + report.metrics().p95ResponseTime() + " ms");
        outputListener.accept("   P99 Response Time : " + report.metrics().p99ResponseTime() + " ms");
        outputListener.accept("   Requests / Second : " + String.format("%.2f", report.metrics().requestsPerSecond()));
        outputListener.accept("   Error Rate        : " + String.format("%.2f%%", report.metrics().errorRate() * 100));
        outputListener.accept("   Status            : " + (report.isHealthy() ? "✅ HEALTHY" : "❌ HIGH ERRORS"));
        outputListener.accept("─".repeat(52));

        return report;
    }
}
