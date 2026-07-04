package com.example.bodhak.ir;

/**
 * Represents a stable, unique identifier for an IR node.
 */
public record NodeId(
    String compilationUnitId,
    long sequenceId
) {}
