package com.example.bodhakfrontend.ir.model;

/**
 * Semantic classification of a method call.
 *
 * <p>Used by {@link IrCall} to describe the nature of the invocation,
 * independent of the originating language or framework.</p>
 *
 * <p>Classification is performed heuristically by each {@code IrBuilder}
 * implementation based on known type/method name patterns.</p>
 */
public enum CallType {
    /** Intra-project or intra-module call with no I/O side effect. */
    LOCAL,
    /** Call to a database driver, ORM, or repository abstraction. */
    DATABASE,
    /** Call to an HTTP client, messaging client, or RPC stub. */
    NETWORK,
    /** Call to a file system API. */
    FILE_IO,
    /** Call to an in-process or remote cache (Redis, Caffeine, …). */
    CACHE,
    /** Could not be classified with available heuristics. */
    UNKNOWN
}
