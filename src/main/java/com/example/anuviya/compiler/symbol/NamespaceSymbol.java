package com.example.anuviya.compiler.symbol;

/**
 * Represents a namespace symbol.
 */
public record NamespaceSymbol(SymbolId id, String name) implements Symbol {
    @Override
    public SymbolKind kind() {
        return SymbolKind.NAMESPACE;
    }
}
