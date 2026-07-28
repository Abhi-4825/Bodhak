package com.example.anuviya.model.entity;

/**
 * Language-neutral kinds of code entities.
 * Each language plugin maps its own constructs onto these values.
 *
 * Java:   CLASS, INTERFACE, ENUM, RECORD
 * Python: CLASS, FUNCTION, MODULE, SCRIPT
 * Kotlin: CLASS, INTERFACE, OBJECT, DATA_CLASS (use CLASS + tag)
 */
public enum EntityKind {
    // ── Universal ────────────────────────────────────────────
    CLASS,
    INTERFACE,
    ENUM,

    // ── Java-specific (supported from day 1) ─────────────────
    RECORD,

    // ── Python-specific ───────────────────────────────────────
    /** A top-level function (not inside a class). */
    FUNCTION,
    /** A Python module (the whole .py file treated as a namespace entity). */
    MODULE,
    /** A file that contains a __main__ guard and acts as a runnable script. */
    SCRIPT,

    // ── Future-proof (add languages without changing this file) ─
    /** Kotlin object declarations, Scala objects, etc. */
    OBJECT,
    /** Rust/Kotlin traits, Scala traits, etc. */
    TRAIT,
    /** Go/Rust structs. */
    STRUCT
}
