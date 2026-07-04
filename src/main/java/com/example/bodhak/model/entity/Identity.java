package com.example.bodhak.model.entity;

import java.util.Set;

/**
 * Unique identifiers and language origin metadata for an entity.
 */
public record Identity(
    String entityName,
    String simpleName,
    String namespaceName,
    EntityKind kind,
    String languageId,
    Set<ModifierKind> modifiers
) {}
