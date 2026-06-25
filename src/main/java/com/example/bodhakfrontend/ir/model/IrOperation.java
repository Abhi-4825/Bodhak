package com.example.bodhakfrontend.ir.model;

/**
 * Root of the BIR operation hierarchy.
 *
 * <p>Sealed so that all possible semantic operations are enumerable by the
 * compiler. Detectors use pattern-matching switch over this hierarchy.</p>
 *
 * <p>Adding a new operation type:
 * <ol>
 *   <li>Create a new {@code record Ir… implements IrOperation} in this package.</li>
 *   <li>Add it to the {@code permits} clause below.</li>
 *   <li>Existing builders do NOT need to change — they simply won't emit the new type.</li>
 * </ol>
 * </p>
 */
public sealed interface IrOperation
        permits IrLoop, IrConditional, IrCall, IrAllocation, IrCollectionOperation, IrReturn {}
