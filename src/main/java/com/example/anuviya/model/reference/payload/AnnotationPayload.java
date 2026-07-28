package com.example.anuviya.model.reference.payload;

import java.util.Map;

/**
 * Payload for annotations and decorators.
 */
public record AnnotationPayload(
    String annotationName, 
    Map<String, String> values
) implements ReferencePayload {}
