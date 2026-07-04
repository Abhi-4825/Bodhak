package com.example.bodhak.compiler.symbol;

/**
 * Sealed interface representing a compiler-resolved symbol.
 */
public sealed interface Symbol permits
    NamespaceSymbol,
    ModuleSymbol,
    EntitySymbol,
    MemberSymbol {
    SymbolId id();
    String name();
    SymbolKind kind();
}
