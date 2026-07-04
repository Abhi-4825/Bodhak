package com.example.bodhak.context.db;

import com.example.bodhak.model.reference.SemanticReference;
import com.example.bodhak.model.reference.ReferenceKind;
import com.example.bodhak.model.reference.ReferenceQuery;
import com.example.bodhak.compiler.symbol.SymbolId;
import java.util.*;

/**
 * Stores and indexes canonical semantic compiler facts.
 */
public class ReferenceDatabase {
    private final List<SemanticReference> references = new ArrayList<>();
    private final Map<SymbolId, List<SemanticReference>> outgoingIndex = new HashMap<>();
    private final Map<SymbolId, List<SemanticReference>> incomingIndex = new HashMap<>();
    private final Map<ReferenceKind, List<SemanticReference>> kindIndex = new HashMap<>();

    public ReferenceDatabase() {}

    public synchronized void addReference(SemanticReference ref) {
        references.add(ref);
        outgoingIndex.computeIfAbsent(ref.sourceSymbol().id(), k -> new ArrayList<>()).add(ref);
        incomingIndex.computeIfAbsent(ref.targetSymbol().id(), k -> new ArrayList<>()).add(ref);
        kindIndex.computeIfAbsent(ref.kind(), k -> new ArrayList<>()).add(ref);
    }

    public synchronized List<SemanticReference> getAllReferences() {
        return List.copyOf(references);
    }

    public synchronized List<SemanticReference> getOutgoing(SymbolId sourceId) {
        return List.copyOf(outgoingIndex.getOrDefault(sourceId, Collections.emptyList()));
    }

    public synchronized List<SemanticReference> getIncoming(SymbolId targetId) {
        return List.copyOf(incomingIndex.getOrDefault(targetId, Collections.emptyList()));
    }

    public synchronized List<SemanticReference> getByKind(ReferenceKind kind) {
        return List.copyOf(kindIndex.getOrDefault(kind, Collections.emptyList()));
    }

    public synchronized List<SemanticReference> query(ReferenceQuery query) {
        Collection<SemanticReference> searchSpace = references;
        if (query.sourceId() != null) {
            searchSpace = outgoingIndex.getOrDefault(query.sourceId(), Collections.emptyList());
        } else if (query.targetId() != null) {
            searchSpace = incomingIndex.getOrDefault(query.targetId(), Collections.emptyList());
        } else if (query.kind() != null) {
            searchSpace = kindIndex.getOrDefault(query.kind(), Collections.emptyList());
        }

        return searchSpace.stream()
            .filter(query::matches)
            .toList();
    }
}
