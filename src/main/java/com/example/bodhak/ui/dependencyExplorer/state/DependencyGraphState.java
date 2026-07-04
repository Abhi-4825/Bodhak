package com.example.bodhak.ui.dependencyExplorer.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.entity.EntityKind;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.*;

public class DependencyGraphState {

    public record GraphNode(String name, String simpleName, EntityKind kind) {}
    public record GraphEdge(String source, String target, String type) {}

    private final ObservableList<GraphNode> nodes = FXCollections.observableArrayList();
    private final ObservableList<GraphEdge> edges = FXCollections.observableArrayList();
    private String focusEntityName = "";

    private final StringProperty hoveredEdgeSource = new SimpleStringProperty("");
    private final StringProperty hoveredEdgeTarget = new SimpleStringProperty("");
    private final StringProperty hoveredNodeName = new SimpleStringProperty("");

    public StringProperty hoveredEdgeSourceProperty() { return hoveredEdgeSource; }
    public StringProperty hoveredEdgeTargetProperty() { return hoveredEdgeTarget; }
    public StringProperty hoveredNodeNameProperty() { return hoveredNodeName; }

    public void clear() {
        nodes.clear();
        edges.clear();
        focusEntityName = "";
    }

    public void update(AnalysisContext context, EntityInfo focusEntity, GraphMode mode) {
        clear();
        if (context == null || focusEntity == null) return;

        this.focusEntityName = focusEntity.getEntityName();

        // Gather all entities in a lookup map
        Map<String, EntityInfo> entityMap = new HashMap<>();
        for (EntityInfo e : context.getEntities()) {
            entityMap.put(e.getEntityName(), e);
        }

        Set<String> addedNodes = new HashSet<>();
        List<GraphEdge> tempEdges = new ArrayList<>();

        // Add focus node
        addNode(focusEntity, addedNodes);

        // Limit maximum surrounding nodes to 30 to avoid clutter
        int nodeLimit = 30;

        // Outgoing dependencies
        Set<String> dependsOn = focusEntity.getDependsOn();
        if (dependsOn != null) {
            for (String dep : dependsOn) {
                if (addedNodes.size() >= nodeLimit) break;
                EntityInfo depEntity = entityMap.get(dep);
                if (depEntity != null) {
                    addNode(depEntity, addedNodes);
                    String refType = resolveReferenceType(context, focusEntityName, dep);
                    tempEdges.add(new GraphEdge(focusEntityName, dep, refType));
                }
            }
        }

        // Incoming dependencies
        Set<String> usedBy = focusEntity.getUsedBy();
        if (usedBy != null) {
            for (String user : usedBy) {
                if (addedNodes.size() >= nodeLimit) break;
                EntityInfo userEntity = entityMap.get(user);
                if (userEntity != null) {
                    addNode(userEntity, addedNodes);
                    String refType = resolveReferenceType(context, user, focusEntityName);
                    tempEdges.add(new GraphEdge(user, focusEntityName, refType));
                }
            }
        }

        edges.setAll(tempEdges);
    }

    private String resolveReferenceType(AnalysisContext context, String src, String dst) {
        if (context.getReferenceDatabase() == null) return "IMPORT";
        return context.getReferenceDatabase().getAllReferences().stream()
                .filter(r -> r.sourceSymbol().name().startsWith(src) && r.targetSymbol().name().startsWith(dst))
                .map(r -> r.kind().name())
                .findFirst()
                .orElse("IMPORT");
    }

    private void addNode(EntityInfo entity, Set<String> addedNodes) {
        if (addedNodes.add(entity.getEntityName())) {
            nodes.add(new GraphNode(entity.getEntityName(), entity.getSimpleName(), entity.getKind()));
        }
    }

    public ObservableList<GraphNode> getNodes() { return nodes; }
    public ObservableList<GraphEdge> getEdges() { return edges; }
    public String getFocusEntityName() { return focusEntityName; }
}
