package com.example.bodhakfrontend.core.model.entity;

/**
 * Raw access/modifier strings normalised across languages.
 * Java:   PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT, SYNCHRONIZED
 * Python: PUBLIC (no underscore), PRIVATE (single underscore), DUNDER (double underscore)
 * Kotlin: OPEN, OVERRIDE, SUSPEND, ...  — add as needed.
 */
public enum ModifierKind {
    PUBLIC,
    PRIVATE,
    PROTECTED,
    PACKAGE_PRIVATE,

    STATIC,
    FINAL,
    ABSTRACT,
    SYNCHRONIZED,
    TRANSIENT,
    VOLATILE,
    NATIVE,

    /** Python async def / Kotlin suspend fun */
    ASYNC,
    /** Kotlin open, Python methods are implicitly open */
    OPEN,
    /** Python @override or Kotlin override */
    OVERRIDE,

    DEFAULT,
    UNKNOWN;

    /** Map a raw string modifier token to a ModifierKind. */
    public static ModifierKind from(String token) {
        return switch (token.toLowerCase()) {
            case "public"           -> PUBLIC;
            case "private"         -> PRIVATE;
            case "protected"       -> PROTECTED;
            case "static"          -> STATIC;
            case "final"           -> FINAL;
            case "abstract"        -> ABSTRACT;
            case "synchronized"    -> SYNCHRONIZED;
            case "transient"       -> TRANSIENT;
            case "volatile"        -> VOLATILE;
            case "native"          -> NATIVE;
            case "async", "suspend"-> ASYNC;
            case "open"            -> OPEN;
            case "override"        -> OVERRIDE;
            case "default"         -> DEFAULT;
            default                -> UNKNOWN;
        };
    }

    /** Bridge for JavaParser's Modifier.Keyword (Java plugin only). */
    public static ModifierKind from(com.github.javaparser.ast.Modifier.Keyword kw) {
        return from(kw.asString());
    }
}
