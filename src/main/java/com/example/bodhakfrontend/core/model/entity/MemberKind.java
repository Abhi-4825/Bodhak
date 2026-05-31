package com.example.bodhakfrontend.core.model.entity;

/**
 * Distinguishes what kind of member an entity has.
 * Used by MemberInfo to unify methods, constructors, and functions.
 */
public enum MemberKind {
    /** A regular instance/static method (Java, Kotlin). */
    METHOD,
    /** A constructor / __init__ (Java, Kotlin). */
    CONSTRUCTOR,
    /** A top-level or class-level function (Python). */
    FUNCTION,
    /** A property / field-like member with accessor logic. */
    PROPERTY
}
