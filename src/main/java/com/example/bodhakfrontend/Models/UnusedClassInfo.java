package com.example.bodhakfrontend.Models;

public class UnusedClassInfo {

    private final String className;
    private final DependencyNode dependencyNode;
    private final String packageName;
    private final int loc;

    public UnusedClassInfo(
            String className,
            DependencyNode dependencyNode,
            String packageName,
            int loc
    ) {
        this.className = className;
        this.dependencyNode = dependencyNode;
        this.packageName = packageName;
        this.loc = loc;
    }

    // getters

    public String getClassName() {
        return className;
    }

    public DependencyNode getDependencyNode() {
        return dependencyNode;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getLoc() {
        return loc;
    }
}

