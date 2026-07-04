package com.example.bodhak.compiler.pass.metrics;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.entity.Metrics;
import com.example.bodhak.ir.*;
import com.example.bodhak.ir.declaration.*;
import com.example.bodhak.ir.statement.*;
import com.example.bodhak.ir.expression.*;
import com.example.bodhak.compiler.CompilerPass;
import com.example.bodhak.compiler.PipelineContext;
import java.util.*;

/**
 * Compiler pass that traverses the semantic IR node tree to compute code quality metrics for each entity.
 */
public class MetricBuilderPass implements CompilerPass {

    @Override
    public String getName() {
        return "MetricBuilderPass";
    }

    @Override
    public void execute(PipelineContext context) {
        for (CompilationUnit cu : context.getCompilationUnits()) {
            if (cu.getIntermediateRepresentation() == null) continue;

            List<EntityInfo> enrichedEntities = new ArrayList<>();
            for (EntityInfo entity : cu.getEntities()) {
                Metrics metrics = computeMetricsForEntity(cu.getIntermediateRepresentation(), entity);
                
                EntityInfo enriched = new EntityInfo(
                    entity.getIdentity(),
                    entity.getSourceLocation(),
                    entity.getStructure(),
                    entity.getRelationships(),
                    metrics,
                    entity.getDocumentation(),
                    entity.getContribution(),
                    entity.getLanguageMetadata()
                );
                enrichedEntities.add(enriched);
            }
            cu.getEntities().clear();
            cu.getEntities().addAll(enrichedEntities);
        }
    }

    private Metrics computeMetricsForEntity(IRNode root, EntityInfo entity) {
        // Find the node corresponding to the entity FQN or name
        NodeFinder finder = new NodeFinder(entity.getSimpleName());
        root.accept(finder);

        if (finder.foundNode == null) {
            return new Metrics(entity.getLinesOfCode(), 1, 0, 0, 0, entity.getMethodCount(), entity.getConstructorCount());
        }

        ComplexityVisitor visitor = new ComplexityVisitor();
        finder.foundNode.accept(visitor);

        int paramCount = 0;
        if (finder.foundNode instanceof CallableDeclaration cd) {
            paramCount = cd.parameters().size();
        }

        return new Metrics(
            entity.getLinesOfCode(),
            visitor.complexity,
            visitor.maxNesting,
            paramCount,
            visitor.complexity + visitor.maxNesting, // cognitive proxy
            entity.getMethodCount(),
            entity.getConstructorCount()
        );
    }

    private static class NodeFinder implements IRVisitor {
        private final String simpleName;
        private DeclarationNode foundNode;

        public NodeFinder(String simpleName) {
            this.simpleName = simpleName;
        }

        @Override
        public void visit(TypeDeclaration node) {
            if (node.name().equals(simpleName)) {
                foundNode = node;
                return;
            }
            node.members().forEach(m -> m.accept(this));
        }

        @Override
        public void visit(CallableDeclaration node) {
            if (node.name().equals(simpleName)) {
                foundNode = node;
                return;
            }
            if (node.body() != null) {
                node.body().accept(this);
            }
        }
    }

    private static class ComplexityVisitor implements IRVisitor {
        private int complexity = 1;
        private int maxNesting = 0;
        private int currentNesting = 0;

        @Override
        public void visit(IfStatement node) {
            complexity++;
            currentNesting++;
            if (currentNesting > maxNesting) maxNesting = currentNesting;
            if (node.thenBranch() != null) node.thenBranch().accept(this);
            if (node.elseBranch() != null) node.elseBranch().accept(this);
            currentNesting--;
        }

        @Override
        public void visit(LoopStatement node) {
            complexity++;
            currentNesting++;
            if (currentNesting > maxNesting) maxNesting = currentNesting;
            if (node.body() != null) node.body().accept(this);
            currentNesting--;
        }

        @Override
        public void visit(TryStatement node) {
            complexity += node.catchBlocks().size();
            currentNesting++;
            if (currentNesting > maxNesting) maxNesting = currentNesting;
            if (node.tryBlock() != null) node.tryBlock().accept(this);
            node.catchBlocks().forEach(c -> c.accept(this));
            if (node.finallyBlock() != null) node.finallyBlock().accept(this);
            currentNesting--;
        }

        @Override
        public void visit(ReturnStatement node) {
            if (node.expression() != null) {
                node.expression().accept(this);
            }
        }

        @Override
        public void visit(BlockStatement node) {
            node.statements().forEach(s -> s.accept(this));
        }
    }
}
