package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.runtime.RuntimeFacts;
import com.example.anuviya.model.runtime.RuntimeDiagnostics;
import com.example.anuviya.model.runtime.RuntimeValidationPlan;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the pipeline of planner passes to construct structural testing plans and diagnostics.
 */
public class RuntimeValidationPlanner {
    private final List<RuntimePlanningPass> passes = new ArrayList<>();

    public RuntimeValidationPlanner() {
        passes.add(new CapabilityPlanningPass());
        passes.add(new EnvironmentValidationPass());
        passes.add(new AuthenticationPlanningPass());
        passes.add(new ScenarioPlanningPass());
        passes.add(new ExecutionPlanningPass());
    }

    public void addPass(RuntimePlanningPass pass) {
        passes.add(pass);
    }

    public RuntimePlanningResult plan(RuntimeFacts facts) {
        RuntimePlanningContext context = new RuntimePlanningContext();

        for (RuntimePlanningPass pass : passes) {
            pass.execute(context, facts);
        }

        RuntimeValidationPlan plan = new RuntimeValidationPlan(
                context.getServiceEndpoints(),
                context.getAuthenticationDescriptor(),
                context.getScenarioDefinitions()
        );

        RuntimeDiagnostics diagnostics = new RuntimeDiagnostics(context.getDiagnostics());

        return new RuntimePlanningResult(plan, diagnostics, context.isEnabled());
    }

    public record RuntimePlanningResult(
            RuntimeValidationPlan plan,
            RuntimeDiagnostics diagnostics,
            boolean isEnabled
    ) {
    }
}
