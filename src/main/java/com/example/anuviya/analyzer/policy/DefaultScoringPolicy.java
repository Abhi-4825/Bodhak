package com.example.anuviya.analyzer.policy;

import com.example.anuviya.model.reference.SemanticReference;
import com.example.anuviya.model.reference.SemanticRole;

/**
 * Standard compiler scoring logic mapping semantic roles to default weights.
 */
public class DefaultScoringPolicy implements ReferenceScoringPolicy {
    @Override
    public double calculateWeight(SemanticReference reference) {
        SemanticRole role = reference.role();
        if (role instanceof SemanticRole.TypeReferenceRole tr) {
            switch (tr) {
                case SUBTYPE: return 5.0;
                case CONTRACT: return 5.0;
                case PROTOCOL: return 5.0;
                case TRAIT: return 4.0;
            }
        }
        if (role instanceof SemanticRole.MemberReferenceRole mr) {
            switch (mr) {
                case FIELD: return 5.0;
                case PROPERTY: return 4.0;
                case PARAMETER: return 3.0;
                case RETURN: return 3.0;
                case GENERIC: return 2.0;
                case LOCAL: return 2.0;
            }
        }
        if (role instanceof SemanticRole.CallReferenceRole cr) {
            switch (cr) {
                case CONSTRUCTION: return 5.0;
                case INVOCATION: return 3.0;
                case STATIC_CALL: return 2.0;
                case DYNAMIC_CALL: return 3.0;
            }
        }
        return 1.0;
    }
}
