package com.example.bodhakfrontend.Models;

public class IssueContext {

    private final IssueType issueType;

    //signals for class issues
    private final int loc;
    private final int methodCount;
    private final int outgoingDependencies;

    //design
    private final boolean hasCircularDependencies;

    // identity
    private final String className;
    private final String methodName;


    public IssueContext(IssueType issueType, int loc, int methodCount, int outgoingDependencies, boolean hasCircularDependencies, String className, String methodName) {
        this.issueType = issueType;
        this.loc = loc;
        this.methodCount = methodCount;
        this.outgoingDependencies = outgoingDependencies;
        this.hasCircularDependencies = hasCircularDependencies;
        this.className = className;
        this.methodName = methodName;
    }

    public IssueType getIssueType() {
        return issueType;
    }
    public int getLoc() {
        return loc;
    }
    public int getMethodCount() {
        return methodCount;
    }
    public int getOutgoingDependencies() {
        return outgoingDependencies;
    }
    public boolean hasCircularDependencies() {
        return hasCircularDependencies;
    }
    public String getClassName() {
        return className;
    }
    public String getMethodName() {
        return methodName;
    }}
