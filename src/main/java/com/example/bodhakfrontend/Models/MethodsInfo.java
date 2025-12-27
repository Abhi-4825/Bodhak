package com.example.bodhakfrontend.Models;

import java.io.File;
import java.util.List;
import java.util.Set;
public class MethodsInfo {
    private String className;
    private String methodName;
    private String returnType;
    private List<MethodParameterInfo> parameters;
    private List<MethodCallInfo> calledMethods;
    private List<ModifierKind> modifier;
    private int startLine;
    private int endLine;
    private File sourceFile;
    private final boolean isBodyEmpty;

    public MethodsInfo(String className, String methodName, String returnType, List<MethodParameterInfo> parameters, List<MethodCallInfo> calledMethods, List<ModifierKind> modifier, int startLine, int endLine, File sourceFile, boolean isBodyEmpty) {
        this.className = className;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.calledMethods = calledMethods;
        this.modifier = modifier;
        this.startLine = startLine;
        this.endLine = endLine;
        this.sourceFile = sourceFile;
        this.isBodyEmpty = isBodyEmpty;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<MethodParameterInfo> getParameters() {
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

    public File getSourceFile() {
        return sourceFile;
    }

}





