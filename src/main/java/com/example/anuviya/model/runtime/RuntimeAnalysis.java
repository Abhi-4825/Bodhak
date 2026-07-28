package com.example.anuviya.model.runtime;

import java.time.Instant;

/**
 * Immutable workspace record containing structural and provider metadata of a completed load execution.
 */
public record RuntimeAnalysis(
        String executionId,
        Instant timestamp,
        String loadProfileId,
        String executionProviderId,
        PerformanceReport performanceReport,
        RuntimeDiagnostics diagnostics
) {
}
