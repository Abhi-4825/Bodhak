package com.example.bodhak.ir.expression;

/**
 * Categorizes literals parsed in source code (e.g. String, numeric, boolean, etc.).
 */
public enum LiteralKind {
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    CHAR,
    REGEXP
}
