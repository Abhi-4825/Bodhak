package com.example.bodhakfrontend.core.gatling;

import com.example.bodhakfrontend.core.gatling.generator.GatlingScenarioGenerator;
import com.example.bodhakfrontend.core.gatling.generator.GatlingScriptWriter;
import com.example.bodhakfrontend.core.gatling.model.GatlingSimulation;
import com.example.bodhakfrontend.core.gatling.model.LoadTestRequest;
import com.example.bodhakfrontend.core.gatling.model.PerformanceReport;
import com.example.bodhakfrontend.core.gatling.parser.GatlingParseException;
import com.example.bodhakfrontend.core.gatling.parser.GatlingResultParser;
import com.example.bodhakfrontend.core.gatling.runner.GatlingExecutionException;
import com.example.bodhakfrontend.core.gatling.runner.GatlingRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Top-level orchestrator for the Gatling Integration pipeline.
 *
 * Execution flow:
 *   LoadTestRequest
 *       ↓ GatlingScenarioGenerator
 *   GatlingSimulation
 *       ↓ GatlingScriptWriter
 *   Simulation.scala (on disk)
 *       ↓ GatlingRunner
 *   results/ directory
 *       ↓ GatlingResultParser
 *   PerformanceReport
 *
 * This class is the ONLY entry point the UI or any other Bodhak module
 * should ever use for Gatling. All internal components are hidden.
 */
public class GatlingService {

    private final GatlingScenarioGenerator scenarioGenerator;
    private final GatlingScriptWriter scriptWriter;
    private final GatlingRunner runner;
    private final GatlingResultParser resultParser;

    /** Directory where generated Scala scripts are staged before being sent to Gatling. */
    private final Path scriptStagingDir;

    public GatlingService() {
        this(Paths.get(System.getProperty("user.home"), ".bodhak", "gatling-scripts"));
    }

    public GatlingService(Path scriptStagingDir) {
        this.scenarioGenerator = new GatlingScenarioGenerator();
        this.scriptWriter      = new GatlingScriptWriter();
        this.runner            = new GatlingRunner();
        this.resultParser      = new GatlingResultParser();
        this.scriptStagingDir  = scriptStagingDir;
    }

    /**
     * Runs the full Gatling load test pipeline for the given request.
     *
     * NOTE: This is a blocking call. The caller (e.g., JavaFX Task, background thread)
     * is responsible for not invoking this on the UI thread.
     *
     * @param request The endpoint + load profile to test
     * @param baseUrl The base URL of the running application (e.g., "http://localhost:8080")
     * @return A complete PerformanceReport with metrics
     */
    public PerformanceReport runLoadTest(LoadTestRequest request, String baseUrl)
            throws GatlingExecutionException, GatlingParseException, IOException {

        // Stage 1: Generate the internal simulation model
        GatlingSimulation simulation = scenarioGenerator.generate(request, baseUrl);
        System.out.println("[Bodhak Gatling] Scenario generated: " + simulation.scenarioName());

        // Stage 2: Write the Gatling Scala script to disk
        Path scriptFile = scriptWriter.writeScript(simulation, scriptStagingDir);
        System.out.println("[Bodhak Gatling] Script written to: " + scriptFile);

        // Stage 3: Execute Gatling
        Path resultDir = runner.run(scriptFile, simulation.scenarioName());
        System.out.println("[Bodhak Gatling] Results at: " + resultDir);

        // Stage 4: Parse and return the report
        return resultParser.parse(resultDir, request.endpoint(), simulation.scenarioName());
    }
}
