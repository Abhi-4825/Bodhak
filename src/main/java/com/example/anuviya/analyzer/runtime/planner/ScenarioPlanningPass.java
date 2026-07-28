package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.endpoint.ServiceEndpoint;
import com.example.anuviya.model.runtime.RuntimeFacts;
import com.example.anuviya.model.runtime.ScenarioDefinition;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage pass to define workflows and execution scenarios matching the API service structure.
 */
public class ScenarioPlanningPass implements RuntimePlanningPass {
    @Override
    public void execute(RuntimePlanningContext context, RuntimeFacts facts) {
        List<ServiceEndpoint> endpoints = context.getServiceEndpoints();
        if (endpoints.isEmpty()) {
            return;
        }

        List<String> getPaths = new ArrayList<>();
        List<String> crudSequence = new ArrayList<>();

        for (ServiceEndpoint ep : endpoints) {
            String spec = ep.httpMethod() + " " + ep.fullPath();
            if ("GET".equalsIgnoreCase(ep.httpMethod())) {
                getPaths.add(spec);
            }
            crudSequence.add(spec);
        }

        if (!getPaths.isEmpty()) {
            context.addScenarioDefinition(new ScenarioDefinition(
                    "API Surface Verification",
                    "Sequence executing all discovered GET endpoints to verify API accessibility.",
                    getPaths
            ));
        }

        if (!crudSequence.isEmpty()) {
            context.addScenarioDefinition(new ScenarioDefinition(
                    "Complete API Discovery Sequence",
                    "Simulates random multi-user request execution across all service endpoints.",
                    crudSequence
            ));
        }
    }
}
