//package com.example.bodhakfrontend.projectAnalysis;
//
//
//import com.example.bodhakfrontend.Models.*;
//
//import java.util.*;
//
//public class ClassHealthAnalyzer {
//
//    public Map<String, ClassHealthInfo> analyze(
//            List<ClassInfo> classInfos,
//            ClassDependencyInfo classDependencyInfo
//    ) {
//
//        Map<String, ClassHealthInfo> result = new HashMap<>();
//        //class indexing
//        Map<String, DependencyNode> dependencyNodeMap = classDependencyInfo.getClassInfo();
//
//        // Pre-compute reverse deps once
//        Map<String, Set<String>> incoming =
//                classDependencyInfo.getReverseClassDependencies();
//
//        Map<String, Set<String>> outgoing =
//                classDependencyInfo.getClassDependencies();
//
//        Set<String> circularClasses =
//                classDependencyInfo.getClassesInCycles(); // you already added this 👍
//
//        for (ClassInfo info : classInfos) {
//
//            String name = info.getName();
//
//            int out = outgoing.getOrDefault(name, Set.of()).size();
//            int in = incoming.getOrDefault(name, Set.of()).size();
//
//            ClassHealthInfo health = new ClassHealthInfo(name);
//            health.setDependencyNode(dependencyNodeMap.get(name));
//            health.setLoc(info.getLinesOfCode());
//            health.setMethodCount(info.getMethodCount());
//            health.setFieldCount(info.getFieldCount());
//            health.setOutgoingDependencies(out);
//            health.setIncomingDependencies(in);
//            // we will detect issue
//            detectIssues(info,out,in,circularClasses, health);
//            result.put(name, health);
//
//        }
//
//
//        return result;
//    }
//
//
//    private void detectIssues(ClassInfo classInfo, int depOut, int depIn,Set<String> circularClasses, ClassHealthInfo classHealthInfo) {
//
//        //god class
//        if (isGodClass(classInfo)) {
//            classHealthInfo.addIssue(IssueType.GOD_CLASS);
//        }
//        //high coupling
//        if (depIn + depOut > 10) {
//            classHealthInfo.addIssue(IssueType.HIGH_COUPLING);
//        }
//        //circular dependency
//        if(circularClasses.contains(classInfo.getName())) {
//            classHealthInfo.addIssue(IssueType.CIRCULAR_DEPENDENCY);
//        }
//        //Anemic Domain
//        if(classInfo.getFieldCount() >=3 && classInfo.getMethodCount() <=2) {
//            classHealthInfo.addIssue(IssueType.ANEMIC_DOMAIN);
//        }
//        //
//
//    }
//
//
//    // ---------- Rules (simple for now) ----------
//    private boolean isGodClass(ClassInfo info) {
//        return info.getLinesOfCode() > 500
//                || info.getMethodCount() > 20
//                || info.getFieldCount() > 15;
//    }
//
//    private void evaluateSmells(
//            ClassInfo info,
//            int fanIn,
//            int fanOut,
//            boolean inCycle,
//            Set<String> warnings
//    ) {
//
//        if (isGodClass(info)) {
//            warnings.add("God Class: too large, split responsibilities");
//        }
//
//        if (fanIn + fanOut > 10) {
//            warnings.add("Highly Coupled: many dependencies");
//        }
//
//        if (inCycle) {
//            warnings.add("Circular Dependency: tightly coupled");
//        }
//
//        if (info.getFieldCount() >= 3 && info.getMethodCount() <= 2) {
//            warnings.add("Data Holder: logic may be outside the class");
//        }
//    }
//
//}
