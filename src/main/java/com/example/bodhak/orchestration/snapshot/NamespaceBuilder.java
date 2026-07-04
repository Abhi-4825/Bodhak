package com.example.bodhak.orchestration.snapshot;

import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.namespace.NamespaceInfo;
import com.example.bodhak.model.namespace.NamespaceWarning;
import com.example.bodhak.context.CircularDependency;

import java.util.*;

/**
 * Builds NamespaceInfo (formerly PackageInfo) from the complete list of entities.
 * Language-neutral — works purely on Namespace/module names in EntityInfo.
 */
public class NamespaceBuilder {
    
    private final CircularDependency circularDependency = new CircularDependency();

    public Map<String, NamespaceInfo> build(List<EntityInfo> entities) {
        Map<String, NamespaceInfo> map = new HashMap<>();
        
        // 1. Group entities by namespace
        for (EntityInfo e : entities) {
            String nsName = e.getNamespaceName();
            if (nsName == null) nsName = "";
            
            NamespaceInfo ns = map.computeIfAbsent(nsName, NamespaceInfo::new);
            ns.getEntities().add(e);
        }
        
        // 2. Discover dependencies between namespaces
        Map<String, Set<String>> nsDependsOn = new HashMap<>();
        Map<String, Set<String>> nsUsedBy = new HashMap<>();
        
        // Build an entity -> namespace map for quick cross-reference
        Map<String, String> entityToNs = new HashMap<>();
        for (EntityInfo e : entities) {
            entityToNs.put(e.getEntityName(), e.getNamespaceName() == null ? "" : e.getNamespaceName());
        }
        
        for (EntityInfo e : entities) {
            String fromNs = e.getNamespaceName() == null ? "" : e.getNamespaceName();
            for (String depEntity : e.getDependsOn()) {
                String toNs = entityToNs.get(depEntity);
                if (toNs != null && !fromNs.equals(toNs)) {
                    nsDependsOn.computeIfAbsent(fromNs, k -> new HashSet<>()).add(toNs);
                    nsUsedBy.computeIfAbsent(toNs, k -> new HashSet<>()).add(fromNs);
                }
            }
        }
        
        // 3. Circular dependencies
        Set<Set<String>> cycles = circularDependency.findCircularDependency(nsDependsOn);

        // 4. Populate the final NamespaceInfo objects
        for (NamespaceInfo ns : map.values()) {
            String name = ns.getNamespaceName();
            ns.getDependsOn().addAll(nsDependsOn.getOrDefault(name, Set.of()));
            ns.getUsedBy().addAll(nsUsedBy.getOrDefault(name, Set.of()));
            
            Set<Set<String>> myCycles = new HashSet<>();
            for (Set<String> cycle : cycles) {
                if (cycle.contains(name)) myCycles.add(cycle);
            }
            ns.setCircularGroups(myCycles);
            
            // Warnings
            Set<NamespaceWarning> warnings = new HashSet<>();
            if (name.isEmpty() || name.equals("default")) {
                warnings.add(NamespaceWarning.DEFAULT_NAMESPACE);
            } else {
                int coupling = ns.getDependsOn().size() + ns.getUsedBy().size();
                if (coupling > 6) warnings.add(NamespaceWarning.HIGH_COUPLING);
                if (ns.getEntities().size() > 20) warnings.add(NamespaceWarning.GOD_NAMESPACE);
                if (ns.getUsedBy().size() >= 4 && ns.getDependsOn().size() <= 1) {
                    warnings.add(NamespaceWarning.HUB_NAMESPACE);
                }
            }
            ns.setWarnings(warnings);

            // Compute Fan-In and Fan-Out
            int fanIn = ns.getUsedBy().size();
            int fanOut = ns.getDependsOn().size();
            ns.setFanIn(fanIn);
            ns.setFanOut(fanOut);

            // Compute Instability Score (0.0 to 1.0)
            double instability = 0.0;
            if ((fanIn + fanOut) > 0) {
                instability = (double) fanOut / (fanIn + fanOut);
            }
            ns.setInstabilityScore(instability);

            // Populate Findings
            for (NamespaceWarning warning : warnings) {
                ns.getFindings().add(warning.name().replace("_", " "));
            }
            if (!ns.getCircularGroups().isEmpty()) {
                ns.getFindings().add("PARTICIPATES IN CYCLE");
            }
            if (instability > 0.8 && fanOut > 5) {
                ns.getFindings().add("HIGHLY UNSTABLE");
            }

            // Compute Risk Score
            double risk = instability * 50.0;
            if (!ns.getCircularGroups().isEmpty()) risk += 30.0;
            if (fanIn + fanOut > 10) risk += 20.0;
            ns.setRiskScore(Math.min(100.0, risk));
        }
        
        return map;
    }
}
