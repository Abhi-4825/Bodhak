package com.example.anuviya.compiler.symbol;

/**
 * Represents a module symbol.
 */
public record ModuleSymbol(SymbolId id, String name) implements Symbol {
    @Override
    public SymbolKind kind() {
        return SymbolKind.MODULE;
    }
}
