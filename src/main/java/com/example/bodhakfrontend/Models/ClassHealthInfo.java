package com.example.bodhakfrontend.Models;

import java.util.Set;

public class ClassHealthInfo {

    private String className;
    private DependencyNode dependencyNode;

    // Size & complexity
    private int loc;
    private int methodCount;
    private int fieldCount;

    // Dependency signals
    private int outgoingDependencies;
    private int incomingDependencies;

    // Risk flags
    private boolean godClass;
    private boolean highlyCoupled;
    private boolean inCircularDependency;

    // Human-readable warnings
    private Set<String> warnings;

    public ClassHealthInfo(
            String className
    ) {
        this.className = className;

    }

    public void setHighlyCoupled(boolean highlyCoupled) {
        this.highlyCoupled = highlyCoupled;
    }

    public void setGodClass(boolean godClass) {
        this.godClass = godClass;
    }

    public void setIncomingDependencies(int incomingDependencies) {
        this.incomingDependencies = incomingDependencies;
    }

    public void setOutgoingDependencies(int outgoingDependencies) {
        this.outgoingDependencies = outgoingDependencies;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public void setMethodCount(int methodCount) {
        this.methodCount = methodCount;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public void setDependencyNode(DependencyNode dependencyNode) {
        this.dependencyNode = dependencyNode;
    }

    public void setInCircularDependency(boolean inCircularDependency) {
        this.inCircularDependency = inCircularDependency;
    }

    public String getClassName() {
        return className;
    }

    public int getLoc() {
        return loc;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public int getOutgoingDependencies() {
        return outgoingDependencies;
    }

    public int getIncomingDependencies() {
        return incomingDependencies;
    }

    public boolean isGodClass() {
        return godClass;
    }

    public boolean isHighlyCoupled() {
        return highlyCoupled;
    }

    public boolean isInCircularDependency() {
        return inCircularDependency;
    }

    public Set<String> getWarnings() {
        return warnings;
    }

    public DependencyNode getDependencyNode() {
        return dependencyNode;
    }

    @Override
    public String toString() {
        return className;
    }

    public void setWarning(Set<String> warnings) {
        this.warnings = warnings;
    }
}

