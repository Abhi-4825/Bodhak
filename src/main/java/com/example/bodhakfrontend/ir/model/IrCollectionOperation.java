package com.example.bodhakfrontend.ir.model;

/**
 * Represents an operation on a collection (add, remove, contains, iterate, …).
 *
 * <p>Used by detectors to flag costly patterns such as {@code CONTAINS}
 * on a {@code List} inside a loop (O(n²)).</p>
 */
public record IrCollectionOperation(CollectionOperationType operationType) implements IrOperation {}
