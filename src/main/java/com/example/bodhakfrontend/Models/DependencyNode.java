package com.example.bodhakfrontend.Models;

import java.io.File;

public class DependencyNode {
    private final String className;
    private final String pkg;
    private final File sourceFile;
    private final int beginLine;

    public DependencyNode(String className, String pkg, File sourceFile, int beginLine) {
        this.className = className;
        this.pkg = pkg;
        this.sourceFile = sourceFile;
        this.beginLine = beginLine;
    }

    public String getClassName() { return className; }
    public File getSourceFile() { return sourceFile; }
    public int getBeginLine() { return beginLine; }
    public String getPackageName() { return pkg; }

   public String toString()
    {return className;}
}

