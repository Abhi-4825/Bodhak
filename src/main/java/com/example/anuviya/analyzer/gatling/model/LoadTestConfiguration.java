package com.example.anuviya.analyzer.gatling.model;

/**
 * Defines the load testing parameters a user specifies before a test run.
 *
 * V1 only supports ramp-up + constant load profiles.
 * V2+ can extend this with closed-model or stepped profiles.
 *
 * @param virtualUsers     Total number of concurrent simulated users
 * @param rampUpSeconds    Duration over which virtual users are gradually injected
 * @param durationSeconds  Total duration the load is sustained (after full ramp-up)
 */
public record LoadTestConfiguration(
        int virtualUsers,
        int rampUpSeconds,
        int durationSeconds
) {
    public LoadTestConfiguration {
        if (virtualUsers < 1)    throw new IllegalArgumentException("virtualUsers must be >= 1");
        if (rampUpSeconds < 0)   throw new IllegalArgumentException("rampUpSeconds must be >= 0");
        if (durationSeconds < 1) throw new IllegalArgumentException("durationSeconds must be >= 1");
    }
}
