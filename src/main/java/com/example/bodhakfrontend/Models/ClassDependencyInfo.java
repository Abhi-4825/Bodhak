package com.example.bodhakfrontend.Models;

import com.example.bodhakfrontend.dependency.CircularDependency;

import java.util.*;

public class ClassDependencyInfo {
    private Map<String,List<ConstructorInfo>> constructorInfos;
    private Map<String , List<MethodsInfo>> methods;
    private Map<String, DependencyNode> classInfo;
    private Map<String, Set<String>> classDependencies;
    private Map<String, Set<String>> reverseClassDependencies;
    private Set<Set<String>> circularDependencyGroups;
    private Set<String> classesInCycles;

    public ClassDependencyInfo(Map<String,List<ConstructorInfo>> constructorInfos,Map<String, DependencyNode> classInfo, Map<String, Set<String>> classDependencies, Map<String, Set<String>> reverseClassDependencies, Set<Set<String>> circularDependencyGroups,Map<String,List<MethodsInfo>> methods) {
        this.classInfo = classInfo;
        this.classDependencies = classDependencies;
        this.reverseClassDependencies = reverseClassDependencies;
        this.circularDependencyGroups = circularDependencyGroups;
        this.classesInCycles = new HashSet<>();
        for(Set<String> set : circularDependencyGroups){
            classesInCycles.addAll(set);
        }
        this.methods = methods;
        this.constructorInfos = constructorInfos;
    }

    public Map<String, List<ConstructorInfo>> getConstructorInfos() {
        return constructorInfos;
    }

    public Map<String, Set<String>> getReverseClassDependencies() {
        return reverseClassDependencies;
    }

    public Map<String, Set<String>> getClassDependencies() {
        return classDependencies;
    }

    public Map<String, DependencyNode> getClassInfo() {
        return classInfo;
    }

    public Set<Set<String>> getCircularDependencyGroups() {
        return circularDependencyGroups;
    }
    public Optional<Set<String>> getCycleForClass(String className) {
        return circularDependencyGroups.stream().filter(group -> group.contains(className))
                .findFirst();
    }

    public Set<String> getClassesInCycles() {
        return classesInCycles;
    }
    public Map<String, List<MethodsInfo>> getMethods() {
        return methods;
    }

    public void updateClass(
            String className,
            DependencyNode node,
            Set<String> dependencies,
            List<MethodsInfo> methods,
            List<ConstructorInfo> constructors
    ) {
        // 1. Remove old reverse edges
        Set<String> oldDeps = classDependencies.getOrDefault(className, Set.of());
        for (String dep : oldDeps) {
            Set<String> rev = reverseClassDependencies.get(dep);
            if (rev != null) {
                rev.remove(className);
                if (rev.isEmpty()) {
                    reverseClassDependencies.remove(dep);
                }
            }
        }

        // 2. Update main stores
        classInfo.put(className, node);
        classDependencies.put(className, new HashSet<>(dependencies));
        this.methods.put(className, methods);
        constructorInfos.put(className, constructors);

        // 3. Add new reverse edges
        for (String dep : dependencies) {
            reverseClassDependencies
                    .computeIfAbsent(dep, k -> new HashSet<>())
                    .add(className);
        }

        // 4. Recompute cycles (for now full recompute – safe)
        recomputeCycles();
    }

    public void removeClass(String className) {

        // Remove outgoing deps
        Set<String> deps = classDependencies.remove(className);
        if (deps != null) {
            for (String dep : deps) {
                Set<String> rev = reverseClassDependencies.get(dep);
                if (rev != null) {
                    rev.remove(className);
                    if (rev.isEmpty()) {
                        reverseClassDependencies.remove(dep);
                    }
                }
            }
        }

        // Remove incoming deps
        Set<String> incoming = reverseClassDependencies.remove(className);
        if (incoming != null) {
            for (String src : incoming) {
                Set<String> s = classDependencies.get(src);
                if (s != null) {
                    s.remove(className);
                }
            }
        }

        // Remove metadata
        classInfo.remove(className);
        methods.remove(className);
        constructorInfos.remove(className);

        recomputeCycles();
    }
    private void recomputeCycles() {
        CircularDependency detector = new CircularDependency();
        circularDependencyGroups =
                detector.findCircularDependency(classDependencies);

        classesInCycles.clear();
        for (Set<String> group : circularDependencyGroups) {
            classesInCycles.addAll(group);
        }
    }



}
