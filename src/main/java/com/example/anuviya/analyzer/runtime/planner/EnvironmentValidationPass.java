package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.runtime.RuntimeFacts;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.DiagnosticEntry;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.Severity;
import com.example.anuviya.platform.environment.EnvironmentSnapshot;
import com.example.anuviya.platform.environment.model.NetworkStatus;
import com.example.anuviya.platform.state.PlatformState;

/**
 * Validates the runtime environment context without performing direct I/O.
 * Inspects PlatformState for network status and configuration reachability.
 */
public class EnvironmentValidationPass implements RuntimePlanningPass {
    @Override
    public void execute(RuntimePlanningContext context, RuntimeFacts facts) {
        PlatformState platform = PlatformState.getCurrent();
        if (platform == null) {
            context.addDiagnostic(new DiagnosticEntry(
                    "PLATFORM_STATE_MISSING",
                    "Platform state is uninitialized. Skipping environment network checks.",
                    Severity.WARNING
            ));
            return;
        }

        EnvironmentSnapshot snapshot = platform.getEnvironment();
        if (snapshot == null) {
            context.addDiagnostic(new DiagnosticEntry(
                    "ENVIRONMENT_SNAPSHOT_MISSING",
                    "Environment snapshot is unavailable.",
                    Severity.WARNING
            ));
            return;
        }

        if (snapshot.networkStatus() == NetworkStatus.OFFLINE) {
            context.addDiagnostic(new DiagnosticEntry(
                    "NETWORK_OFFLINE",
                    "The local platform reports offline status. Remote API reachability checks will be skipped.",
                    Severity.WARNING
            ));
        }
    }
}
