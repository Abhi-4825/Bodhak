//package com.example.bodhakfrontend.Models;
//
//import java.io.File;
//import java.util.Set;
//
//
//public class ClassInfo {
//   private String name;
//   private String pkg;
//    private ClassKind kind;
//  private int  methodCount;
//   private int fieldCount;
//    private int constructorCount;
//    private final Set<String> annotations;
//    private boolean isAbstract;
//    private boolean isFinal;
//   private boolean isPublic;
//   private int linesOfCode;
//   private int beginLine;
//   private int beginColumn;
//   private File sourceFile;
//
//    public ClassInfo(String name, String pkg, ClassKind kind, int methodCount, int fieldCount, int constructorCount, Set<String> annotations, boolean isAbstract, boolean isFinal, boolean isPublic, int linesOfCode, File sourceFile, int beginLine,int beginColumn) {
//        this.name = name;
//        this.pkg = pkg;
//        this.kind = kind;
//        this.methodCount = methodCount;
//        this.fieldCount = fieldCount;
//        this.constructorCount = constructorCount;
//        this.annotations = annotations;
//        this.isAbstract = isAbstract;
//        this.isFinal = isFinal;
//        this.isPublic = isPublic;
//        this.linesOfCode = linesOfCode;
//        this.sourceFile = sourceFile;
//        this.beginLine = beginLine;
//        this.beginColumn = beginColumn;
//    }
//    public int getBeginColumn() {
//        return beginColumn;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getPkg() {
//        return pkg;
//    }
//
//    public ClassKind getKind() {
//        return kind;
//    }
//
//    public int getMethodCount() {
//        return methodCount;
//    }
//
//    public int getFieldCount() {
//        return fieldCount;
//    }
//
//    public int getConstructorCount() {
//        return constructorCount;
//    }
//
//    public boolean isAbstract() {
//        return isAbstract;
//    }
//
//    public boolean isFinal() {
//        return isFinal;
//    }
//
//    public boolean isPublic() {
//        return isPublic;
//    }
//
//    public int getLinesOfCode() {
//        return linesOfCode;
//    }
//
//    public File getSourceFile() {
//        return sourceFile;
//    }
//    public int getBeginLine() {return beginLine;}
//    public Set<String> getAnnotations() {
//        return annotations;
//    }
//    public boolean hasAnyAnnotation(String... names) {
//        if (annotations == null || annotations.isEmpty()) {
//            return false;
//        }
//        for (String annotation : names) {
//            if (annotations.contains(annotation)) {return true;}
//        }
//        return false;
//    }
//}
