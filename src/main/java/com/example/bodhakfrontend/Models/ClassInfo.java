package com.example.bodhakfrontend.Models;

import java.io.File;


public class ClassInfo {
   private String name;
   private String pkg;
    private ClassKind kind;
  private int  methodCount;
   private int fieldCount;
    private int constructorCount;
    private boolean isAbstract;
    private boolean isFinal;
   private boolean isPublic;
   private int linesOfCode;
   private int beginLine;
   private File sourceFile;

    public ClassInfo(String name, String pkg, ClassKind kind, int methodCount, int fieldCount, int constructorCount, boolean isAbstract, boolean isFinal, boolean isPublic, int linesOfCode, File sourceFile,int beginLine) {
        this.name = name;
        this.pkg = pkg;
        this.kind = kind;
        this.methodCount = methodCount;
        this.fieldCount = fieldCount;
        this.constructorCount = constructorCount;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isPublic = isPublic;
        this.linesOfCode = linesOfCode;
        this.sourceFile = sourceFile;
        this.beginLine = beginLine;
    }

    public String getName() {
        return name;
    }

    public String getPkg() {
        return pkg;
    }

    public ClassKind getKind() {
        return kind;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public int getConstructorCount() {
        return constructorCount;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public File getSourceFile() {
        return sourceFile;
    }
    public int getBeginLine() {return beginLine;}
}
