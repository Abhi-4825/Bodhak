package com.example.anuviya.ir.declaration;

/**
 * Scope of a callable (method, function, etc.).
 */
public enum CallableScope {
    MODULE,   // Python top-level functions, JS module functions
    TYPE,     // Java methods, Python class methods
    LOCAL     // Nested functions (closures), local class methods
}
