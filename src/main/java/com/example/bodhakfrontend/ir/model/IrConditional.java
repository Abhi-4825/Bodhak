package com.example.bodhakfrontend.ir.model;

import java.util.List;

/**
 * Represents any conditional branch (if/else, switch, ternary).
 *
 * <p>{@code body} contains the operations inside the taken branch.
 * The IR merges all branches into a single body; detectors are concerned
 * with whether a costly operation is reachable, not which branch is taken.</p>
 */
public record IrConditional(List<IrOperation> body) implements IrOperation {}
