package com.example.bodhakfrontend.engine.growth.analysis;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.engine.GraphSnapshot;
import com.example.bodhakfrontend.engine.growth.model.BridgeNode;

import java.util.*;

public class ArticulationPointAnalyzer {
    private final Map<String, Integer> discovery =
            new HashMap<>();

    private final Map<String, Integer> low =
            new HashMap<>();

    private final Map<String, String> parent =
            new HashMap<>();

    private final List<BridgeNode> articulationPoints =
            new ArrayList<>();

    private int time = 0;
    public List<BridgeNode> analyze(
            AnalysisContext context
    ) {

        discovery.clear();
        low.clear();
        parent.clear();
        articulationPoints.clear();

        time = 0;

        GraphSnapshot snapshot =
                context.getDependencyGraph()
                        .snapshot();

        Map<String, Set<String>> graph =
                buildUndirectedGraph(snapshot);

        for (String node : graph.keySet()) {

            if (!discovery.containsKey(node)) {

                dfs(node, graph);
            }
        }

        return articulationPoints;
    }
    private void dfs(

            String node,

            Map<String, Set<String>> graph

    ) {

        discovery.put(node, time);

        low.put(node, time);

        time++;

        int childCount = 0;

        boolean isArticulation = false;

        for (String neighbor :
                graph.getOrDefault(
                        node,
                        Set.of()
                )) {

            if (!discovery.containsKey(neighbor)) {

                parent.put(
                        neighbor,
                        node
                );

                childCount++;

                dfs(
                        neighbor,
                        graph
                );

                low.put(
                        node,
                        Math.min(
                                low.get(node),
                                low.get(neighbor)
                        )
                );

                if (parent.containsKey(node)
                        &&
                        low.get(neighbor)
                                >= discovery.get(node)) {

                    isArticulation = true;
                }
            }

            else if (!neighbor.equals(
                    parent.get(node)
            )) {

                low.put(
                        node,
                        Math.min(
                                low.get(node),
                                discovery.get(neighbor)
                        )
                );
            }
        }

        boolean rootArticulation =
                !parent.containsKey(node)
                        && childCount > 1;

        if (rootArticulation
                || isArticulation) {

            articulationPoints.add(
                    new BridgeNode(
                            node,
                            childCount
                    )
            );
        }
    }
    private Map<String, Set<String>> buildUndirectedGraph(
            GraphSnapshot snapshot
    ) {

        Map<String, Set<String>> graph =
                new HashMap<>();

        snapshot.globalDependencies()
                .forEach((from, targets) -> {

                    graph.computeIfAbsent(
                            from,
                            k -> new HashSet<>()
                    );

                    for (String to : targets) {

                        graph.computeIfAbsent(
                                to,
                                k -> new HashSet<>()
                        );

                        graph.get(from).add(to);

                        graph.get(to).add(from);
                    }
                });

        return graph;
    }
}
