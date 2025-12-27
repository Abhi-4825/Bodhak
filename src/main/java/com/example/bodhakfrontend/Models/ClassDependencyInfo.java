package com.example.bodhakfrontend.Models;

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
}
