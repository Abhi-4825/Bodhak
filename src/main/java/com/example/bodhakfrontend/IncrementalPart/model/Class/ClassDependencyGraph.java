package com.example.bodhakfrontend.IncrementalPart.model.Class;

import java.util.Map;
import java.util.Set;

public class ClassDependencyGraph {
    private final Map<String, Set<String>> dependsOn;
    private final Map<String, Set<String>> usedBy;

    public ClassDependencyGraph(Map<String, Set<String>> dependsOn, Map<String, Set<String>> usedBy) {
        this.dependsOn = dependsOn;
        this.usedBy = usedBy;
    }
    public Map<String, Set<String>> getDependsOn() {
        return dependsOn;
    }
    public Map<String, Set<String>> getUsedBy() {
        return usedBy;
    }

}
