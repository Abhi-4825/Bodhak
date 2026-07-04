package com.example.bodhak.model.reference;

/**
 * Categorizes semantic references collected by the compiler.
 */
public enum ReferenceKind {
    TYPE,         // Subtyping, traits, protocols
    MEMBER,       // Field layout, property declarations
    CALL,         // Callable invocations, constructor calls
    IMPORT,       // Package/module/namespace imports
    ANNOTATION,   // Structural decorators or attributes
    CONTROL_FLOW, // Exceptions, return paths
    DATA_FLOW,    // Variables, closure captures, reads/writes
    CONCURRENCY,  // Parallel processing, channel operations
    FRAMEWORK     // Dependency injection bindings
}
