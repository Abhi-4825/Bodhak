package com.example.bodhak.ui.dependencyExplorer.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.GraphSnapshot;
import com.example.bodhak.model.entity.EntityInfo;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class SelectedEntityState {

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty namespace = new SimpleStringProperty("");
    private final StringProperty kind = new SimpleStringProperty("");
    private final IntegerProperty loc = new SimpleIntegerProperty(0);
    private final IntegerProperty fanOut = new SimpleIntegerProperty(0);
    private final IntegerProperty fanIn = new SimpleIntegerProperty(0);
    private final DoubleProperty instability = new SimpleDoubleProperty(0.0);
    private final IntegerProperty depth = new SimpleIntegerProperty(0);
    private final IntegerProperty referenceCount = new SimpleIntegerProperty(0);

    private final ObservableList<String> dependsOn = FXCollections.observableArrayList();
    private final ObservableList<String> usedBy = FXCollections.observableArrayList();

    public void clear() {
        name.set("");
        namespace.set("");
        kind.set("");
        loc.set(0);
        fanOut.set(0);
        fanIn.set(0);
        instability.set(0.0);
        depth.set(0);
        referenceCount.set(0);
        dependsOn.clear();
        usedBy.clear();
    }

    public void update(AnalysisContext context, EntityInfo entity) {
        clear();
        if (context == null || entity == null) return;

        name.set(entity.getEntityName());
        namespace.set(entity.getNamespaceName());
        kind.set(entity.getKind().name());
        loc.set(entity.getLinesOfCode());

        int ce = entity.getDependsOn() != null ? entity.getDependsOn().size() : 0;
        int ca = entity.getUsedBy() != null ? entity.getUsedBy().size() : 0;
        fanOut.set(ce);
        fanIn.set(ca);

        if (ca + ce > 0) {
            instability.set((double) ce / (ca + ce));
        } else {
            instability.set(0.0);
        }

        // Calculate Depth using reverse dependencies
        if (context.getDependencyGraph() != null) {
            GraphSnapshot snapshot = context.getDependencyGraph().snapshot();
            if (snapshot != null) {
                depth.set(calculateEntityDepth(entity.getEntityName(), snapshot.reverseDependencies()));
            }
        }

        // Calculate reference count from ReferenceDatabase
        if (context.getReferenceDatabase() != null) {
            long refs = context.getReferenceDatabase().getAllReferences().stream()
                    .filter(r -> r.sourceSymbol().name().startsWith(entity.getEntityName()) ||
                                 r.targetSymbol().name().startsWith(entity.getEntityName()))
                    .count();
            referenceCount.set((int) refs);
        }

        if (entity.getDependsOn() != null) {
            dependsOn.addAll(entity.getDependsOn());
        }
        if (entity.getUsedBy() != null) {
            usedBy.addAll(entity.getUsedBy());
        }
    }

    private int calculateEntityDepth(String node, Map<String, Set<String>> reverseDeps) {
        return getDepth(node, reverseDeps, new HashMap<>(), new HashSet<>());
    }

    private int getDepth(String node, Map<String, Set<String>> reverseDeps, Map<String, Integer> memo, Set<String> visiting) {
        if (memo.containsKey(node)) return memo.get(node);
        if (visiting.contains(node)) return 0;

        visiting.add(node);
        Set<String> incoming = reverseDeps.get(node);
        int maxParentDepth = 0;
        if (incoming != null) {
            for (String parent : incoming) {
                maxParentDepth = Math.max(maxParentDepth, getDepth(parent, reverseDeps, memo, visiting));
            }
        }
        visiting.remove(node);
        int depthVal = 1 + maxParentDepth;
        memo.put(node, depthVal);
        return depthVal;
    }

    public ReadOnlyStringProperty nameProperty() { return name; }
    public ReadOnlyStringProperty namespaceProperty() { return namespace; }
    public ReadOnlyStringProperty kindProperty() { return kind; }
    public ReadOnlyIntegerProperty locProperty() { return loc; }
    public ReadOnlyIntegerProperty fanOutProperty() { return fanOut; }
    public ReadOnlyIntegerProperty fanInProperty() { return fanIn; }
    public ReadOnlyDoubleProperty instabilityProperty() { return instability; }
    public ReadOnlyIntegerProperty depthProperty() { return depth; }
    public ReadOnlyIntegerProperty referenceCountProperty() { return referenceCount; }

    public ObservableList<String> getDependsOn() { return dependsOn; }
    public ObservableList<String> getUsedBy() { return usedBy; }
}
