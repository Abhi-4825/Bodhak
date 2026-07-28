package com.example.anuviya.model.runtime;

/**
 * Immutable descriptor constructed by the compiler pipeline for authentication.
 */
public record AuthenticationDescriptor(
        boolean required,
        AuthenticationProfile profile
) {
}
