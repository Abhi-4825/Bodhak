package com.example.bodhak.model.reference.payload;

/**
 * Payload for callable parameters.
 */
public record CallableParameterPayload(
    int position, 
    String parameterName
) implements ReferencePayload {}
