package com.example.bodhak.context;


import java.util.*;

public class CircularDependency {


    private int index = 0;
    private final Stack<String> stack = new Stack<>();
    private final Map<String, Integer> indices = new HashMap<>();
    private final Map<String, Integer> lowlinks = new HashMap<>();
    private final Set<String> onStack = new HashSet<>();
    private final Set<Set<String>> stronglyConnectedComponents = new HashSet<>();

    public Set<Set<String>> findCircularDependency(Map<String, Set<String>> dependencies) {
        index = 0;
        stack.clear();
        indices.clear();
        lowlinks.clear();
        onStack.clear();
        stronglyConnectedComponents.clear();

        for (String node : dependencies.keySet()) {
            if (!indices.containsKey(node)) {
                strongconnect(node, dependencies);
            }
        }
        
        Set<Set<String>> filtered = new HashSet<>();
        for (Set<String> scc : stronglyConnectedComponents) {
            if (scc.size() > 1) {
                filtered.add(scc);
            } else if (scc.size() == 1) {
                // Check if it's a self-loop
                String n = scc.iterator().next();
                if (dependencies.containsKey(n) && dependencies.get(n).contains(n)) {
                    filtered.add(scc);
                }
            }
        }
        return filtered;
    }

    private void strongconnect(String v, Map<String, Set<String>> dependencies) {
        indices.put(v, index);
        lowlinks.put(v, index);
        index++;
        stack.push(v);
        onStack.add(v);

        Set<String> successors = dependencies.get(v);
        if (successors != null) {
            for (String w : successors) {
                if (!indices.containsKey(w)) {
                    strongconnect(w, dependencies);
                    lowlinks.put(v, Math.min(lowlinks.get(v), lowlinks.get(w)));
                } else if (onStack.contains(w)) {
                    lowlinks.put(v, Math.min(lowlinks.get(v), indices.get(w)));
                }
            }
        }

        if (lowlinks.get(v).equals(indices.get(v))) {
            Set<String> scc = new HashSet<>();
            String w;
            do {
                w = stack.pop();
                onStack.remove(w);
                scc.add(w);
            } while (!v.equals(w));
            stronglyConnectedComponents.add(scc);
        }
    }
}
