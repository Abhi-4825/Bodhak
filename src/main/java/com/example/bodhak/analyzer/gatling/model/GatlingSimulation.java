package com.example.bodhak.analyzer.gatling.model;
import com.example.bodhak.model.endpoint.ServiceEndpoint;

/**
 * Bodhak's internal abstract representation of a Gatling simulation.
 *
 * This is the key abstraction layer between the Bodhak domain model
 * and the Gatling-specific Scala script. Nothing downstream of this
 * (GatlingScriptWriter, GatlingRunner) needs to understand ServiceEndpoint.
 *
 * V2: Add optional AuthConfig field here to support JWT headers.
 * V3: Add optional RequestBody field here to support POST payloads.
 *
 * @param scenarioName  Unique name for this simulation (used in Scala class name)
 * @param baseUrl       The host + port of the target server (e.g., "http://localhost:8080")
 * @param fullPath      The endpoint path (e.g., "/api/v1/users")
 * @param httpMethod    The HTTP verb (V1 = "GET" only)
 * @param configuration The load profile
 */
public record GatlingSimulation(
        String scenarioName,
        String baseUrl,
        String fullPath,
        String httpMethod,
        LoadTestConfiguration configuration
) {
    public GatlingSimulation {
        if (scenarioName == null || scenarioName.isBlank()) throw new IllegalArgumentException("scenarioName must not be blank");
        if (baseUrl == null || baseUrl.isBlank())           throw new IllegalArgumentException("baseUrl must not be blank");
        if (fullPath == null || fullPath.isBlank())         throw new IllegalArgumentException("fullPath must not be blank");
        if (httpMethod == null || httpMethod.isBlank())     throw new IllegalArgumentException("httpMethod must not be blank");
        if (configuration == null)                          throw new IllegalArgumentException("configuration must not be null");
    }
}
