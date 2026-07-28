package com.example.anuviya.model.runtime;

import java.util.List;

/**
 * Immutable state container for the Runtime Validation subsystem.
 */
public record RuntimeState(
        String lifecycleState, // PLANNING, AUTHENTICATING, GENERATING, EXECUTING, COLLECTING, COMPLETED, FAILED, CANCELLED
        String executionStatus,
        String selectedProviderId,
        String selectedProfileId,
        PerformanceReport latestReport,
        RuntimeDiagnostics diagnostics,
        List<String> historyIds
) {
    public static RuntimeState initial() {
        return new RuntimeState("PLANNING", "Idle", "GATLING", "SMOKE", null, new RuntimeDiagnostics(List.of()), List.of());
    }
}
