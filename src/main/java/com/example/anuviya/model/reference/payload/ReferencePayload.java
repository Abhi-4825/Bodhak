package com.example.anuviya.model.reference.payload;

/**
 * Type-safe metadata payload for references.
 */
public sealed interface ReferencePayload permits
    CallableInvocationPayload,
    CallableParameterPayload,
    AnnotationPayload,
    FieldAccessPayload,
    EmptyPayload {
}
