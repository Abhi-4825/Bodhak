package com.example.bodhak.model.reference.payload;

import java.util.List;

/**
 * Payload for callable invocations.
 */
public record CallableInvocationPayload(
    String callableName, 
    List<String> argumentTypes
) implements ReferencePayload {}
