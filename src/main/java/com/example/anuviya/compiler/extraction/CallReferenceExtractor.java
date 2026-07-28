package com.example.anuviya.compiler.extraction;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.compiler.symbol.*;
import com.example.anuviya.compiler.resolution.NameResolver;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.model.reference.*;
import com.example.anuviya.model.reference.payload.CallableInvocationPayload;
import com.example.anuviya.ir.*;
import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.ir.statement.*;
import com.example.anuviya.ir.expression.CallExpression;
import com.example.anuviya.ir.expression.ObjectCreationExpression;
import java.util.*;
import java.util.Map;

/**
 * Extracts method call, constructor execution, and API route references.
 */
public class CallReferenceExtractor implements ReferenceExtractor {

    private final Set<String> knownEntityNames;
    private final Map<String, SymbolKind> knownEntityKinds;

    public CallReferenceExtractor() {
        this.knownEntityNames = Collections.emptySet();
        this.knownEntityKinds = Collections.emptyMap();
    }

    public CallReferenceExtractor(Set<String> knownEntityNames) {
        this.knownEntityNames = knownEntityNames;
        this.knownEntityKinds = Collections.emptyMap();
    }

    public CallReferenceExtractor(Set<String> knownEntityNames, Map<String, SymbolKind> knownEntityKinds) {
        this.knownEntityNames = knownEntityNames;
        this.knownEntityKinds = knownEntityKinds != null ? knownEntityKinds : Collections.emptyMap();
    }

    @Override
    public void extract(IRNode node, SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu) {
        NameResolver resolver = NameResolver.build(cu, knownEntityNames);
        node.accept(new Visitor(symbolTable, database, cu, knownEntityNames, knownEntityKinds, resolver));
    }

    private static class Visitor implements IRVisitor {
        private final SymbolTable symbolTable;
        private final ReferenceDatabase database;
        private final CompilationUnit cu;
        private final Set<String> knownEntityNames;
        private final Map<String, SymbolKind> knownEntityKinds;
        private final NameResolver resolver;
        private final Deque<EntitySymbol> entityScope = new ArrayDeque<>();
        private final Deque<MemberSymbol> memberScope = new ArrayDeque<>();
        private String currentNamespace = "";

        public Visitor(SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu,
                       Set<String> knownEntityNames, Map<String, SymbolKind> knownEntityKinds, NameResolver resolver) {
            this.symbolTable = symbolTable;
            this.database = database;
            this.cu = cu;
            this.knownEntityNames = knownEntityNames;
            this.knownEntityKinds = knownEntityKinds;
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

            if (node.body() != null) {
                node.body().accept(this);
            }

            memberScope.pop();
            if (pushed) {
                entityScope.pop();
            }
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
            // Resolve to correct SymbolKind using global entity kinds map
            SymbolKind targetKind = knownEntityKinds.getOrDefault(resolvedFqn, SymbolKind.CLASS);
            EntitySymbol targetEntity = symbolTable.getOrCreateEntity(resolvedFqn, targetKind);

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
