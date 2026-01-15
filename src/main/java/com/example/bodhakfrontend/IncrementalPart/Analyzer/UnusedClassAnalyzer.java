package com.example.bodhakfrontend.IncrementalPart.Analyzer;


import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.EntryPointInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.UnusedClassInfo;

import java.util.List;
import java.util.Set;
import java.util.*;

public class UnusedClassAnalyzer {

    public enum Mode {
        PROD,   // ignore tests
        DEV,    // include tests
        STRICT  // report tests separately
    }

    public Set<UnusedClassInfo> analyze(
            List<ClassInfo> allClasses,
            EntryPointInfo entryPointInfo,
            Mode mode
    ) {

        Map<String, ClassInfo> classMap = new HashMap<>();
        for (ClassInfo c : allClasses) {
            classMap.put(c.getClassName(), c);
        }

        // Roots
        Set<String> explicitRoots = new HashSet<>();
        entryPointInfo.getAllRoots()
                .forEach(e -> explicitRoots.add(e.className()));

        // Framework roots
        Set<String> frameworkRoots = new HashSet<>();
        if (entryPointInfo.getProjectFlavors()
                .contains(EntryPointInfo.ProjectFlavor.SPRING_BOOT)) {

            for (ClassInfo c : allClasses) {
                if (isSpringComponent(c)) {
                    frameworkRoots.add(c.getClassName());
                }
            }
        }

        // Reachability
        Set<String> reachable = new HashSet<>();
        explicitRoots.forEach(r -> dfs(r, classMap, reachable));
        frameworkRoots.forEach(r -> dfs(r, classMap, reachable));

        Set<UnusedClassInfo> result = new HashSet<>();

        for (ClassInfo c : allClasses) {

            if (mode == Mode.PROD && c.getClassContribution().isTestClass()) {
                continue;
            }

            String name = c.getClassName();

            if (explicitRoots.contains(name)) {
                continue; // entry points are always used
            }

            if (reachable.contains(name)) {
                continue; // used transitively
            }

            // Now classify
            if (isSpringComponent(c)) {
                result.add(new UnusedClassInfo(
                        c,
                        UnusedClassInfo.UsageStatus.FRAMEWORK_REACHABLE,
                        "Only reachable via Spring component scanning"
                ));
            } else {
                result.add(new UnusedClassInfo(
                        c,
                        UnusedClassInfo.UsageStatus.UNUSED,
                        "No reachable path from any entry point"
                ));
            }
        }

        return result;
    }


    private void dfs(
            String className,
            Map<String, ClassInfo> classMap,
            Set<String> visited
    ) {
        if (!visited.add(className)) return;

        ClassInfo cls = classMap.get(className);
        if (cls == null) return;

        for (String dep : cls.getDependsOn()) {
            dfs(dep, classMap, visited);
        }
    }

    private boolean isSpringComponent(ClassInfo c) {
        Set<String> ann = c.getAnnotations();
        return ann.contains("Component")
                || ann.contains("Service")
                || ann.contains("Repository")
                || ann.contains("Controller")
                || ann.contains("RestController")
                || ann.contains("Configuration")
                || ann.contains("SpringBootApplication");
    }
}

