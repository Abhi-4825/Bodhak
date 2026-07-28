package com.example.anuviya.ui.dependencyExplorer.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.ui.dependencyExplorer.components.EntityPath;
import com.example.anuviya.model.reference.SemanticReference;
import com.example.anuviya.model.reference.ReferenceKind;
import com.example.anuviya.ui.dependencyExplorer.components.PathQueryEngine;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class CyclesState {

    private final ObservableList<EntityPath> cycles = FXCollections.observableArrayList();
    private final ObjectProperty<EntityPath> selectedCycle = new SimpleObjectProperty<>(null);

    // Overall metrics properties
    private final StringProperty totalCyclesMetric = new SimpleStringProperty("0");
    private final StringProperty affectedEntitiesMetric = new SimpleStringProperty("0");
    private final StringProperty maxCycleLengthMetric = new SimpleStringProperty("0");
    private final StringProperty strongComponentsMetric = new SimpleStringProperty("0");
    private final StringProperty cycleDensityMetric = new SimpleStringProperty("0.00%");
    private final StringProperty cycleHealthMetric = new SimpleStringProperty("Healthy");

    // Selected cycle metrics properties
    private final StringProperty cycleLengthMetric = new SimpleStringProperty("-");
    private final StringProperty internalDepsMetric = new SimpleStringProperty("-");
    private final StringProperty externalDepsMetric = new SimpleStringProperty("-");
    private final StringProperty instabilityMetric = new SimpleStringProperty("-");
    private final StringProperty impactScoreMetric = new SimpleStringProperty("-");

    // Root Cause Analysis properties
    private final StringProperty primaryCause = new SimpleStringProperty("—");
    private final StringProperty secondaryCause = new SimpleStringProperty("—");
    private final StringProperty introducedBy = new SimpleStringProperty("—");
    private final StringProperty cyclePattern = new SimpleStringProperty("—");

    // Cycle Impact properties
    private final StringProperty entitiesAffected = new SimpleStringProperty("0");
    private final StringProperty namespacesAffected = new SimpleStringProperty("0");
    private final StringProperty compUnitsAffected = new SimpleStringProperty("0");
    private final StringProperty archRisk = new SimpleStringProperty("MEDIUM");
    private final DoubleProperty refactorCostProgress = new SimpleDoubleProperty(0.0);
    private final StringProperty refactorCostLabel = new SimpleStringProperty("0%");

    public void clear() {
        cycles.clear();
        selectedCycle.set(null);
        totalCyclesMetric.set("0");
        affectedEntitiesMetric.set("0");
        maxCycleLengthMetric.set("0");
        strongComponentsMetric.set("0");
        cycleDensityMetric.set("0.00%");
        cycleHealthMetric.set("Healthy");

        cycleLengthMetric.set("-");
        internalDepsMetric.set("-");
        externalDepsMetric.set("-");
        instabilityMetric.set("-");
        impactScoreMetric.set("-");

        primaryCause.set("—");
        secondaryCause.set("—");
        introducedBy.set("—");
        cyclePattern.set("—");

        entitiesAffected.set("0");
        namespacesAffected.set("0");
        compUnitsAffected.set("0");
        archRisk.set("MEDIUM");
        refactorCostProgress.set(0.0);
        refactorCostLabel.set("0%");
    }

    public void update(AnalysisContext context) {
        clear();
        if (context == null || context.getDependencyGraph() == null) {
            return;
        }

        var graphIndex = context.getSemanticGraphIndex();
        Set<Set<String>> circularGroups = context.getDependencyGraph().snapshot().circularGroups();
        List<EntityPath> detectedCycles = new ArrayList<>();
        for (Set<String> scc : circularGroups) {
            if (scc.size() > 1) {
                EntityPath path = PathQueryEngine.extractCycleFromSCC(graphIndex, scc);
                if (path != null && !path.getEntities().isEmpty()) {
                    detectedCycles.add(path);
                } else {
                    List<String> rawNodes = new ArrayList<>(scc);
                    if (rawNodes.size() > 1 && !rawNodes.get(0).equals(rawNodes.get(rawNodes.size() - 1))) {
                        rawNodes.add(rawNodes.get(0));
                    }
                    detectedCycles.add(new EntityPath(rawNodes, Collections.emptyList()));
                }
            }
        }

        // Sort cycles by size descending
        detectedCycles.sort((c1, c2) -> Integer.compare(c2.getEntities().size(), c1.getEntities().size()));
        cycles.addAll(detectedCycles);

        if (!cycles.isEmpty()) {
            selectedCycle.set(cycles.get(0));
            updateSelectedCycleMetrics(context, cycles.get(0));
        }

        // Overall Cycles Metrics
        int total = cycles.size();
        totalCyclesMetric.set(String.valueOf(total));
        strongComponentsMetric.set(String.valueOf(total));

        Set<String> affected = new HashSet<>();
        int maxLen = 0;
        for (EntityPath p : cycles) {
            affected.addAll(p.getEntities());
            if (p.getHops() > maxLen) {
                maxLen = p.getHops();
            }
        }
        affectedEntitiesMetric.set(String.valueOf(affected.size()));
        maxCycleLengthMetric.set(String.valueOf(maxLen));

        if (graphIndex.getEntityCount() > 0) {
            int totalEdges = 0;
            for (int i = 0; i < graphIndex.getEntityCount(); i++) {
                totalEdges += graphIndex.getForwardEdges(i).length;
            }
            int cycleEdges = 0;
            for (EntityPath p : cycles) {
                cycleEdges += p.getHops();
            }
            double density = totalEdges > 0 ? (cycleEdges / (double) totalEdges) * 100 : 0.0;
            cycleDensityMetric.set(String.format("%.2f%%", density));
        }

        if (total == 0) {
            cycleHealthMetric.set("Healthy");
        } else if (total < 5) {
            cycleHealthMetric.set("Needs Attention");
        } else {
            cycleHealthMetric.set("Critical");
        }
    }

    public void updateSelectedCycleMetrics(AnalysisContext context, EntityPath cycle) {
        if (cycle == null) {
            cycleLengthMetric.set("-");
            internalDepsMetric.set("-");
            externalDepsMetric.set("-");
            instabilityMetric.set("-");
            impactScoreMetric.set("-");

            primaryCause.set("—");
            secondaryCause.set("—");
            introducedBy.set("—");
            cyclePattern.set("—");

            entitiesAffected.set("0");
            namespacesAffected.set("0");
            compUnitsAffected.set("0");
            archRisk.set("MEDIUM");
            refactorCostProgress.set(0.0);
            refactorCostLabel.set("0%");
            return;
        }

        List<String> nodes = cycle.getEntities();
        int nEntities = nodes.size() - 1;

        // 1. Basic Metrics
        cycleLengthMetric.set(String.valueOf(nEntities));
        
        int internalDeps = 0;
        int externalDeps = 0;
        Set<String> cycleNodeSet = new HashSet<>(nodes);
        var graphIndex = context != null ? context.getSemanticGraphIndex() : null;
        if (graphIndex != null) {
            for (String node : cycleNodeSet) {
                int id = graphIndex.getEntityId(node);
                if (id != -1) {
                    for (int targetId : graphIndex.getForwardEdges(id)) {
                        String targetName = graphIndex.getEntityName(targetId);
                        if (cycleNodeSet.contains(targetName)) {
                            internalDeps++;
                        } else {
                            externalDeps++;
                        }
                    }
                }
            }
        }
        internalDepsMetric.set(String.valueOf(internalDeps));
        externalDepsMetric.set(String.valueOf(externalDeps));

        double instability = (internalDeps + externalDeps) > 0 ? (double) externalDeps / (internalDeps + externalDeps) : 0.0;
        instabilityMetric.set(String.format("%.2f", instability));

        double impact = (nEntities / 50.0);
        if (impact > 1.0) impact = 1.0;
        impactScoreMetric.set(String.format("%s (%.2f)", nEntities >= 20 ? "High" : (nEntities >= 6 ? "Medium" : "Low"), impact));

        // 2. Root Cause Analysis Calculations
        String primCause = "Circular Package Import";
        String secCause = "Indirect Reference Coupling";
        String introBy = "N/A";
        String pattern = nEntities == 2 ? "Mutual Reference" : "Multi-Node Circular Loop";

        if (context != null && nEntities > 0) {
            boolean hasCallsFwd = false;
            boolean hasCallsBwd = false;
            boolean hasInheritance = false;
            boolean hasFields = false;
            boolean hasTypes = false;

            String nodeA = nodes.get(0);
            String nodeB = nodes.get(1);

            ReferenceDatabase refDb = context.getReferenceDatabase();
            if (refDb != null) {
                for (SemanticReference ref : refDb.getAllReferences()) {
                    String src = PathQueryEngine.getEntityQualifiedName(ref.sourceSymbol());
                    String dst = PathQueryEngine.getEntityQualifiedName(ref.targetSymbol());
                    
                    if (src.equals(nodeA) && dst.equals(nodeB)) {
                        introBy = ref.sourceFile().getName() + "\nLine " + ref.location().startLine();
                    }
                    
                    for (int i = 0; i < nodes.size() - 1; i++) {
                        String fromNode = nodes.get(i);
                        String toNode = nodes.get(i + 1);
                        if (src.equals(fromNode) && dst.equals(toNode)) {
                            if (ref.kind() == ReferenceKind.CALL) {
                                hasCallsFwd = true;
                            } else if (ref.kind() == ReferenceKind.TYPE) {
                                String roleName = ref.role().toString().toUpperCase();
                                if (roleName.contains("SUBCLASS") || roleName.contains("INHERIT") || roleName.contains("EXTEND") || roleName.contains("IMPLEMENT")) {
                                    hasInheritance = true;
                                } else {
                                    hasTypes = true;
                                }
                            } else if (ref.kind() == ReferenceKind.MEMBER || ref.kind() == ReferenceKind.DATA_FLOW) {
                                hasFields = true;
                            }
                        }
                        if (src.equals(toNode) && dst.equals(fromNode)) {
                            if (ref.kind() == ReferenceKind.CALL) {
                                hasCallsBwd = true;
                            }
                        }
                    }
                }
            }

            if (hasInheritance) {
                primCause = "Inheritance Loop";
            } else if (hasCallsFwd && hasCallsBwd) {
                primCause = "Bidirectional Service Calls";
            } else if (hasCallsFwd) {
                primCause = "Direct Method Invocation";
            }

            if (hasFields) {
                secCause = "Shared State Dependency";
            } else if (hasTypes) {
                secCause = "Shared Model Dependency";
            }
        }

        primaryCause.set("✓ " + primCause);
        secondaryCause.set("✓ " + secCause);
        introducedBy.set(introBy);
        cyclePattern.set(pattern);

        // 3. Impact Calculations
        int affCount = nEntities;
        Set<String> uniqueNamespaces = new HashSet<>();
        Set<String> uniqueCompUnits = new HashSet<>();

        for (int i = 0; i < nEntities; i++) {
            String nodeName = nodes.get(i);
            int lastDot = nodeName.lastIndexOf('.');
            if (lastDot != -1) {
                uniqueNamespaces.add(nodeName.substring(0, lastDot));
            } else {
                uniqueNamespaces.add("default");
            }

            if (context != null) {
                context.findEntity(nodeName).ifPresent(entity -> {
                    uniqueCompUnits.add(entity.getSourceFile().getAbsolutePath());
                });
            }
        }

        if (context != null) {
            Set<String> affectedSet = new HashSet<>(nodes);
            Queue<String> queue = new LinkedList<>(nodes);
            var graph = context.getDependencyGraph();
            if (graph != null) {
                while (!queue.isEmpty()) {
                    String curr = queue.poll();
                    context.findEntity(curr).ifPresent(entity -> {
                        for (String dep : entity.getUsedBy()) {
                            if (affectedSet.add(dep)) {
                                queue.add(dep);
                            }
                        }
                    });
                }
            }
            affCount = affectedSet.size();
        }

        entitiesAffected.set(String.valueOf(affCount));
        namespacesAffected.set(String.valueOf(uniqueNamespaces.size()));
        compUnitsAffected.set(String.valueOf(uniqueCompUnits.isEmpty() ? 1 : uniqueCompUnits.size()));

        String risk = "MEDIUM";
        if (nEntities >= 15 || affCount >= 50) {
            risk = "CRITICAL";
        } else if (nEntities >= 6 || affCount >= 20) {
            risk = "HIGH";
        } else if (nEntities <= 2 && affCount <= 3) {
            risk = "LOW";
        }
        archRisk.set(risk);

        double costPercent = Math.min(95.0, 15.0 * nEntities + 3.0 * uniqueCompUnits.size() + 1.0 * affCount);
        if (nEntities == 0) costPercent = 0;
        refactorCostProgress.set(costPercent / 100.0);
        refactorCostLabel.set(String.format("%.0f%%", costPercent));
    }

    // Getters for properties
    public ObservableList<EntityPath> getCycles() { return cycles; }
    public ObjectProperty<EntityPath> selectedCycleProperty() { return selectedCycle; }

    public StringProperty totalCyclesMetricProperty() { return totalCyclesMetric; }
    public StringProperty affectedEntitiesMetricProperty() { return affectedEntitiesMetric; }
    public StringProperty maxCycleLengthMetricProperty() { return maxCycleLengthMetric; }
    public StringProperty strongComponentsMetricProperty() { return strongComponentsMetric; }
    public StringProperty cycleDensityMetricProperty() { return cycleDensityMetric; }
    public StringProperty cycleHealthMetricProperty() { return cycleHealthMetric; }

    public StringProperty cycleLengthMetricProperty() { return cycleLengthMetric; }
    public StringProperty internalDepsMetricProperty() { return internalDepsMetric; }
    public StringProperty externalDepsMetricProperty() { return externalDepsMetric; }
    public StringProperty instabilityMetricProperty() { return instabilityMetric; }
    public StringProperty impactScoreMetricProperty() { return impactScoreMetric; }

    public StringProperty primaryCauseProperty() { return primaryCause; }
    public StringProperty secondaryCauseProperty() { return secondaryCause; }
    public StringProperty introducedByProperty() { return introducedBy; }
    public StringProperty cyclePatternProperty() { return cyclePattern; }

    public StringProperty entitiesAffectedProperty() { return entitiesAffected; }
    public StringProperty namespacesAffectedProperty() { return namespacesAffected; }
    public StringProperty compUnitsAffectedProperty() { return compUnitsAffected; }
    public StringProperty archRiskProperty() { return archRisk; }
    public DoubleProperty refactorCostProgressProperty() { return refactorCostProgress; }
    public StringProperty refactorCostLabelProperty() { return refactorCostLabel; }
}
