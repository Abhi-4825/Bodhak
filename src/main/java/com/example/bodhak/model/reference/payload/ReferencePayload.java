package com.example.bodhak.model.reference.payload;

import java.util.List;
import java.util.Map;

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
