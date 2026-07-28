package com.example.anuviya.model.reference.payload;

/**
 * Payload for field reads and writes.
 */
public record FieldAccessPayload(
    String fieldName, 
    boolean isWrite
) implements ReferencePayload {}
