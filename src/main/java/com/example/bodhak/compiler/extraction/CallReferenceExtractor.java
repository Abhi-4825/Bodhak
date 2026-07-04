package com.example.bodhak.compiler.extraction;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.compiler.symbol.*;
import com.example.bodhak.compiler.resolution.NameResolver;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.model.reference.*;
import com.example.bodhak.model.reference.payload.CallableInvocationPayload;
import com.example.bodhak.ir.*;
import com.example.bodhak.ir.declaration.*;
import com.example.bodhak.ir.statement.*;
import com.example.bodhak.ir.expression.CallExpression;
import com.example.bodhak.ir.expression.ObjectCreationExpression;
import java.util.*;

/**
 * Extracts method call, constructor execution, and API route references.
 */
public class CallReferenceExtractor implements ReferenceExtractor {

    private final Set<String> knownEntityNames;

    public CallReferenceExtractor() {
        this.knownEntityNames = Collections.emptySet();
    }

    public CallReferenceExtractor(Set<String> knownEntityNames) {
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
            if (entityScope.isEmpty()) return;
            EntitySymbol parentEntity = entityScope.peek();
            
            SymbolKind kind = node.name().equals("<init>") ? SymbolKind.CONSTRUCTOR : SymbolKind.METHOD;
            MemberSymbol memberSymbol = symbolTable.getOrCreateMember(parentEntity, node.name(), kind);
            memberScope.push(memberSymbol);

            if (node.body() != null) {
                node.body().accept(this);
            }

            memberScope.pop();
        }

        @Override
        public void visit(LocalVariableStatement node) {
            if (node.declaration() != null) {
                node.declaration().accept(this);
            }
        }

        @Override
        public void visit(ObjectCreationExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol sourceEntity = entityScope.peek();
            MemberSymbol sourceMember = memberScope.peek();

            String resolvedFqn = resolver.resolve(node.type().qualifiedName().toString());
            EntitySymbol targetEntity = symbolTable.getOrCreateEntity(resolvedFqn, SymbolKind.CLASS);
            MemberSymbol targetMember = symbolTable.getOrCreateMember(targetEntity, "<init>", SymbolKind.CONSTRUCTOR);

            long nodeHash = Objects.hash(cu.getFilePath().toString(), node.sourceRange().startLine(), node.sourceRange().startColumn());
            SymbolId originId = new SymbolId(nodeHash);

            SemanticReference ref = new SemanticReference(
                sourceEntity,
                targetEntity,
                originId,
                ReferenceKind.CALL,
                SemanticRole.CallReferenceRole.CONSTRUCTION,
                new ReferenceCharacteristics(true, true, false, false),
                cu.getFilePath().toFile(),
                node.sourceRange(),
                new CallableInvocationPayload("<init>", Collections.emptyList())
            );
            database.addReference(ref);

            if (node.type() != null) {
                node.type().accept(this);
            }
            node.arguments().forEach(a -> a.accept(this));
        }

        @Override
        public void visit(CallExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol sourceEntity = entityScope.peek();
            MemberSymbol sourceMember = memberScope.peek();

            String calleeName = node.callableName();

            String resolvedFqn = resolver.resolve(calleeName);
            Optional<String> staticClass = resolver.resolveStaticMember(calleeName);
            if (staticClass.isPresent()) {
                resolvedFqn = staticClass.get();
            }
            EntitySymbol targetEntity = symbolTable.getOrCreateEntity(resolvedFqn, SymbolKind.CLASS);

            long nodeHash = Objects.hash(cu.getFilePath().toString(), node.sourceRange().startLine(), node.sourceRange().startColumn());
            SymbolId originId = new SymbolId(nodeHash);

            SemanticReference ref = new SemanticReference(
                sourceEntity,
                targetEntity,
                originId,
                ReferenceKind.CALL,
                SemanticRole.CallReferenceRole.INVOCATION,
                new ReferenceCharacteristics(false, true, false, false),
                cu.getFilePath().toFile(),
                node.sourceRange(),
                new CallableInvocationPayload(calleeName, Collections.emptyList())
            );
            database.addReference(ref);

            if (node.receiver() != null) {
                node.receiver().accept(this);
            }
            node.arguments().forEach(a -> a.accept(this));
        }
    }
}
