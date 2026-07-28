package com.example.anuviya.analyzer.growth.algorithm;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.GraphSnapshot;
import com.example.anuviya.analyzer.growth.model.CentralityNode;

import java.util.*;

public class BetweennessCentralityAnalyzer {

    public List<CentralityNode> analyze(AnalysisContext context) {

        GraphSnapshot snapshot = context.getDependencyGraph().snapshot();

        Map<String, Set<String>> graph = buildUndirectedGraph(snapshot);

        Map<String, Double> centrality = new HashMap<>();

        for (String node : graph.keySet()) {
            centrality.put(node, 0.0);
        }

        for (String source : graph.keySet()) {

            Deque<String> stack = new ArrayDeque<>();

            Map<String, List<String>> predecessors = new HashMap<>();

            Map<String, Integer> distance = new HashMap<>();

            Map<String, Double> sigma = new HashMap<>();

            for (String v : graph.keySet()) {

                predecessors.put(v, new ArrayList<>());

                distance.put(v, -1);

                sigma.put(v, 0.0);
            }

            distance.put(source, 0);

            sigma.put(source, 1.0);

            Queue<String> queue = new ArrayDeque<>();

            queue.add(source);

            while (!queue.isEmpty()) {

                String v = queue.poll();

                stack.push(v);

                for (String w : graph.getOrDefault(v, Set.of())) {

                    if (distance.get(w) < 0) {

                        queue.add(w);

                        distance.put(w, distance.get(v) + 1);
                    }

                    if (distance.get(w) == distance.get(v) + 1) {

                        sigma.put(w, sigma.get(w) + sigma.get(v));

                        predecessors.get(w).add(v);
                    }
                }
            }

            Map<String, Double> delta = new HashMap<>();

            for (String v : graph.keySet()) {
                delta.put(v, 0.0);
            }

            while (!stack.isEmpty()) {

                String w = stack.pop();

                for (String v : predecessors.get(w)) {

                    double contribution =

                            (sigma.get(v) / sigma.get(w))

                                    *

                                    (1.0 + delta.get(w));

                    delta.put(v, delta.get(v) + contribution);
                }

                if (!w.equals(source)) {

                    centrality.put(w, centrality.get(w) + delta.get(w));
                }
            }
        }

        return centrality.entrySet().stream().map(entry -> new CentralityNode(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(CentralityNode::score).reversed()).toList();
    }

    private Map<String, Set<String>> buildUndirectedGraph(GraphSnapshot snapshot) {

        Map<String, Set<String>> graph = new HashMap<>();

        snapshot.globalDependencies().forEach((from, targets) -> {

            graph.computeIfAbsent(from, k -> new HashSet<>());

            for (String to : targets) {

                graph.computeIfAbsent(to, k -> new HashSet<>());

                graph.get(from).add(to);

                graph.get(to).add(from);
            }
        });

        return graph;
    }
}
