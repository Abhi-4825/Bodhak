package com.example.anuviya.compiler.extraction;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.compiler.symbol.*;
import com.example.anuviya.compiler.resolution.NameResolver;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.model.reference.*;
import com.example.anuviya.model.reference.payload.EmptyPayload;
import com.example.anuviya.ir.*;
import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.ir.statement.*;
import com.example.anuviya.ir.expression.*;
import java.util.*;

/**
 * Extracts fields, properties, parameter references, returns, and local variables.
 */
public class DataFlowReferenceExtractor implements ReferenceExtractor {

    private final Set<String> knownEntityNames;

    public DataFlowReferenceExtractor() {
        this.knownEntityNames = Collections.emptySet();
    }

    public DataFlowReferenceExtractor(Set<String> knownEntityNames) {
        this.knownEntityNames = knownEntityNames;
    }

    @Override
    public void extract(IRNode node, SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu) {
        NameResolver resolver = NameResolver.build(cu, knownEntityNames);
        node.accept(new Visitor(symbolTable, database, cu, knownEntityNames, resolver));
    }

    private static class Visitor implements IRVisitor {
        private final SymbolTable symbolTable;
        private final ReferenceDatabase database;
        private final CompilationUnit cu;
        private final Set<String> knownEntityNames;
        private final NameResolver resolver;
        private final Deque<EntitySymbol> entityScope = new ArrayDeque<>();
        private final Deque<MemberSymbol> memberScope = new ArrayDeque<>();
        private String currentNamespace = "";
        private boolean isVisitingSignature = false;

        public Visitor(SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu,
                       Set<String> knownEntityNames, NameResolver resolver) {
            this.symbolTable = symbolTable;
            this.database = database;
            this.cu = cu;
            this.knownEntityNames = knownEntityNames;
            this.resolver = resolver;
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

            node.members().forEach(m -> m.accept(this));

            entityScope.pop();
        }

        @Override
        public void visit(CallableDeclaration node) {
            EntitySymbol currentEntity = null;
            boolean pushed = false;
            if (entityScope.isEmpty()) {
                String fqn = currentNamespace.isEmpty() ? node.name() : currentNamespace + "." + node.name();
                currentEntity = symbolTable.getOrCreateEntity(fqn, SymbolKind.FUNCTION);
                entityScope.push(currentEntity);
                pushed = true;
            } else {
                currentEntity = entityScope.peek();
            }
            
            SymbolKind kind = node.name().equals("<init>") ? SymbolKind.CONSTRUCTOR : SymbolKind.METHOD;
            MemberSymbol memberSymbol = symbolTable.getOrCreateMember(currentEntity, node.name(), kind);
            memberScope.push(memberSymbol);

            // 1. Return type reference
            if (node.returnType() != null && node.returnType().qualifiedName() != null && !node.returnType().qualifiedName().toString().isEmpty()) {
                String resolvedFqn = resolver.resolve(node.returnType().qualifiedName().toString());
                EntitySymbol targetSymbol = symbolTable.getOrCreateEntity(resolvedFqn, SymbolKind.CLASS);
                
                long nodeHash = Objects.hash(cu.getFilePath().toString(), node.sourceRange().startLine(), node.sourceRange().startColumn());
                SymbolId originId = new SymbolId(nodeHash);

                SemanticReference ref = new SemanticReference(
                    currentEntity,
                    targetSymbol,
                    originId,
                    ReferenceKind.MEMBER,
                    SemanticRole.MemberReferenceRole.RETURN,
                    new ReferenceCharacteristics(true, false, false, false),
                    cu.getFilePath().toFile(),
                    node.sourceRange(),
                    new EmptyPayload()
                );
                database.addReference(ref);
                
                node.returnType().accept(this);
            }

            // 2. Parameters (mark signature state)
            isVisitingSignature = true;
            node.parameters().forEach(p -> p.accept(this));
            isVisitingSignature = false;

            // 3. Throws types
            node.throwsTypes().forEach(t -> t.accept(this));

            if (node.body() != null) {
                node.body().accept(this);
            }

            memberScope.pop();
            if (pushed) {
                entityScope.pop();
            }
        }

        @Override
        public void visit(VariableDeclaration node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol sourceEntity = entityScope.peek();
            MemberSymbol sourceMember = memberScope.peek();

            String typeName = node.type() != null ? node.type().qualifiedName().toString() : "";
            String resolvedFqn = resolver.resolve(typeName);
            EntitySymbol targetSymbol = symbolTable.getOrCreateEntity(resolvedFqn, SymbolKind.CLASS);

            long nodeHash = Objects.hash(cu.getFilePath().toString(), node.sourceRange().startLine(), node.sourceRange().startColumn());
            SymbolId originId = new SymbolId(nodeHash);

            SemanticRole role;
            ReferenceKind kind;
            ReferenceCharacteristics chars;

            if (sourceMember == null) {
                // Class/Struct field
                kind = ReferenceKind.MEMBER;
                role = SemanticRole.MemberReferenceRole.FIELD;
                chars = new ReferenceCharacteristics(true, true, false, false);
            } else if (isVisitingSignature) {
                // Method parameter
                kind = ReferenceKind.MEMBER;
                role = SemanticRole.MemberReferenceRole.PARAMETER;
                chars = new ReferenceCharacteristics(true, false, false, false);
            } else {
                // Local variable
                kind = ReferenceKind.DATA_FLOW;
                role = SemanticRole.MemberReferenceRole.LOCAL;
                chars = new ReferenceCharacteristics(true, false, false, false);
            }

            SemanticReference ref = new SemanticReference(
                sourceEntity,
                targetSymbol,
                originId,
                kind,
                role,
                chars,
                cu.getFilePath().toFile(),
                node.sourceRange(),
                new EmptyPayload()
            );
            database.addReference(ref);

            if (node.type() != null) {
                node.type().accept(this);
            }
            if (node.initializer() != null) {
                node.initializer().accept(this);
            }
        }

        @Override
        public void visit(LocalVariableStatement node) {
            if (node.declaration() != null) {
                node.declaration().accept(this);
            }
        }

        @Override
        public void visit(TypeReferenceExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol sourceEntity = entityScope.peek();

            String resolvedFqn = resolver.resolve(node.qualifiedName().toString());
            EntitySymbol targetSymbol = symbolTable.getOrCreateEntity(resolvedFqn, SymbolKind.CLASS);

            long nodeHash = Objects.hash(cu.getFilePath().toString(), node.sourceRange().startLine(), node.sourceRange().startColumn());
            SymbolId originId = new SymbolId(nodeHash);

            SemanticReference ref = new SemanticReference(
                sourceEntity,
                targetSymbol,
                originId,
                ReferenceKind.TYPE,
                SemanticRole.TypeReferenceRole.SUBTYPE,
                new ReferenceCharacteristics(true, false, false, false),
                cu.getFilePath().toFile(),
                node.sourceRange(),
                new EmptyPayload()
            );
            database.addReference(ref);

            node.typeArguments().forEach(ta -> ta.accept(this));
            node.bounds().forEach(b -> b.accept(this));
        }

        @Override
        public void visit(VariableReferenceExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol sourceEntity = entityScope.peek();

            Optional<String> staticImportClass = resolver.resolveStaticMember(node.variableName());
            if (staticImportClass.isPresent()) {
                String fqn = staticImportClass.get();
                EntitySymbol targetSymbol = symbolTable.getOrCreateEntity(fqn, SymbolKind.CLASS);
                
                long nodeHash = Objects.hash(cu.getFilePath().toString(), node.sourceRange().startLine(), node.sourceRange().startColumn());
                SymbolId originId = new SymbolId(nodeHash);

                SemanticReference ref = new SemanticReference(
                    sourceEntity,
                    targetSymbol,
                    originId,
                    ReferenceKind.TYPE,
                    SemanticRole.TypeReferenceRole.SUBTYPE,
                    new ReferenceCharacteristics(true, false, false, false),
                    cu.getFilePath().toFile(),
                    node.sourceRange(),
                    new EmptyPayload()
                );
                database.addReference(ref);
            }
        }
    }
}
