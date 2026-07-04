package com.example.bodhak.compiler;
import com.example.bodhak.model.diagnostic.DiagnosticMessage;

import com.example.bodhak.ir.IRNode;
import com.example.bodhak.ir.declaration.ImportDeclaration;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.entity.Relationships;
import com.example.bodhak.model.entity.Metrics;
import com.example.bodhak.model.entity.LanguageMetadata;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Models the compilation context and analytical results for a single source file.
 */
public final class CompilationUnit {
    private final Path filePath;
    private final IRNode intermediateRepresentation;
    private final List<EntityInfo> entities = new ArrayList<>();
    private final List<ImportDeclaration> imports = new ArrayList<>();
    private Relationships resolvedRelations;
    private Metrics aggregatedMetrics;
    private final List<DiagnosticMessage> diagnostics = new ArrayList<>();
    private LanguageMetadata languageMetadata;

    public CompilationUnit(Path filePath, IRNode intermediateRepresentation) {
        this.filePath = filePath;
        this.intermediateRepresentation = intermediateRepresentation;
    }

    public Path getFilePath() {
        return filePath;
    }

    public IRNode getIntermediateRepresentation() {
        return intermediateRepresentation;
    }
    
    public List<EntityInfo> getEntities() {
        return entities;
    }
    
    public void addEntity(EntityInfo entity) {
        this.entities.add(entity);
    }
    
    public List<ImportDeclaration> getImports() {
        return imports;
    }
    
    public void addImport(ImportDeclaration imp) {
        this.imports.add(imp);
    }

    public Relationships getResolvedRelations() {
        return resolvedRelations;
    }

    public void setResolvedRelations(Relationships resolvedRelations) {
        this.resolvedRelations = resolvedRelations;
    }

    public Metrics getAggregatedMetrics() {
        return aggregatedMetrics;
    }

    public void setAggregatedMetrics(Metrics aggregatedMetrics) {
        this.aggregatedMetrics = aggregatedMetrics;
    }

    public List<DiagnosticMessage> getDiagnostics() {
        return diagnostics;
    }

    public void addDiagnostic(DiagnosticMessage diag) {
        this.diagnostics.add(diag);
    }

    public LanguageMetadata getLanguageMetadata() {
        return languageMetadata;
    }

    public void setLanguageMetadata(LanguageMetadata languageMetadata) {
        this.languageMetadata = languageMetadata;
    }
}
