package com.example.anuviya.analyzer.runtime.engine;

/**
 * Immutable capabilities contract exposed by concrete execution providers.
 */
public record ExecutionProviderCapabilities(
        boolean streaming,
        boolean distributedExecution,
        boolean resourceMetrics,
        boolean authentication,
        boolean customScenarios,
        boolean assertions
) {
}
