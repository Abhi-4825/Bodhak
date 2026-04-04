package com.example.bodhakfrontend.ui;

import com.example.bodhakfrontend.Backend.GraphBuilder;
import com.example.bodhakfrontend.Backend.models.incrementalModel.ClassInfoViewModel;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import java.util.*;

public class DependencyGraphWindow {

    private static final Set<String> expandedNodes = new HashSet<>();
    private static final Map<String, Integer> nodeDepth = new HashMap<>();
    private static final Map<String, Set<String>> nodeChildren = new HashMap<>();
    private static final Set<String> dynamicNodes = new HashSet<>();
    private static final Set<String> dynamicEdges = new HashSet<>();

    private static final int MAX_DEPTH = 2;


    private static String ROOT_ID;

    public static void show(String rootClass,
                            Map<String, ClassInfoViewModel> vmMap) {

        expandedNodes.clear();
        nodeDepth.clear();
        nodeChildren.clear();
        dynamicNodes.clear();
        dynamicEdges.clear();

        ROOT_ID = rootClass; // FIX

        Stage stage = new Stage();
        Graph graph = new SingleGraph("Dependencies");
        graph.setAttribute("ui.stylesheet", """
        graph {
            fill-color: #1a1a2e;
            padding: 40px;
        }

        node {
            shape: box;
            size-mode: fit;
            padding: 8px, 24px;
            text-size: 14;
            text-alignment: center;
            text-color: white;
            text-style: bold;
            fill-mode: gradient-vertical;
            stroke-mode: plain;
            stroke-color: #16213e;
            stroke-width: 2px;
            shadow-mode: plain;
            shadow-color: #0f0f1a;
            shadow-width: 5px;
            shadow-offset: 3px, 3px;
            z-index: 0;
        }

        node.root {
            fill-color: #e94560, #ff6b81;
            stroke-color: #e94560;
        }

        node.dep {
            fill-color: #0f3460, #2980b9;
            stroke-color: #0f3460;
        }

        node.user {
            fill-color: #00b894, #55efc4;
            stroke-color: #00b894;
        }

        edge {
            shape: cubic-curve;
            fill-color: #535c68;
            size: 2px;
            arrow-shape: arrow;
            arrow-size: 12px, 6px;
            z-index: 1;
        }
        """);

        GraphBuilder builder = new GraphBuilder(vmMap);
        builder.build(graph, rootClass);

        nodeDepth.put(rootClass, 0);

        FxViewer viewer = new FxViewer(graph,
                FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);

        FxViewPanel panel = (FxViewPanel) viewer.addDefaultView(false);

        stage.setScene(new Scene(new StackPane(panel), 900, 600));
        stage.show();

        ViewerPipe pipe = viewer.newViewerPipe();

        pipe.addViewerListener(new ViewerListener() {
            @Override
            public void buttonPushed(String nodeId) {
                onNodeClicked(nodeId, graph, vmMap);
            }

            @Override public void buttonReleased(String id) {}

            @Override
            public void mouseOver(String s) {

            }

            @Override
            public void mouseLeft(String s) {

            }

            @Override public void viewClosed(String id) {}
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                pipe.pump();
            }
        }.start();
    }

    private static void onNodeClicked(String nodeId,
                                      Graph graph,
                                      Map<String, ClassInfoViewModel> vmMap) {

        if (expandedNodes.contains(nodeId)) {
            collapseNode(graph, nodeId);
            return;
        }

        int depth = nodeDepth.getOrDefault(nodeId, 0);
        if (depth >= MAX_DEPTH) return;

        ClassInfoViewModel vm = vmMap.get(nodeId);
        if (vm == null) return;

        expandedNodes.add(nodeId);
        expandNode(graph, vm, depth, vmMap);
    }

    private static void expandNode(Graph graph,
                                   ClassInfoViewModel vm,
                                   int parentDepth,
                                   Map<String, ClassInfoViewModel> vmMap) {

        String parentId = vm.getName();
        Node parent = graph.getNode(parentId);

        Object[] pos = (Object[]) parent.getAttribute("xyz");
        double baseX = ((Number) pos[0]).doubleValue();
        double baseY = ((Number) pos[1]).doubleValue();

        Set<String> children = nodeChildren.computeIfAbsent(parentId, k -> new HashSet<>());

        //  Only real root shows both
        if (parentId.equals(ROOT_ID)) {

            // dependsOn → right
            expandDirection(graph, vm.getDependsOn(), parentId, parentDepth,
                    vmMap, children, baseX, baseY, +8, true);

            // usedBy → left
            expandDirection(graph, vm.getUsedBy(), parentId, parentDepth,
                    vmMap, children, baseX, baseY, -8, false);
        }
        else {

            // ALL OTHER NODES → ONLY dependsOn

            if (baseX >= 0) {
                expandDirection(graph, vm.getDependsOn(), parentId, parentDepth,
                        vmMap, children, baseX, baseY, +8, true);
            } else {
                expandDirection(graph, vm.getDependsOn(), parentId, parentDepth,
                        vmMap, children, baseX, baseY, -8, true);
            }
        }
    }

    private static void expandDirection(Graph graph,
                                        Collection<String> targets,
                                        String parentId,
                                        int parentDepth,
                                        Map<String, ClassInfoViewModel> vmMap,
                                        Set<String> children,
                                        double baseX,
                                        double baseY,
                                        double offsetX,
                                        boolean outgoing) {

        if (targets == null || targets.isEmpty()) return;

        List<String> list = new ArrayList<>(targets);
        double spacingY = 3;
        double startY = -(list.size() - 1) * spacingY / 2;

        for (int i = 0; i < list.size(); i++) {

            ClassInfoViewModel targetVm = vmMap.get(list.get(i));
            if (targetVm == null) continue;

            String id = targetVm.getName();
            children.add(id);

            if (graph.getNode(id) == null) {

                Node node = graph.addNode(id);
                node.setAttribute("ui.label", getSimpleName(id));
                node.setAttribute("ui.class", outgoing ? "dep" : "user");

                double x = baseX + offsetX;
                double y = baseY + startY + i * spacingY;

                node.setAttribute("xyz", x, y, 0);

                nodeDepth.put(id, parentDepth + 1);
                dynamicNodes.add(id);
            }

            String edgeId = outgoing
                    ? parentId + "->" + id
                    : id + "->" + parentId;

            if (graph.getEdge(edgeId) == null) {

                if (outgoing)
                    graph.addEdge(edgeId, parentId, id, true);
                else
                    graph.addEdge(edgeId, id, parentId, true);

                dynamicEdges.add(edgeId);
            }
        }
    }

    private static void collapseNode(Graph graph, String nodeId) {

        expandedNodes.remove(nodeId);

        Set<String> children = nodeChildren.remove(nodeId);
        if (children == null) return;

        for (String childId : children) {

            collapseNode(graph, childId);

            String e1 = nodeId + "->" + childId;
            String e2 = childId + "->" + nodeId;

            // REMOVE ONLY dynamic edges
            if (dynamicEdges.contains(e1)) {
                graph.removeEdge(e1);
                dynamicEdges.remove(e1);
            }

            if (dynamicEdges.contains(e2)) {
                graph.removeEdge(e2);
                dynamicEdges.remove(e2);
            }

            Node child = graph.getNode(childId);

            if (child != null &&
                    child.getDegree() == 0 &&
                    dynamicNodes.contains(childId)) {

                graph.removeNode(childId);
                dynamicNodes.remove(childId);
                nodeDepth.remove(childId);
            }
        }
    }

    private static String getSimpleName(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i + 1) : name;
    }
}