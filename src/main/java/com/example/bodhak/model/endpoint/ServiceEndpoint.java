package com.example.bodhak.model.endpoint;

/**
 * Represents a single HTTP endpoint discovered in the source code.
 */
public record ServiceEndpoint(
        String httpMethod,
        String fullPath,
        String sourceEntityName
) {
}
