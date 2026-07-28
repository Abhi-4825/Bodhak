package com.example.anuviya.model.entity;

import java.util.Set;

/**
 * Dependency relations, inheritance hierarchy, and cycle groups for an entity.
 */
public record Relationships(
    Set<String> dependsOn,
    Set<String> usedBy,
    Set<String> superTypes,
    Set<Set<String>> circularGroups
) {}
