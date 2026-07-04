package com.example.bodhak.compiler.symbol;

/**
 * Represents an entity member symbol (method, field, constructor, etc.).
 */
public record MemberSymbol(SymbolId id, EntitySymbol parent, String name, SymbolKind kind) implements Symbol {}
