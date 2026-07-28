package com.example.anuviya.model.reference;

/**
 * Descriptive properties of a semantic reference.
 */
public record ReferenceCharacteristics(
    boolean isCompileTime,
    boolean isRuntime,
    boolean isTransitive,
    boolean isOptional
) {}
