package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.runtime.RuntimeFacts;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.DiagnosticEntry;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.Severity;

/**
 * Plans general routing, default port validation, and maps diagnostic requirements for target systems.
 */
public class ExecutionPlanningPass implements RuntimePlanningPass {
    @Override
    public void execute(RuntimePlanningContext context, RuntimeFacts facts) {
        if (facts.port() <= 0) {
            context.addDiagnostic(new DiagnosticEntry(
                    "INVALID_PORT",
                    "Discovered port '" + facts.port() + "' is invalid. Validation will assume standard port 8080.",
                    Severity.WARNING
            ));
        }

        context.setAttribute("targetHost", facts.host() != null ? facts.host() : "localhost");
        context.setAttribute("targetPort", facts.port() > 0 ? facts.port() : 8080);
    }
}
