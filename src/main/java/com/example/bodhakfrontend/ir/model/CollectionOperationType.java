package com.example.bodhakfrontend.ir.model;

/**
 * Semantic classification of a collection operation.
 *
 * <p>Language-independent: covers Java {@code List/Map/Set}, Python lists/dicts,
 * and any future language's equivalent container operations.</p>
 */
public enum CollectionOperationType {
    ADD,
    REMOVE,
    LOOKUP,
    CONTAINS,
    ITERATE
}
