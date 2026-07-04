package com.example.bodhak.compiler.extraction;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.compiler.symbol.*;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.model.reference.*;
import com.example.bodhak.model.reference.payload.AnnotationPayload;
import com.example.bodhak.ir.*;
import com.example.bodhak.ir.declaration.*;
import java.util.*;

/**
 * Extracts decorator metadata, annotations, and framework dependency injection bindings.
 */
public class FrameworkReferenceExtractor implements ReferenceExtractor {

    @Override
    public void extract(IRNode node, SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu) {
        node.accept(new Visitor(symbolTable, database, cu));
    }

    private static class Visitor implements IRVisitor {
        private final SymbolTable symbolTable;
        private final ReferenceDatabase database;
        private final CompilationUnit cu;
        private final Deque<EntitySymbol> entityScope = new ArrayDeque<>();
        private String currentNamespace = "";

        public Visitor(SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu) {
            this.symbolTable = symbolTable;
            this.database = database;
            this.cu = cu;
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            this.currentNamespace = node.name();
        }

        @Override
        public void visit(TypeDeclaration node) {
            String parentFqn = entityScope.isEmpty() ? "" : entityScope.peek().name();
            String fqn = parentFqn.isEmpty() 
                ? (currentNamespace.isEmpty() ? node.name() : currentNamespace + "." + node.name())
                : parentFqn + "." + node.name();

            EntitySymbol entitySymbol = symbolTable.getOrCreateEntity(fqn, SymbolKind.CLASS);
            entityScope.push(entitySymbol);

            // Extract decorators on classes
            for (DecoratorNode dec : node.decorators()) {
                long nodeHash = Objects.hash(cu.getFilePath().toString(), dec.sourceRange().startLine(), dec.sourceRange().startColumn());
                SymbolId originId = new SymbolId(nodeHash);

                // Generic Annotation reference
                EntitySymbol decSymbol = symbolTable.getOrCreateEntity(dec.name(), SymbolKind.CLASS);
                
                SemanticReference annoRef = new SemanticReference(
                    entitySymbol,
                    decSymbol,
                    originId,
                    ReferenceKind.ANNOTATION,
                    SemanticRole.AnnotationReferenceRole.DECORATED_BY,
                    new ReferenceCharacteristics(true, false, false, false),
                    cu.getFilePath().toFile(),
                    dec.sourceRange(),
                    new AnnotationPayload(dec.name(), new HashMap<>())
                );
                database.addReference(annoRef);

                // Framework DI reference check
                if (isDIAnnotation(dec.name())) {
                    SemanticReference diRef = new SemanticReference(
                        entitySymbol,
                        decSymbol,
                        originId,
                        ReferenceKind.FRAMEWORK,
                        SemanticRole.FrameworkRole.INJECTED_BY,
                        new ReferenceCharacteristics(false, true, false, false),
                        cu.getFilePath().toFile(),
                        dec.sourceRange(),
                        new AnnotationPayload(dec.name(), new HashMap<>())
                    );
                    database.addReference(diRef);
                }
            }

            node.members().forEach(m -> m.accept(this));

            entityScope.pop();
        }

        @Override
        public void visit(CallableDeclaration node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol parentEntity = entityScope.peek();

            for (DecoratorNode dec : node.decorators()) {
                long nodeHash = Objects.hash(cu.getFilePath().toString(), dec.sourceRange().startLine(), dec.sourceRange().startColumn());
                SymbolId originId = new SymbolId(nodeHash);

                EntitySymbol decSymbol = symbolTable.getOrCreateEntity(dec.name(), SymbolKind.CLASS);

                SemanticReference annoRef = new SemanticReference(
                    parentEntity,
                    decSymbol,
                    originId,
                    ReferenceKind.ANNOTATION,
                    SemanticRole.AnnotationReferenceRole.DECORATED_BY,
                    new ReferenceCharacteristics(true, false, false, false),
                    cu.getFilePath().toFile(),
                    dec.sourceRange(),
                    new AnnotationPayload(dec.name(), new HashMap<>())
                );
                database.addReference(annoRef);

                if (isDIAnnotation(dec.name())) {
                    SemanticReference diRef = new SemanticReference(
                        parentEntity,
                        decSymbol,
                        originId,
                        ReferenceKind.FRAMEWORK,
                        SemanticRole.FrameworkRole.INJECTED_BY,
                        new ReferenceCharacteristics(false, true, false, false),
                        cu.getFilePath().toFile(),
                        dec.sourceRange(),
                        new AnnotationPayload(dec.name(), new HashMap<>())
                    );
                    database.addReference(diRef);
                }
            }
        }

        private boolean isDIAnnotation(String name) {
            String lower = name.toLowerCase();
            return lower.contains("inject") || lower.contains("autowired") || 
                   lower.contains("component") || lower.contains("service") || 
                   lower.contains("repository") || lower.contains("bean");
        }
    }
}
