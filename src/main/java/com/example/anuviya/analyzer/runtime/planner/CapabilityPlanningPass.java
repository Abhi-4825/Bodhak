package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.runtime.*;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.DiagnosticEntry;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.Severity;

/**
 * Validates project capabilities and restricts runtime testing to supported project archetypes.
 */
public class CapabilityPlanningPass implements RuntimePlanningPass {
    @Override
    public void execute(RuntimePlanningContext context, RuntimeFacts facts) {
        if (!facts.capabilities().contains("REST_API") && !facts.capabilities().contains("WEB_APP")) {
            context.setEnabled(false);
            context.addDiagnostic(new DiagnosticEntry(
                    "INCOMPATIBLE_PROJECT_TYPE",
                    "Runtime Validation is disabled because this project is classified as a Library/Desktop/CLI application.",
                    Severity.CRITICAL
            ));
        } else {
            context.setEnabled(true);
            context.addServiceEndpoints(facts.endpoints());
        }
    }
}
