package com.example.anuviya.model.runtime;

import java.util.Map;

/**
 * Immutable execution configuration constructed just-in-time from RuntimeValidationPlan and user inputs.
 */
public record ExecutionPlan(
        String loadProfileId,
        int concurrency,
        int durationSeconds,
        int rampUpSeconds,
        Map<String, String> headers,
        Map<String, String> environment,
        int maxRetries
) {
}
