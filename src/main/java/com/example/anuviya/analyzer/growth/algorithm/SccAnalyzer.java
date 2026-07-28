package com.example.anuviya.analyzer.growth.algorithm;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.GraphSnapshot;
import com.example.anuviya.analyzer.growth.model.SccCluster;

import java.util.*;

public class SccAnalyzer {

    private final Map<String, Integer> indexMap =
            new HashMap<>();

    private final Map<String, Integer> lowLinkMap =
            new HashMap<>();

    private final Deque<String> stack =
            new ArrayDeque<>();

    private final Set<String> onStack =
            new HashSet<>();

    private final List<SccCluster> clusters =
            new ArrayList<>();

    private int index = 0;

    public List<SccCluster> analyze(
            AnalysisContext context
    ) {

        indexMap.clear();
        lowLinkMap.clear();
        stack.clear();
        onStack.clear();
        clusters.clear();
        index = 0;

        GraphSnapshot snapshot =
                context.getDependencyGraph()
                        .snapshot();

        Map<String, Set<String>> graph =
                snapshot.globalDependencies();

        for (String node : graph.keySet()) {

            if (!indexMap.containsKey(node)) {

                strongConnect(
                        node,
                        graph
                );
            }
        }

        return clusters;
    }

    private void strongConnect(
            String node,
            Map<String, Set<String>> graph
    ) {

        indexMap.put(
                node,
                index
        );

        lowLinkMap.put(
                node,
                index
        );

        index++;

        stack.push(node);

        onStack.add(node);

        for (String neighbor :
                graph.getOrDefault(
                        node,
                        Set.of()
                )) {

            if (!indexMap.containsKey(neighbor)) {

                strongConnect(
                        neighbor,
                        graph
                );

                lowLinkMap.put(
                        node,
                        Math.min(
                                lowLinkMap.get(node),
                                lowLinkMap.get(neighbor)
                        )
                );
            }

            else if (onStack.contains(neighbor)) {

                lowLinkMap.put(
                        node,
                        Math.min(
                                lowLinkMap.get(node),
                                indexMap.get(neighbor)
                        )
                );
            }
        }

        if (Objects.equals(
                lowLinkMap.get(node),
                indexMap.get(node)
        )) {

            Set<String> component =
                    new HashSet<>();

            String current;

            do {

                current = stack.pop();

                onStack.remove(current);

                component.add(current);

            } while (!current.equals(node));

            clusters.add(
                    new SccCluster(
                            "SCC-" + clusters.size(),
                            component,
                            component.size()
                    )
            );
        }
    }
}
