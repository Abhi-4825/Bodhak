package com.example.bodhakfrontend.Backend.models.Class;

import java.io.File;
import java.util.List;

public class MethodInfo {
    private String methodName;
    private String returnType;
    private List<ParameterInfo> parameters;
    private List<MethodCallInfo> calledMethods;
    private List<ModifierKind> modifier;
    private int startLine;
    private int endLine;
    private int startColumn;
    private  boolean isBodyEmpty;
    private  int statementCount;
    private final File sourceFile;

    public MethodInfo(String methodName, String returnType, List<ParameterInfo> parameters, List<MethodCallInfo> calledMethods,
                      List<com.example.bodhakfrontend.Backend.models.Class.ModifierKind> modifier, int startLine, int endLine, int startColumn, boolean isBodyEmpty, int statementCount, File sourceFile) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.calledMethods = calledMethods;
        this.modifier=modifier;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startColumn = startColumn;
        this.isBodyEmpty = isBodyEmpty;
        this.statementCount = statementCount;
        this.sourceFile = sourceFile;
    }
    public String getMethodName() {
        return methodName;
    }
    public String getReturnType() {
        return returnType;
    }
    public List<ParameterInfo> getParameters() {
        return parameters;
    }
    public List<MethodCallInfo> getCalledMethods() {
        return calledMethods;
    }
    public List<ModifierKind> getModifier() {
        return modifier;
    }
    public int getStartLine() {
        return startLine;
    }
    public int getEndLine() {
        return endLine;
    }
    public int getStartColumn() {
        return startColumn;
    }
    public boolean isBodyEmpty() {
        return isBodyEmpty;
    }
    public int getStatementCount() {
        return statementCount;
    }
    public File getSourceFile() {
        return sourceFile;
    }
}
