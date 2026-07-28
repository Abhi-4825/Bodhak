package com.example.anuviya.ir.expression;

/**
 * Categorizes method/function call dispatch types.
 */
public enum CallKind {
    INSTANCE,
    STATIC,
    CONSTRUCTOR,
    SUPER,
    THIS,
    EXTENSION,
    UNKNOWN
}
