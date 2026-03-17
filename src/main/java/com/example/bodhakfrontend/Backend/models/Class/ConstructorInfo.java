package com.example.bodhakfrontend.Backend.models.Class;

import java.io.File;
import java.util.List;

public class ConstructorInfo {
    private final String constructorName;
    private final List<ParameterInfo> parameters;
    private final List<ModifierKind> modifiers;
    private final int startLine;
    private final int endLine;
    private final int startColumn;
    private final File sourceFile;

    public ConstructorInfo(String constructorName,List<ParameterInfo> parameters, List<ModifierKind> modifiers, int startLine, int endLine, int startColumn, File sourceFile) {
        this.constructorName = constructorName;
        this.parameters = parameters;
        this.modifiers = modifiers;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startColumn = startColumn;
        this.sourceFile = sourceFile;


    }

    public String getConstructorName() {
        return constructorName;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public List<ModifierKind> getModifiers() {
        return modifiers;
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
    public File getSourceFile() {
        return sourceFile;
    }
}
