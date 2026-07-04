package com.example.bodhak.model.reference;

/**
 * Sealed contract for role categories defining references.
 */
public sealed interface SemanticRole permits
    SemanticRole.TypeReferenceRole,
    SemanticRole.MemberReferenceRole,
    SemanticRole.CallReferenceRole,
    SemanticRole.ImportReferenceRole,
    SemanticRole.AnnotationReferenceRole,
    SemanticRole.ControlFlowRole,
    SemanticRole.DataFlowRole,
    SemanticRole.ConcurrencyRole,
    SemanticRole.FrameworkRole {

    enum TypeReferenceRole implements SemanticRole {
        SUBTYPE,
        CONTRACT,
        PROTOCOL,
        TRAIT
    }

    enum MemberReferenceRole implements SemanticRole {
        FIELD,
        PROPERTY,
        PARAMETER,
        RETURN,
        GENERIC,
        LOCAL
    }

    enum CallReferenceRole implements SemanticRole {
        INVOCATION,
        CONSTRUCTION,
        STATIC_CALL,
        DYNAMIC_CALL
    }

    enum ImportReferenceRole implements SemanticRole {
        MODULE_IMPORT,
        NAMESPACE_IMPORT
    }

    enum AnnotationReferenceRole implements SemanticRole {
        DECORATED_BY
    }

    enum ControlFlowRole implements SemanticRole {
        THROWS,
        CATCHES
    }

    enum DataFlowRole implements SemanticRole {
        USES_AS_LOCAL,
        LAMBDA_CAPTURE,
        READS,
        WRITES
    }

    enum ConcurrencyRole implements SemanticRole {
        SPAWNS_TASK,
        WRITES_CHANNEL
    }

    enum FrameworkRole implements SemanticRole {
        INJECTED_BY
    }
}
