package com.example.anuviya.ir;

/**
 * Represents a stable, unique identifier for an IR node.
 */
public record NodeId(
    String compilationUnitId,
    long sequenceId
) {}
