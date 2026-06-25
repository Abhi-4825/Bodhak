package com.example.bodhakfrontend.ir.model;

import java.util.List;

/**
 * IR representation of a single callable unit (method, function, constructor).
 *
 * <p>Contains a flat-but-nested ordered list of {@link IrOperation}s that
 * describe *what* the method does, not how it was written.</p>
 */
public record IrMethod(String methodName, List<IrOperation> operations) {}
