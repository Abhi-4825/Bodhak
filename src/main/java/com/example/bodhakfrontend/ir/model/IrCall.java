package com.example.bodhakfrontend.ir.model;

/**
 * Represents any method/function invocation.
 *
 * <p>{@code target} is the method name (or "scope.method" if resolvable)
 * purely for diagnostic output — detectors should rely on {@link CallType}.</p>
 *
 * <p>{@link CallType} classifies the semantic nature of the call so that
 * detectors can check for DATABASE or NETWORK calls inside loops without
 * knowing the originating language.</p>
 */
public record IrCall(String target, CallType callType) implements IrOperation {}
