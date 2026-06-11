package com.example.bodhakfrontend.core.api.model;

/**
 * Represents a single HTTP endpoint discovered in the source code.
 *
 * @param httpMethod       The HTTP method (e.g. GET, POST, PUT, DELETE)
 * @param fullPath         The complete resolved path (e.g. /api/v1/users/{id})
 * @param sourceEntityName The fully-qualified entity name that defines this endpoint
 */
public record ServiceEndpoint(
        String httpMethod,
        String fullPath,
        String sourceEntityName
) {
}
