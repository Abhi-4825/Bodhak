package com.example.bodhakfrontend.IncrementalPart.model.Class;




import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ClassInfo {
    public enum Kind {
        CLASS,
        INTERFACE,
        ENUM,
        RECORDS
    }
    private final String className;
    private final String packageName;
    private final File sourceFile;
    private final Kind kind;
    private final Set<String> fields;
    private final List<MethodInfo> methods;
    private final List<ConstructorInfo> constructors;
    private final Set<String> annotations;
    private final Set<String> dependsOn;
    private final Set<String> usedBy;
    private final Set<Set<String>> circularDependencyGroups;
    private final boolean isAbstract;
    private final boolean isFinal;
    private final boolean isPublic;
    private final int linesOfCode;
    private final int beginLine;
    private final int beginColumn;
    private final ClassContribution classContribution;
    private final Set<IssueType> issueType = new HashSet<>();
    private final List<String> warnings = new ArrayList<>();


    public ClassInfo(String className, String packageName, File sourceFile, Kind kind, Set<String> fields, List<MethodInfo> methods, List<ConstructorInfo> constructors
            , Set<String> annotations, Set<String> dependsOn
            , Set<String> usedBy,
                     Set<Set<String>> circularDependencyGroups, boolean isAbstract, boolean isFinal, boolean isPublic
            , int linesOfCode, int beginLine, int beginColumn, ClassContribution classContribution) {
        this.className = className;
        this.packageName = packageName;
        this.sourceFile = sourceFile;
        this.kind = kind;
        this.fields = fields;
        this.methods = methods;
        this.constructors = constructors;
        this.annotations = annotations;
        this.dependsOn = dependsOn;
        this.usedBy = usedBy;
        this.circularDependencyGroups = circularDependencyGroups;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isPublic = isPublic;
        this.linesOfCode = linesOfCode;
        this.beginLine = beginLine;
        this.beginColumn = beginColumn;
        this.classContribution = classContribution;
    }
    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public Kind getKind() {
        return kind;
    }

    public Set<String> getFields() {
        return fields;
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public List<ConstructorInfo> getConstructors() {
        return constructors;
    }

    public Set<String> getAnnotations() {
        return annotations;
    }

    public Set<String> getDependsOn() {
        return dependsOn;
    }

    public Set<String> getUsedBy() {
        return usedBy;
    }

    public Set<Set<String>> getCircularDependencyGroups() {
        return circularDependencyGroups;
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

    public int getBeginLine() {
        return beginLine;
    }

    public int getBeginColumn() {
        return beginColumn;
    }
    public  ClassContribution getClassContribution() {
        return classContribution;
    }

    @Override
    public String toString() {
        return className.substring(className.lastIndexOf('.') + 1);
    }


    // finding the issue
    private void detectIssues() {

        //god class
        if (isGodClass()) {
            issueType.add(IssueType.GOD_CLASS);
        }
        //high coupling
        if (dependsOn.size() + usedBy.size() > 10) {
            issueType.add(IssueType.HIGH_COUPLING);
        }
        //circular dependency
        if (!circularDependencyGroups.isEmpty()) {
            issueType.add(IssueType.CIRCULAR_DEPENDENCY);
        }
        //Anemic Domain
        if (getFields().size() >= 3 && getMethods().size() <= 2) {
            issueType.add(IssueType.ANEMIC_DOMAIN);
        }
        //

    }


    private boolean isGodClass() {
        return getLinesOfCode() > 500
                || getMethods().size() > 20
                || getFields().size() > 15;
    }

    private void evaluateSmells() {

        if (isGodClass()) {
            warnings.add("God Class: too large, split responsibilities");
        }

        if (dependsOn.size() + usedBy.size() > 10) {
            warnings.add("Highly Coupled: many dependencies");
        }

        if (!circularDependencyGroups.isEmpty()) {
            warnings.add("Circular Dependency: tightly coupled");
        }

        if (getFields().size() >= 3 && getMethods().size() <= 2) {
            warnings.add("Data Holder: logic may be outside the class");
        }
    }


    // get issue


    public Set<IssueType> getIssueType() {
        issueType.clear();
        detectIssues();
        return issueType;
    }

    //get warnings
    public List<String> getWarnings() {
        warnings.clear();
        evaluateSmells();
        return warnings;
    }




}
