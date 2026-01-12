package com.example.bodhakfrontend.Models;

import java.util.HashSet;
import java.util.List;
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
    //issue types

    Set<IssueType> issueTypes=new HashSet<>();


    // Risk flags


    public ClassHealthInfo(
            String className
    ) {
        this.className = className;

    }

    public void setIssueTypes(Set<IssueType> issueTypes) {
        this.issueTypes = issueTypes;
    }
    public Set<IssueType> getIssueTypes() {
        return issueTypes;
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

     public void addIssue(IssueType issueType){
        issueTypes.add(issueType);
     }


    public DependencyNode getDependencyNode() {
        return dependencyNode;
    }

    @Override
    public String toString() {
        return className;
    }


    public String getPackage() {
        return dependencyNode.getPackageName();
    }
}

