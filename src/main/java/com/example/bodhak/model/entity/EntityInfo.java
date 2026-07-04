package com.example.bodhak.model.entity;

import java.io.File;
import java.util.Set;

/**
 * Immutable aggregate root representing a semantic code entity in Bodhak.
 */
public final class EntityInfo {
    private final Identity identity;
    private final SourceLocation sourceLocation;
    private final Structure structure;
    private final Relationships relationships;
    private final Metrics metrics;
    private final Documentation documentation;
    private final Contribution contribution;
    private final LanguageMetadata languageMetadata;

    public EntityInfo(
        Identity identity,
        SourceLocation sourceLocation,
        Structure structure,
        Relationships relationships,
        Metrics metrics,
        Documentation documentation,
        Contribution contribution,
        LanguageMetadata languageMetadata
    ) {
        this.identity = identity;
        this.sourceLocation = sourceLocation;
        this.structure = structure;
        this.relationships = relationships;
        this.metrics = metrics;
        this.documentation = documentation;
        this.contribution = contribution;
        this.languageMetadata = languageMetadata;
    }

    public Identity getIdentity() { return identity; }
    public SourceLocation getSourceLocation() { return sourceLocation; }
    public Structure getStructure() { return structure; }
    public Relationships getRelationships() { return relationships; }
    public Metrics getMetrics() { return metrics; }
    public Documentation getDocumentation() { return documentation; }
    public Contribution getContribution() { return contribution; }
    public LanguageMetadata getLanguageMetadata() { return languageMetadata; }

    // ── Compatibility Getters ────────────────────────────────────────────────
    
    public String getEntityName() { return identity.entityName(); }
    
    public String getSimpleName() { return identity.simpleName(); }
    
    public String getNamespaceName() { return identity.namespaceName(); }
    
    public EntityKind getKind() { return identity.kind(); }
    
    public String getLanguage() { return identity.languageId(); }
    
    public File getSourceFile() { return sourceLocation.sourceFile(); }
    
    public int getLinesOfCode() { return metrics.linesOfCode(); }
    
    public int getBeginLine() { return sourceLocation.beginLine(); }
    
    public int getBeginColumn() { return sourceLocation.beginColumn(); }
    
    public Set<String> getFields() { return structure.fields(); }
    
    public Set<String> getDecorators() { return documentation.decorators(); }
    
    public Set<String> getDependsOn() { return relationships.dependsOn(); }
    
    public Set<String> getUsedBy() { return relationships.usedBy(); }
    
    public Set<Set<String>> getCircularGroups() { return relationships.circularGroups(); }
    
    public boolean isAbstract() { return identity.modifiers().contains(ModifierKind.ABSTRACT); }
    
    public boolean isFinal() { return identity.modifiers().contains(ModifierKind.FINAL); }
    
    public boolean isPublic() { return identity.modifiers().contains(ModifierKind.PUBLIC); }
    
    public boolean isStatic() { return identity.modifiers().contains(ModifierKind.STATIC); }

    public long getMethodCount() { return metrics.methodCount(); }
    
    public long getConstructorCount() { return metrics.constructorCount(); }
    
    @Override
    public String toString() { return getSimpleName(); }
}
