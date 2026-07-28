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
 * Redesigned compiler-grade semantic TypeReferenceExtractor.
 * Extracts every type reference represented by the normalized IR,
 * using decoupled symbol kind lookup maps to remain fully language-agnostic.
 */
public class TypeReferenceExtractor implements ReferenceExtractor {

    private final Set<String> knownEntityNames;
    private final Map<String, SymbolKind> knownEntityKinds;

    public TypeReferenceExtractor() {
        this.knownEntityNames = Collections.emptySet();
        this.knownEntityKinds = Collections.emptyMap();
    }

    public TypeReferenceExtractor(Set<String> knownEntityNames) {
        this.knownEntityNames = knownEntityNames;
        this.knownEntityKinds = Collections.emptyMap();
    }

    public TypeReferenceExtractor(Set<String> knownEntityNames, Map<String, SymbolKind> knownEntityKinds) {
        this.knownEntityNames = knownEntityNames;
        this.knownEntityKinds = knownEntityKinds;
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
                       Set<String> knownEntityNames, Map<String, SymbolKind> knownEntityKinds,
                       NameResolver resolver) {
            this.symbolTable = symbolTable;
            this.database = database;
            this.cu = cu;
            this.knownEntityNames = knownEntityNames;
            this.knownEntityKinds = knownEntityKinds;
            this.resolver = resolver;
        }

        private SymbolKind getSymbolKind(String fqn) {
            return knownEntityKinds.getOrDefault(fqn, SymbolKind.CLASS);
        }

        private EntitySymbol getEntitySymbol(String name) {
            String resolvedFqn = resolver.resolve(name);
            SymbolKind kind = getSymbolKind(resolvedFqn);
            return symbolTable.getOrCreateEntity(resolvedFqn, kind);
        }

        private void addReference(EntitySymbol source, EntitySymbol target, SourceRange range,
                                  ReferenceKind kind, SemanticRole role) {
            if (source == null || target == null) return;
            long nodeHash = Objects.hash(cu.getFilePath().toString(), range.startLine(), range.startColumn());
            SymbolId originId = new SymbolId(nodeHash);
            SemanticReference ref = new SemanticReference(
                source,
                target,
                originId,
                kind,
                role,
                new ReferenceCharacteristics(true, false, false, false),
                cu.getFilePath().toFile(),
                range,
                new EmptyPayload()
            );
            database.addReference(ref);
        }

        // ── 1. Type Declaration & Super Types ────────────────────────────────────

        @Override
        public void visit(NamespaceDeclaration node) {
            this.currentNamespace = node.name();
        }

        @Override
        public void visit(ImportDeclaration node) {
            extractImportReferences(node);
        }

        @Override
        public void visit(TypeDeclaration node) {
            extractDeclaredType(node);

            String parentFqn = entityScope.isEmpty() ? "" : entityScope.peek().name();
            String defaultFqn = parentFqn.isEmpty() 
                ? (currentNamespace.isEmpty() ? node.name() : currentNamespace + "." + node.name())
                : parentFqn + "." + node.name();
            String fqn = FqnResolver.resolveEntityFqn(cu, node.name(), node.sourceRange(), defaultFqn);
            EntitySymbol typeSymbol = symbolTable.getOrCreateEntity(fqn, getSymbolKind(fqn));
            entityScope.push(typeSymbol);

            extractSuperTypes(node);
            
            node.decorators().forEach(d -> d.accept(this));
            node.members().forEach(m -> m.accept(this));

            entityScope.pop();
        }

        private void extractDeclaredType(TypeDeclaration node) {
            String parentFqn = entityScope.isEmpty() ? "" : entityScope.peek().name();
            String defaultFqn = parentFqn.isEmpty() 
                ? (currentNamespace.isEmpty() ? node.name() : currentNamespace + "." + node.name())
                : parentFqn + "." + node.name();
            String fqn = FqnResolver.resolveEntityFqn(cu, node.name(), node.sourceRange(), defaultFqn);
            symbolTable.getOrCreateEntity(fqn, getSymbolKind(fqn));
        }

        private void extractSuperTypes(TypeDeclaration node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            node.extendsTypes().forEach(et -> {
                EntitySymbol target = getEntitySymbol(et.qualifiedName().toString());
                addReference(source, target, et.sourceRange(), ReferenceKind.TYPE, SemanticRole.TypeReferenceRole.SUBTYPE);
                et.accept(this);
            });

            node.implementsTypes().forEach(it -> {
                EntitySymbol target = getEntitySymbol(it.qualifiedName().toString());
                addReference(source, target, it.sourceRange(), ReferenceKind.TYPE, SemanticRole.TypeReferenceRole.CONTRACT);
                it.accept(this);
            });

            node.permitsTypes().forEach(pt -> {
                EntitySymbol target = getEntitySymbol(pt.qualifiedName().toString());
                addReference(source, target, pt.sourceRange(), ReferenceKind.TYPE, SemanticRole.TypeReferenceRole.SUBTYPE);
                pt.accept(this);
            });
        }

        private void extractImportReferences(ImportDeclaration node) {
            EntitySymbol target = getEntitySymbol(node.path());
            EntitySymbol source = null;
            if (!entityScope.isEmpty()) {
                source = entityScope.peek();
            } else if (!cu.getEntities().isEmpty()) {
                String firstEntity = cu.getEntities().get(0).getEntityName();
                source = symbolTable.getOrCreateEntity(firstEntity, getSymbolKind(firstEntity));
            }
            if (source != null) {
                addReference(source, target, node.sourceRange(), ReferenceKind.IMPORT,
                             node.isStatic() ? SemanticRole.ImportReferenceRole.NAMESPACE_IMPORT 
                                             : SemanticRole.ImportReferenceRole.MODULE_IMPORT);
            }
        }

        // ── 2. Members & Signature Types ────────────────────────────────────────

        @Override
        public void visit(CallableDeclaration node) {
            EntitySymbol parentEntity;
            boolean pushedEntity = false;
            if (entityScope.isEmpty()) {
                String defaultFqn = currentNamespace.isEmpty() ? node.name() : currentNamespace + "." + node.name();
                String fqn = FqnResolver.resolveEntityFqn(cu, node.name(), node.sourceRange(), defaultFqn);
                parentEntity = symbolTable.getOrCreateEntity(fqn, getSymbolKind(fqn));
                entityScope.push(parentEntity);
                pushedEntity = true;
            } else {
                parentEntity = entityScope.peek();
            }
            
            SymbolKind kind = node.name().equals("<init>") ? SymbolKind.CONSTRUCTOR : SymbolKind.METHOD;
            MemberSymbol memberSymbol = symbolTable.getOrCreateMember(parentEntity, node.name(), kind);
            memberScope.push(memberSymbol);

            extractSignatureTypes(node);
            extractExceptionTypes(node);

            node.decorators().forEach(d -> d.accept(this));
            node.parameters().forEach(p -> p.accept(this));

            if (node.body() != null) {
                node.body().accept(this);
            }

            memberScope.pop();
            if (pushedEntity) {
                entityScope.pop();
            }
        }

        @Override
        public void visit(VariableDeclaration node) {
            if (entityScope.isEmpty()) return;
            
            if (memberScope.isEmpty()) {
                extractFieldTypes(node);
            } else {
                if (node.kind() == VariableKind.PARAMETER) {
                    // Handled in extractSignatureTypes
                } else {
                    extractLocalType(node);
                }
            }

            node.decorators().forEach(d -> d.accept(this));

            if (node.type() != null) {
                node.type().accept(this);
            }
            if (node.initializer() != null) {
                node.initializer().accept(this);
            }
        }

        private void extractFieldTypes(VariableDeclaration node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            if (node.type() != null) {
                EntitySymbol target = getEntitySymbol(node.type().qualifiedName().toString());
                addReference(source, target, node.sourceRange(), ReferenceKind.MEMBER,
                             node.kind() == VariableKind.FIELD ? SemanticRole.MemberReferenceRole.FIELD 
                                                               : SemanticRole.MemberReferenceRole.PROPERTY);
            }
        }

        private void extractSignatureTypes(CallableDeclaration node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            if (node.returnType() != null && node.returnType().qualifiedName() != null && !node.returnType().qualifiedName().toString().isEmpty()) {
                EntitySymbol target = getEntitySymbol(node.returnType().qualifiedName().toString());
                addReference(source, target, node.returnType().sourceRange(), ReferenceKind.MEMBER, SemanticRole.MemberReferenceRole.RETURN);
                node.returnType().accept(this);
            }

            node.parameters().forEach(p -> {
                if (p.type() != null) {
                    EntitySymbol target = getEntitySymbol(p.type().qualifiedName().toString());
                    addReference(source, target, p.sourceRange(), ReferenceKind.MEMBER, SemanticRole.MemberReferenceRole.PARAMETER);
                }
            });
        }

        private void extractLocalType(VariableDeclaration node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            if (node.type() != null) {
                EntitySymbol target = getEntitySymbol(node.type().qualifiedName().toString());
                addReference(source, target, node.sourceRange(), ReferenceKind.MEMBER, SemanticRole.MemberReferenceRole.LOCAL);
            }
        }

        // ── 3. Annotations, Exception Handling & Casts ───────────────────────────

        @Override
        public void visit(DecoratorNode node) {
            extractAnnotationTypes(node);
        }

        @Override
        public void visit(AnnotationExpression node) {
            extractAnnotationTypes(node);
        }

        private void extractAnnotationTypes(DecoratorNode node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            EntitySymbol target = getEntitySymbol(node.name());
            addReference(source, target, node.sourceRange(), ReferenceKind.ANNOTATION, SemanticRole.AnnotationReferenceRole.DECORATED_BY);

            node.arguments().forEach(a -> a.accept(this));
        }

        private void extractAnnotationTypes(AnnotationExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            EntitySymbol target = getEntitySymbol(node.name().toString());
            addReference(source, target, node.sourceRange(), ReferenceKind.ANNOTATION, SemanticRole.AnnotationReferenceRole.DECORATED_BY);

            node.arguments().forEach(a -> a.accept(this));
        }

        private void extractExceptionTypes(CallableDeclaration node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            node.throwsTypes().forEach(t -> {
                EntitySymbol target = getEntitySymbol(t.qualifiedName().toString());
                addReference(source, target, t.sourceRange(), ReferenceKind.CONTROL_FLOW, SemanticRole.ControlFlowRole.THROWS);
                t.accept(this);
            });
        }

        private void extractExceptionTypes(TryStatement node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            node.catchBlocks().forEach(c -> {
                c.accept(new IRVisitor() {
                    @Override
                    public void visit(TypeReferenceExpression tr) {
                        EntitySymbol target = getEntitySymbol(tr.qualifiedName().toString());
                        addReference(source, target, tr.sourceRange(), ReferenceKind.CONTROL_FLOW, SemanticRole.ControlFlowRole.CATCHES);
                    }
                });
            });
        }

        @Override
        public void visit(CastExpression node) {
            extractCastTypes(node);
            if (node.targetType() != null) node.targetType().accept(this);
            if (node.expression() != null) node.expression().accept(this);
        }

        private void extractCastTypes(CastExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();
            if (node.targetType() != null) {
                EntitySymbol target = getEntitySymbol(node.targetType().qualifiedName().toString());
                addReference(source, target, node.sourceRange(), ReferenceKind.DATA_FLOW, SemanticRole.DataFlowRole.USES_AS_LOCAL);
            }
        }

        // ── 4. Type References & Generic Recursion ───────────────────────────────

        @Override
        public void visit(TypeReferenceExpression node) {
            extractTypeReference(node);
            extractGenericTypes(node);
        }

        private void extractTypeReference(TypeReferenceExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            EntitySymbol target = getEntitySymbol(node.qualifiedName().toString());
            addReference(source, target, node.sourceRange(), ReferenceKind.TYPE, SemanticRole.TypeReferenceRole.SUBTYPE);
        }

        private void extractGenericTypes(TypeReferenceExpression node) {
            node.typeArguments().forEach(ta -> {
                extractTypeReference(ta);
                extractGenericTypes(ta);
            });
            node.bounds().forEach(b -> {
                extractTypeReference(b);
                extractGenericTypes(b);
            });
        }

        // ── 5. Standard Traversal Overrides (Visitor Propagations) ───────────────

        @Override
        public void visit(ModuleDeclaration node) {
            node.declarations().forEach(d -> d.accept(this));
            node.imports().forEach(imp -> imp.accept(this));
        }

        @Override
        public void visit(BlockStatement node) {
            node.statements().forEach(s -> s.accept(this));
        }

        @Override
        public void visit(ExpressionStatement node) {
            if (node.expression() != null) node.expression().accept(this);
        }

        @Override
        public void visit(IfStatement node) {
            if (node.condition() != null) node.condition().accept(this);
            if (node.thenBranch() != null) node.thenBranch().accept(this);
            if (node.elseBranch() != null) node.elseBranch().accept(this);
        }

        @Override
        public void visit(LoopStatement node) {
            if (node.condition() != null) node.condition().accept(this);
            if (node.body() != null) node.body().accept(this);
        }

        @Override
        public void visit(ReturnStatement node) {
            if (node.expression() != null) node.expression().accept(this);
        }

        @Override
        public void visit(LocalVariableStatement node) {
            if (node.declaration() != null) {
                node.declaration().accept(this);
            }
        }

        @Override
        public void visit(CallExpression node) {
            if (node.receiver() != null) node.receiver().accept(this);
            node.arguments().forEach(a -> a.accept(this));
        }

        @Override
        public void visit(ObjectCreationExpression node) {
            if (node.type() != null) node.type().accept(this);
            node.arguments().forEach(a -> a.accept(this));
        }

        @Override
        public void visit(VariableReferenceExpression node) {
            extractStaticImportReference(node);
        }

        private void extractStaticImportReference(VariableReferenceExpression node) {
            if (entityScope.isEmpty()) return;
            EntitySymbol source = entityScope.peek();

            Optional<String> staticImportClass = resolver.resolveStaticMember(node.variableName());
            if (staticImportClass.isPresent()) {
                String fqn = staticImportClass.get();
                EntitySymbol targetSymbol = symbolTable.getOrCreateEntity(fqn, getSymbolKind(fqn));
                addReference(source, targetSymbol, node.sourceRange(), ReferenceKind.TYPE, SemanticRole.TypeReferenceRole.SUBTYPE);
            }
        }

        @Override
        public void visit(ThisExpression node) {}

        @Override
        public void visit(SuperExpression node) {}

        @Override
        public void visit(ClassLiteralExpression node) {
            if (node.type() != null) node.type().accept(this);
        }

        @Override
        public void visit(FieldAccessExpression node) {
            List<String> chain = collectFieldAccessChain(node);
            Optional<String> resolvedType = resolver.resolveFieldAccessChain(chain);
            resolvedType.ifPresent(fqn -> {
                EntitySymbol target = symbolTable.getOrCreateEntity(fqn, getSymbolKind(fqn));
                addReference(entityScope.peek(), target, node.sourceRange(),
                             ReferenceKind.TYPE, SemanticRole.TypeReferenceRole.SUBTYPE);
            });
            if (node.receiver() != null) {
                node.receiver().accept(this);
            }
        }

        private List<String> collectFieldAccessChain(FieldAccessExpression node) {
            List<String> segments = new ArrayList<>();
            segments.add(node.fieldName());
            ExpressionNode receiver = node.receiver();
            while (receiver instanceof FieldAccessExpression fae) {
                segments.add(fae.fieldName());
                receiver = fae.receiver();
            }
            if (receiver instanceof VariableReferenceExpression vre) {
                segments.add(vre.variableName());
            }
            return segments;
        }

        @Override
        public void visit(AssignmentExpression node) {
            if (node.target() != null) node.target().accept(this);
            if (node.value() != null) node.value().accept(this);
        }

        @Override
        public void visit(LiteralExpression node) {}

        @Override
        public void visit(LambdaExpression node) {
            node.parameters().forEach(p -> p.accept(this));
            if (node.body() != null) node.body().accept(this);
        }

        @Override
        public void visit(BinaryExpression node) {
            extractPatternTypes(node);
            if (node.left() != null) node.left().accept(this);
            if (node.right() != null) node.right().accept(this);
        }

        @Override
        public void visit(UnaryExpression node) {
            if (node.expression() != null) node.expression().accept(this);
        }

        @Override
        public void visit(ConditionalExpression node) {
            if (node.condition() != null) node.condition().accept(this);
            if (node.thenExpr() != null) node.thenExpr().accept(this);
            if (node.elseExpr() != null) node.elseExpr().accept(this);
        }

        @Override
        public void visit(ArrayAccessExpression node) {
            if (node.array() != null) node.array().accept(this);
            if (node.index() != null) node.index().accept(this);
        }

        @Override
        public void visit(ArrayCreationExpression node) {
            if (node.type() != null) node.type().accept(this);
            node.dimensions().forEach(d -> d.accept(this));
            if (node.initializer() != null) node.initializer().accept(this);
        }

        private void extractPatternTypes(ExpressionNode node) {
            // Extension point for type matching / instanceof pattern matching
        }
    }
}
