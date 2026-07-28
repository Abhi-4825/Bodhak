package com.example.anuviya.model.runtime;

import com.example.anuviya.model.endpoint.ServiceEndpoint;
import java.util.List;

/**
 * Immutable structural IR representing target API structures, authentication details, and scenario steps.
 */
public record RuntimeValidationPlan(
        List<ServiceEndpoint> serviceEndpoints,
        AuthenticationDescriptor authenticationDescriptor,
        List<ScenarioDefinition> scenarioDefinitions
) {
}
