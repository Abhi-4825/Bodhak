package com.example.anuviya.context.db;

import com.example.anuviya.compiler.symbol.SymbolId;
import com.example.anuviya.ir.NodeId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps AST/IR reference NodeIds to their resolved SymbolIds.
 */
public class BindingTable {
    private final Map<NodeId, SymbolId> bindings = new ConcurrentHashMap<>();

    public void bind(NodeId nodeId, SymbolId targetSymbolId) {
        if (nodeId == null || targetSymbolId == null) return;
        bindings.put(nodeId, targetSymbolId);
    }

    public SymbolId getBinding(NodeId nodeId) {
        if (nodeId == null) return null;
        return bindings.get(nodeId);
    }

    public void clear() {
        bindings.clear();
    }
}
