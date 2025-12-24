package com.example.bodhakfrontend.projectAnalysis;


import com.example.bodhakfrontend.Models.ClassInfo;
import com.example.bodhakfrontend.Models.ClassHealthInfo;
import com.example.bodhakfrontend.Models.ClassDependencyInfo;
import com.example.bodhakfrontend.Models.DependencyNode;

import java.util.*;

public class ClassHealthAnalyzer {

    public Map<String, ClassHealthInfo> analyze(
            List<ClassInfo> classInfos,
            ClassDependencyInfo classDependencyInfo
    ) {

        Map<String, ClassHealthInfo> result = new HashMap<>();
        //class indexing
        Map<String, DependencyNode> dependencyNodeMap = classDependencyInfo.getClassInfo();

        // Pre-compute reverse deps once
        Map<String, Set<String>> incoming =
                classDependencyInfo.getReverseClassDependencies();

        Map<String, Set<String>> outgoing =
                classDependencyInfo.getClassDependencies();

        Set<String> circularClasses =
                classDependencyInfo.getClassesInCycles(); // you already added this 👍

        for (ClassInfo info : classInfos) {

            String name = info.getName();

            int out = outgoing.getOrDefault(name, Set.of()).size();
            int in  = incoming.getOrDefault(name, Set.of()).size();

            boolean godClass = isGodClass(info);
            boolean highlyCoupled = out + in > 10; // threshold (tunable)
            boolean inCycle = circularClasses.contains(name);

            Set<String> warnings = new LinkedHashSet<>();

            if (godClass) warnings.add("God Class (too large)");
            if (highlyCoupled) warnings.add("Highly Coupled");
            if (inCycle) warnings.add("Part of Circular Dependency");

            ClassHealthInfo health = new ClassHealthInfo(
                    name);
            health.setDependencyNode(dependencyNodeMap.get(name));
            health.setLoc(info.getLinesOfCode());
            health.setMethodCount(info.getMethodCount());
            health.setFieldCount(info.getFieldCount());
            health.setOutgoingDependencies(out);
            health.setIncomingDependencies(in);
            health.setGodClass(godClass);
            health.setHighlyCoupled(highlyCoupled);
            health.setInCircularDependency(inCycle);
            health.setWarning(warnings);
            evaluateSmells(info,out,in,inCycle,warnings);
            result.put(name, health);
            
        }


        return result;
    }

    // ---------- Rules (simple for now) ----------
    private boolean isGodClass(ClassInfo info) {
        return info.getLinesOfCode() > 500
                || info.getMethodCount() > 20
                || info.getFieldCount() > 15;
    }
    private void evaluateSmells(
            ClassInfo info,
            int fanIn,
            int fanOut,
            boolean inCycle,
            Set<String> warnings
    ) {

        if (isGodClass(info)) {
            warnings.add("God Class: too large, split responsibilities");
        }

        if (fanIn + fanOut > 10) {
            warnings.add("Highly Coupled: many dependencies");
        }

        if (inCycle) {
            warnings.add("Circular Dependency: tightly coupled");
        }

        if (info.getFieldCount() >= 3 && info.getMethodCount() <= 2) {
            warnings.add("Data Holder: logic may be outside the class");
        }
    }

}
