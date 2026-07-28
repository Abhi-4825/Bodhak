package com.example.anuviya.model.reference;

import com.example.anuviya.compiler.symbol.SymbolId;

/**
 * Descriptive query constraints to search semantic references in the database.
 */
public record ReferenceQuery(
    SymbolId sourceId,
    SymbolId targetId,
    ReferenceKind kind,
    SemanticRole role
) {
    public boolean matches(SemanticReference ref) {
        if (sourceId != null && !ref.sourceSymbol().id().equals(sourceId)) return false;
        if (targetId != null && !ref.targetSymbol().id().equals(targetId)) return false;
        if (kind != null && ref.kind() != kind) return false;
        if (role != null && ref.role() != role) return false;
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SymbolId sourceId;
        private SymbolId targetId;
        private ReferenceKind kind;
        private SemanticRole role;

        public Builder from(SymbolId sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder to(SymbolId targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder kind(ReferenceKind kind) {
            this.kind = kind;
            return this;
        }

        public Builder role(SemanticRole role) {
            this.role = role;
            return this;
        }

        public ReferenceQuery build() {
            return new ReferenceQuery(sourceId, targetId, kind, role);
        }
    }
}
