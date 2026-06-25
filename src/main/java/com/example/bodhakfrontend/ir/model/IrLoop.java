package com.example.bodhakfrontend.ir.model;

import java.util.List;

/**
 * Represents any loop construct (for, while, do-while, for-each, forEach lambda).
 *
 * <p>{@code nestingDepth} starts at 1 for a top-level loop and increments for
 * each enclosing loop. Detectors use this to flag O(n²) or deeper patterns.</p>
 *
 * <p>{@code body} contains the operations inside the loop body, which may
 * themselves contain nested {@link IrLoop}s.</p>
 */
public record IrLoop(int nestingDepth, List<IrOperation> body) implements IrOperation {}
