package com.example.bodhakfrontend.ir.model;

import java.util.List;

/**
 * Root of the Bodhak Intermediate Representation (BIR).
 *
 * <p>Represents an entire analysed project as a flat list of IR entities.
 * Language-agnostic: it does not expose Java classes, Python modules,
 * or any other language-specific concept.</p>
 *
 * <p>Future consumers (NestedLoopDetector, DatabaseCallInLoopDetector, …)
 * operate exclusively on this model.</p>
 */
public record IrProject(List<IrEntity> entities) {}
