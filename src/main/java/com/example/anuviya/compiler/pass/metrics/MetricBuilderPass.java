package com.example.anuviya.compiler.pass.metrics;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.Metrics;
import com.example.anuviya.ir.*;
import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.ir.statement.*;
import com.example.anuviya.compiler.CompilerPass;
import com.example.anuviya.compiler.PipelineContext;
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
            if (cu.getIntermediateRepresentation() == null) {
                cu.setAggregatedMetrics(new Metrics(100, 1, 0, 0, 0, 0, 0));
                continue;
            }

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

            // Compute and set aggregated metrics for this compilation unit
            int cuLoc = 0;
            int cuComplexity = 0;
            int cuNesting = 0;
            int cuParams = 0;
            int cuCognitive = 0;
            long cuMethods = 0;
            long cuConstructors = 0;

            for (EntityInfo entity : cu.getEntities()) {
                Metrics m = entity.getMetrics();
                if (m != null) {
                    if (entity.getKind() == com.example.anuviya.model.entity.EntityKind.CLASS ||
                        entity.getKind() == com.example.anuviya.model.entity.EntityKind.INTERFACE ||
                        entity.getKind() == com.example.anuviya.model.entity.EntityKind.RECORD) {
                        cuLoc += m.linesOfCode();
                    } else if (cuLoc == 0) {
                        cuLoc = Math.max(cuLoc, m.linesOfCode());
                    }
                    cuComplexity = Math.max(cuComplexity, m.cyclomaticComplexity());
                    cuNesting = Math.max(cuNesting, m.nestingDepth());
                    cuParams += m.parameterCount();
                    cuCognitive += m.cognitiveComplexity();
                    cuMethods += m.methodCount();
                    cuConstructors += m.constructorCount();
                }
            }
            if (cuLoc == 0) {
                cuLoc = 100; // default fallback if no classes found
            }
            if (cuComplexity == 0) {
                cuComplexity = 1;
            }
            cu.setAggregatedMetrics(new Metrics(
                cuLoc,
                cuComplexity,
                cuNesting,
                cuParams,
                cuCognitive,
                cuMethods,
                cuConstructors
            ));
        }

        int totalEntities = 0;
        long totalLoc = 0;
        for (CompilationUnit cu : context.getCompilationUnits()) {
            totalEntities += cu.getEntities().size();
            for (EntityInfo e : cu.getEntities()) {
                totalLoc += e.getMetrics().linesOfCode();
            }
        }
        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.MetricsComputed(totalEntities, totalLoc)
        );
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
            if (node.members() != null) {
                node.members().forEach(m -> {
                    if (m != null) m.accept(this);
                });
            }
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
            if (node.catchBlocks() != null) {
                complexity += node.catchBlocks().size();
            }
            currentNesting++;
            if (currentNesting > maxNesting) maxNesting = currentNesting;
            if (node.tryBlock() != null) node.tryBlock().accept(this);
            if (node.catchBlocks() != null) {
                node.catchBlocks().forEach(c -> {
                    if (c != null) c.accept(this);
                });
            }
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
            if (node.statements() != null) {
                node.statements().forEach(s -> {
                    if (s != null) s.accept(this);
                });
            }
        }
    }
}
