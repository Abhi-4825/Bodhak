package com.example.bodhakfrontend.dependency;

import java.util.*;

public class CircularDependency {
    public Set<Set<String>> findCircularDependency(
            Map<String,Set<String>> depdencyGraph
    ) {
        Set<String> visited = new HashSet<>();
        List<String> stack = new ArrayList<>();
        Set<Set<String>> cycles = new HashSet<>();

        for (String cls : depdencyGraph.keySet()) {
            if (!visited.contains(cls)) {
                dfs(cls, depdencyGraph, visited, stack, cycles);
            }
        }

        return cycles;
    }

private void dfs(
        String className,
        Map<String, Set<String>> dependencyGraph,
        Set<String> visited,
        List<String> stack,
        Set<Set<String>> cycles
) {
    //  Cycle detected
    if (stack.contains(className)) {
        int index = stack.indexOf(className);
        Set<String> cycle =
                new HashSet<>(stack.subList(index, stack.size()));
        cycles.add(cycle);
        return;
    }

    if (visited.contains(className)) return;

    visited.add(className);
    stack.add(className);

    Set<String> deps =dependencyGraph
                    .getOrDefault(className, Set.of());

    for (String dep : deps) {
        dfs(dep, dependencyGraph, visited, stack, cycles);
    }

    //  backtrack
    stack.remove(stack.size() - 1);
}

}
