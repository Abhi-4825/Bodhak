package com.example.bodhakfrontend.ir.model;

/**
 * Represents an object or value allocation (new expressions, factory calls).
 *
 * <p>{@code type} is the simple or qualified type name, e.g. {@code ArrayList}
 * or {@code com.example.OrderDto}. Used by allocation-in-loop detectors.</p>
 */
public record IrAllocation(String type) implements IrOperation {}
