package com.example.bodhakfrontend.ir.model;

/**
 * Represents a return statement or final expression in a callable unit.
 *
 * <p>Useful for control-flow analysis and completeness checks.</p>
 */
public record IrReturn() implements IrOperation {}
