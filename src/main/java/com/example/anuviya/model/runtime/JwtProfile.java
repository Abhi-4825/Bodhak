package com.example.anuviya.model.runtime;

/**
 * JWT-based authentication configuration.
 */
public record JwtProfile(
        String loginEndpoint,
        String tokenFieldName,
        String headerFormat // e.g., "Bearer %s"
) implements AuthenticationProfile {

    @Override
    public String getAuthType() {
        return "JWT";
    }
}
