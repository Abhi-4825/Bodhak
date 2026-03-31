package com.example.bodhakfrontend.Backend;

import com.example.bodhakfrontend.Backend.models.incrementalModel.ClassInfoViewModel;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;


import java.util.*;

public class GraphBuilder {

    private final Map<String, ClassInfoViewModel> vmMap;
    private final Set<String> visited = new HashSet<>();

    public GraphBuilder(Map<String, ClassInfoViewModel> vmMap) {
        this.vmMap = vmMap;
    }

    public void build(Graph graph, String rootName) {

        ClassInfoViewModel root = vmMap.get(rootName);
        if (root == null) return;

        addNode(graph, root.getName(), "root");
        graph.getNode(root.getName()).setAttribute("xyz",0,0,0);

        placeDepends(graph,root,1,0);
        placeUsedBy(graph,root,1,0);
    }
    private void placeDepends(Graph graph, ClassInfoViewModel vm, int level, double yStart) {

        double spacingY = 2;
        List<String> deps = new ArrayList<>(vm.getDependsOn());

        int total = deps.size();
        double startY = -(total - 1) * spacingY / 2;

        for (int i = 0; i < total; i++) {

            String dep = deps.get(i);
            ClassInfoViewModel depVm = vmMap.get(dep);
            if (depVm == null) continue;

            addNode(graph, depVm.getName(), "dep");

            double x = level * 10;
            double y = startY + i * spacingY;

            graph.getNode(depVm.getName())
                    .setAttribute("xyz", x, y, 0);

            String edgeId = vm.getName() + "->" + depVm.getName();

            if (graph.getEdge(edgeId) == null) {
                graph.addEdge(edgeId, vm.getName(), depVm.getName(), true);
            }
        }
    }

    private void placeUsedBy(Graph graph, ClassInfoViewModel vm, int level, double yStart) {

        double spacingY = 2;
        List<String> users = new ArrayList<>(vm.getUsedBy());

        int total = users.size();
        double startY = -(total - 1) * spacingY / 2;

        for (int i = 0; i < total; i++) {

            String user = users.get(i);
            ClassInfoViewModel userVm = vmMap.get(user);
            if (userVm == null) continue;

            addNode(graph, userVm.getName(), "user");

            double x = level * -10;
            double y = startY + i * spacingY;

            graph.getNode(userVm.getName())
                    .setAttribute("xyz", x, y, 0);

            String edgeId = userVm.getName() + "->" + vm.getName();

            if (graph.getEdge(edgeId) == null) {
                graph.addEdge(edgeId, userVm.getName(), vm.getName(), true);
            }
        }
    }

    private void addNode(Graph graph, String name, String type) {
        if (graph.getNode(name) == null) {
            Node node = graph.addNode(name);
            node.setAttribute("ui.label", getSimpleName(name));
            node.setAttribute("ui.class", type);
        }
    }
    private String getSimpleName(String fullName) {
        int i = fullName.lastIndexOf('.');
        return (i >= 0) ? fullName.substring(i + 1) : fullName;
    }
}
