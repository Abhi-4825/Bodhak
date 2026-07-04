package com.example.bodhak.analyzer.gatling.generator;

import com.example.bodhak.analyzer.gatling.model.GatlingSimulation;
import com.example.bodhak.analyzer.gatling.model.LoadTestRequest;

/**
 * Converts a LoadTestRequest into a Bodhak-internal GatlingSimulation model.
 *
 * This is the first stage of the pipeline:
 *   LoadTestRequest → GatlingSimulation
 *
 * V1 Limitation: Only GET endpoints are supported.
 * V2: Extend to include auth config, request headers.
 * V3: Extend to include request body for POST/PUT.
 */
public class GatlingScenarioGenerator {

    /**
     * Generates an internal GatlingSimulation representation from the user request.
     *
     * @param request The user's chosen endpoint + load profile
     * @param baseUrl The target server URL (e.g. "http://localhost:8080")
     * @return A GatlingSimulation ready to be converted to a Scala script
     * @throws UnsupportedOperationException if the endpoint method is not GET (V1 restriction)
     */
    public GatlingSimulation generate(LoadTestRequest request, String baseUrl) {
        // V1: Only GET is supported
        if (!"GET".equalsIgnoreCase(request.endpoint().httpMethod())) {
            throw new UnsupportedOperationException(
                    "Bodhak Gatling V1 only supports GET endpoints. " +
                    "Support for " + request.endpoint().httpMethod() + " is planned for V3.");
        }

        // Sanitize the scenario name to be a valid Scala class identifier
        String rawName = request.endpoint().fullPath()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        String scenarioName = "BodhakSim_" + rawName + "_" + System.currentTimeMillis();

        return new GatlingSimulation(
                scenarioName,
                baseUrl.stripTrailing(),
                request.endpoint().fullPath(),
                request.endpoint().httpMethod().toUpperCase(),
                request.configuration()
        );
    }
}
