package com.example.anuviya.model.runtime;

import java.util.List;

/**
 * Immutable definition of a scenario workflow sequence.
 */
public record ScenarioDefinition(
        String name,
        String description,
        List<String> endpointsSequence
) {
}
