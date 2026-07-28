package com.example.anuviya.compiler.symbol;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages unique symbol mapping and lookups in the compiler context.
 */
public class SymbolTable {
    private final AtomicLong counter = new AtomicLong(1);
    private final Map<String, Symbol> symbolCache = new ConcurrentHashMap<>();
    private final Map<SymbolId, Symbol> idCache = new ConcurrentHashMap<>();

    public SymbolTable() {}

    private SymbolId nextId() {
        return new SymbolId(counter.getAndIncrement());
    }

    public synchronized NamespaceSymbol getOrCreateNamespace(String name) {
        String key = "ns:" + name;
        return (NamespaceSymbol) symbolCache.computeIfAbsent(key, k -> {
            NamespaceSymbol sym = new NamespaceSymbol(nextId(), name);
            idCache.put(sym.id(), sym);
            return sym;
        });
    }

    public synchronized ModuleSymbol getOrCreateModule(String name) {
        String key = "mod:" + name;
        return (ModuleSymbol) symbolCache.computeIfAbsent(key, k -> {
            ModuleSymbol sym = new ModuleSymbol(nextId(), name);
            idCache.put(sym.id(), sym);
            return sym;
        });
    }

    public synchronized EntitySymbol getOrCreateEntity(String name, SymbolKind kind) {
        String key = "ent:" + name;
        return (EntitySymbol) symbolCache.computeIfAbsent(key, k -> {
            EntitySymbol sym = new EntitySymbol(nextId(), name, kind);
            idCache.put(sym.id(), sym);
            return sym;
        });
    }

    public synchronized MemberSymbol getOrCreateMember(EntitySymbol parent, String name, SymbolKind kind) {
        String key = "mem:" + parent.name() + "#" + name;
        return (MemberSymbol) symbolCache.computeIfAbsent(key, k -> {
            MemberSymbol sym = new MemberSymbol(nextId(), parent, name, kind);
            idCache.put(sym.id(), sym);
            return sym;
        });
    }

    public Symbol getById(SymbolId id) {
        return idCache.get(id);
    }

    public synchronized Collection<Symbol> getAllSymbols() {
        return List.copyOf(idCache.values());
    }
}
