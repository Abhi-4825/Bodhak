package com.example.bodhakfrontend.core.gatling.model;

import com.example.bodhakfrontend.core.api.model.ServiceEndpoint;

/**
 * Pairs a specific endpoint with the load test parameters the user chose.
 *
 * This is the input object handed to GatlingScenarioGenerator.
 * Keeping endpoint and configuration separate lets them evolve independently
 * (e.g. V2 adds auth metadata without touching LoadTestConfiguration).
 *
 * @param endpoint      The discovered HTTP endpoint to test
 * @param configuration The load profile (users, ramp-up, duration)
 */
public record LoadTestRequest(
        ServiceEndpoint endpoint,
        LoadTestConfiguration configuration
) {
    public LoadTestRequest {
        if (endpoint == null)      throw new IllegalArgumentException("endpoint must not be null");
        if (configuration == null) throw new IllegalArgumentException("configuration must not be null");
    }
}
