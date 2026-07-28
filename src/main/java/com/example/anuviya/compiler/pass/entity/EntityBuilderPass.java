package com.example.anuviya.compiler.pass.entity;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.model.entity.*;
import com.example.anuviya.ir.*;
import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.compiler.CompilerPass;
import com.example.anuviya.compiler.PipelineContext;
import com.example.anuviya.model.entity.EntityKind;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compiler pass that traverses the semantic IR node trees and maps them to skeleton EntityInfo instances.
 */
public class EntityBuilderPass implements CompilerPass {

    @Override
    public String getName() {
        return "EntityBuilderPass";
    }

    @Override
    public void execute(PipelineContext context) {
        Set<String> seenFqns = new HashSet<>();
        for (CompilationUnit cu : context.getCompilationUnits()) {
            if (cu.getIntermediateRepresentation() != null) {
                String languageId = getLanguageId(cu.getFilePath().toString());
                // Namespace is set dynamically by NamespaceDeclaration nodes in the IR tree
                cu.getIntermediateRepresentation().accept(new EntityVisitor(cu, languageId, "", seenFqns));
            }
        }
    }

    private String getLanguageId(String path) {
        if (path.endsWith(".java")) return "java";
        if (path.endsWith(".py")) return "python";
        return "unknown";
    }

    private static class EntityVisitor implements IRVisitor {
        private final CompilationUnit cu;
        private final String languageId;
        private String currentNamespace;
        private final Deque<String> scopeFqn = new ArrayDeque<>();
        private final Set<String> seenFqns;

        public EntityVisitor(CompilationUnit cu, String languageId, String initialNamespace, Set<String> seenFqns) {
            this.cu = cu;
            this.languageId = languageId;
            this.currentNamespace = initialNamespace;
            this.seenFqns = seenFqns;
        }

        private String makeUniqueFqn(String baseFqn) {
            String fqn = baseFqn;
            int counter = 1;
            while (seenFqns.contains(fqn)) {
                fqn = baseFqn + "_" + counter++;
            }
            seenFqns.add(fqn);
            return fqn;
        }

        @Override
        public void visit(ModuleDeclaration node) {
            // Module is the compilation unit, NOT an entity.
            // Only classes and top-level functions inside it become entities.
            // Walk children to discover them.
            node.declarations().forEach(d -> {
                if (d != null) d.accept(this);
            });
            node.imports().forEach(imp -> {
                if (imp != null) imp.accept(this);
            });
            if (node.moduleStatements() != null) {
                node.moduleStatements().forEach(s -> {
                    if (s != null) s.accept(this);
                });
            }
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            this.currentNamespace = node.name();
        }

        @Override
        public void visit(ImportDeclaration node) {
            cu.addImport(node);
        }

        @Override
        public void visit(TypeDeclaration node) {
            String parentFqn = scopeFqn.peek();
            String fqn = (parentFqn == null || parentFqn.isEmpty())
                    ? (currentNamespace.isEmpty() ? node.name() : currentNamespace + "." + node.name())
                    : parentFqn + "." + node.name();
            fqn = makeUniqueFqn(fqn);

            scopeFqn.push(fqn);

            // Extract tags
            Set<String> tags = new HashSet<>();
            node.decorators().forEach(d -> {
                tags.add(d.name());
                if (d.name().contains("dataclass")) tags.add("is_dataclass");
                if (d.name().contains("route") || d.name().contains("get") || d.name().contains("post")) {
                    tags.add("framework_root");
                }
            });

            // Fields, callables, and nested types
            Set<String> fieldNames = new HashSet<>();
            List<String> memberNames = new ArrayList<>();
            List<VariableDeclaration> fieldDecls = new ArrayList<>();
            List<CallableDeclaration> callableDecls = new ArrayList<>();
            List<TypeDeclaration> nestedTypes = new ArrayList<>();

            for (DeclarationNode member : node.members()) {
                if (member instanceof VariableDeclaration vd) {
                    fieldNames.add(vd.name());
                    fieldDecls.add(vd);
                } else if (member instanceof CallableDeclaration cd) {
                    memberNames.add(cd.name());
                    callableDecls.add(cd);
                } else if (member instanceof TypeDeclaration td) {
                    nestedTypes.add(td);
                }
            }

            EntityKind kind = EntityKind.CLASS;
            switch (node.typeKind()) {
                case INTERFACE -> kind = EntityKind.INTERFACE;
                case RECORD -> kind = EntityKind.RECORD;
                case ENUM -> kind = EntityKind.ENUM;
                case STRUCT -> kind = EntityKind.STRUCT;
                case TRAIT -> kind = EntityKind.TRAIT;
                case PROTOCOL -> kind = EntityKind.INTERFACE;
                case CLASS -> kind = EntityKind.CLASS;
            }

            long methodCount = callableDecls.stream().filter(m -> !m.name().equals("<init>")).count();
            long constructorCount = callableDecls.stream().filter(m -> m.name().equals("<init>")).count();

            int loc = node.sourceRange().endLine() - node.sourceRange().startLine() + 1;

            Identity identity = new Identity(fqn, node.name(), currentNamespace, kind, languageId, node.modifiers());
            SourceLocation location = new SourceLocation(
                cu.getFilePath().toFile(),
                node.sourceRange().startLine(),
                node.sourceRange().startColumn(),
                node.sourceRange().endLine(),
                node.sourceRange().endColumn(),
                loc
            );
            List<String> superTypes = new ArrayList<>();
            node.extendsTypes().forEach(et -> superTypes.add(et.qualifiedName().toString()));
            node.implementsTypes().forEach(it -> superTypes.add(it.qualifiedName().toString()));

            Structure structure = new Structure(fieldNames, memberNames, fieldDecls, callableDecls, nestedTypes);
            Relationships relationships = new Relationships(new HashSet<>(), new HashSet<>(), new HashSet<>(superTypes), new HashSet<>());
            Metrics metrics = new Metrics(loc, 1, 0, 0, 0, methodCount, constructorCount);
            Documentation documentation = new Documentation(
                node.documentation(),
                new ArrayList<>(),
                node.decorators().stream().map(DecoratorNode::name).collect(Collectors.toSet()),
                node.decorators()
            );
            Contribution contribution = new Contribution(tags, null);

            EntityInfo entity = new EntityInfo(identity, location, structure, relationships, metrics, documentation, contribution, new LanguageMetadata(new HashMap<>()));
            cu.addEntity(entity);

            // Recurse members
            if (node.members() != null) {
                node.members().forEach(m -> {
                    if (m != null) m.accept(this);
                });
            }

            scopeFqn.pop();
        }

        @Override
        public void visit(CallableDeclaration node) {
            String parentFqn = scopeFqn.peek();
            if (node.scope() == CallableScope.MODULE || (parentFqn == null || parentFqn.isEmpty())) {
                String fqn = currentNamespace.isEmpty() ? node.name() : currentNamespace + "." + node.name();
                fqn = makeUniqueFqn(fqn);
                Set<String> tags = new HashSet<>();
                node.decorators().forEach(d -> tags.add(d.name()));
                if (node.name().startsWith("test_")) {
                    tags.add("pytest_test");
                }

                int loc = node.sourceRange().endLine() - node.sourceRange().startLine() + 1;

                Identity identity = new Identity(fqn, node.name(), currentNamespace, EntityKind.FUNCTION, languageId, node.modifiers());
                SourceLocation location = new SourceLocation(
                    cu.getFilePath().toFile(),
                    node.sourceRange().startLine(),
                    node.sourceRange().startColumn(),
                    node.sourceRange().endLine(),
                    node.sourceRange().endColumn(),
                    loc
                );
                Structure structure = new Structure(new HashSet<>(), new ArrayList<>(), new ArrayList<>(), List.of(node), new ArrayList<>());
                Relationships relationships = new Relationships(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
                Metrics metrics = new Metrics(loc, 1, 0, node.parameters().size(), 0, 1, 0);
                Documentation documentation = new Documentation(
                    node.documentation(),
                    new ArrayList<>(),
                    node.decorators().stream().map(DecoratorNode::name).collect(Collectors.toSet()),
                    node.decorators()
                );
                Contribution contribution = new Contribution(tags, null);

                EntityInfo entity = new EntityInfo(identity, location, structure, relationships, metrics, documentation, contribution, new LanguageMetadata(new HashMap<>()));
                cu.addEntity(entity);
            }
            
            if (node.body() != null) {
                node.body().accept(this);
            }
        }
    }
}
