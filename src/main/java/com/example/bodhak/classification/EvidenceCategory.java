package com.example.bodhak.classification;

/**
 * Classifies the source of a piece of detection evidence.
 * Used by the scoring engine to weight different evidence types.
 */
public enum EvidenceCategory {
    /** pom.xml, package.json, requirements.txt entries */
    DEPENDENCY,
    /** Build file existence/structure */
    BUILD_FILE,
    /** Decorators / annotations on entities */
    AST_ANNOTATION,
    /** Code patterns (method calls, inheritance) */
    AST_PATTERN,
    /** application.yml, next.config.js, etc. */
    CONFIGURATION,
    /** Directory conventions (pages/, api/, .fxml) */
    FILE_STRUCTURE,
    /** EntityContribution tags from language plugins */
    ENTITY_TAG
}
