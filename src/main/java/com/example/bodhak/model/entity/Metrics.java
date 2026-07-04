package com.example.bodhak.model.entity;

/**
 * Code quality and size metrics calculated directly from the IR.
 */
public record Metrics(
    int linesOfCode,
    int cyclomaticComplexity,
    int nestingDepth,
    int parameterCount,
    int cognitiveComplexity,
    long methodCount,
    long constructorCount
) {}
