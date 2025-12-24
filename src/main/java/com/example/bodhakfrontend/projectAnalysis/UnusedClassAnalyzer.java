package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.Models.*;

import java.util.*;

public class UnusedClassAnalyzer {

    public List<UnusedClassInfo> analyze(
            List<ClassInfo> classes,
            ClassDependencyInfo dependencyInfo,
            EntryPointInfo entryPointInfo
    ) {

        List<UnusedClassInfo> unused = new ArrayList<>();

        Set<String> entryPoints = new HashSet<>();
        if (entryPointInfo.getPrimaryEntry() != null) {
            entryPoints.add(entryPointInfo.getPrimaryEntry());
        }
        entryPoints.addAll(entryPointInfo.getSecondaryEntries());

        Map<String, Set<String>> incoming =
                dependencyInfo.getReverseClassDependencies();

        Set<String> cycleClasses =
                dependencyInfo.getClassesInCycles();

        for (ClassInfo cls : classes) {
            DependencyNode dependencyNode=dependencyInfo.getClassInfo().get(cls.getName());
            String name = cls.getName();

            boolean hasIncoming =
                    incoming.containsKey(name)
                            && !incoming.get(name).isEmpty();

            boolean isEntryPoint =
                    entryPoints.contains(name);

            boolean inCycle =
                    cycleClasses.contains(name);

            if (!hasIncoming && !isEntryPoint && !inCycle) {
                unused.add(
                        new UnusedClassInfo(
                                name,
                                dependencyNode,
                                cls.getPkg(),
                                cls.getLinesOfCode()
                        )
                );
            }
        }

        return unused;
    }
}

