package com.example.bodhakfrontend.dependency;

import java.util.*;

public class CircularDependency {

    public Set<Set<String>> findCircularDependency(
            Map<String, Set<String>> dependencyGraph
    ) {
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<Set<String>> cycles = new HashSet<>();

        for (String cls : dependencyGraph.keySet()) {
            if (!visited.contains(cls)) {
                dfs(cls, dependencyGraph, visited, inStack, stack, cycles);
            }
        }

        return cycles;
    }

    private void dfs(
            String className,
            Map<String, Set<String>> graph,
            Set<String> visited,
            Set<String> inStack,
            Deque<String> stack,
            Set<Set<String>> cycles
    ) {
        if (inStack.contains(className)) {
            // extract cycle
            Set<String> cycle = new HashSet<>();
            Iterator<String> it = stack.iterator();
            while (it.hasNext()) {
                String s = it.next();
                cycle.add(s);
                if (s.equals(className)) break;
            }
            cycles.add(cycle);
            return;
        }

        if (visited.contains(className)) return;

        visited.add(className);
        inStack.add(className);
        stack.push(className);

        for (String dep : graph.getOrDefault(className, Set.of())) {
            dfs(dep, graph, visited, inStack, stack, cycles);
        }

        stack.pop();
        inStack.remove(className);
    }
}
