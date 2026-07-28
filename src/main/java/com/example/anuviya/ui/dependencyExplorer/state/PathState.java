package com.example.anuviya.ui.dependencyExplorer.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.GraphSnapshot;
import com.example.anuviya.model.entity.EntityInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class PathState {

    private final ObservableList<String> pathNodes = FXCollections.observableArrayList();

    public void clear() {
        pathNodes.clear();
    }

    public void update(AnalysisContext context, EntityInfo entity) {
        clear();
        if (context == null || entity == null || context.getDependencyGraph() == null) return;

        GraphSnapshot snapshot = context.getDependencyGraph().snapshot();
        if (snapshot == null) return;

        String target = entity.getEntityName();

        // Standard BFS to find shortest path from any root node to target
        Map<String, Set<String>> reverseGraph = snapshot.reverseDependencies(); // child -> parents (incoming)
        
        Queue<List<String>> queue = new LinkedList<>();
        queue.add(Collections.singletonList(target));
        Set<String> visited = new HashSet<>();
        visited.add(target);

        List<String> shortestPath = null;

        while (!queue.isEmpty()) {
            List<String> currentPath = queue.poll();
            String current = currentPath.get(currentPath.size() - 1);

            Set<String> parents = reverseGraph.get(current);
            if (parents == null || parents.isEmpty()) {
                // We reached a root!
                shortestPath = currentPath;
                break;
            }

            for (String parent : parents) {
                if (!visited.contains(parent)) {
                    visited.add(parent);
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(parent);
                    queue.add(newPath);
                }
            }
        }

        if (shortestPath != null) {
            List<String> finalPath = new ArrayList<>(shortestPath);
            Collections.reverse(finalPath);
            
            pathNodes.add("Workspace");
            for (String p : finalPath) {
                int lastDot = p.lastIndexOf('.');
                String simple = lastDot == -1 ? p : p.substring(lastDot + 1);
                pathNodes.add(simple);
            }
        } else {
            pathNodes.add("Workspace");
            pathNodes.add(entity.getSimpleName());
        }
    }

    public ObservableList<String> getPathNodes() {
        return pathNodes;
    }
}
