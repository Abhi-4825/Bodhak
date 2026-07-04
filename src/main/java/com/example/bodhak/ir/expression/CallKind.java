package com.example.bodhak.ir.expression;

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
