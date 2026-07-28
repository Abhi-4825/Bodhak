package com.example.anuviya.compiler.symbol;

/**
 * Represents an entity symbol (class, interface, struct, etc.).
 */
public record EntitySymbol(SymbolId id, String name, SymbolKind kind) implements Symbol {}
