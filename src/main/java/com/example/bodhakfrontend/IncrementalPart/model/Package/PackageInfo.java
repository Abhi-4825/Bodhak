package com.example.bodhakfrontend.IncrementalPart.model.Package;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;


import java.util.Set;


public class PackageInfo {

    private final String packageName;
    private  final Set<PackageWarning> warnings;
    // Classes inside this package
    private final Set<ClassInfo> classes;

    // Packages this package depends on
    private final Set<String> dependsOn;

    // Packages that depend on this package
    private final Set<String> usedBy;
    // cycles for Circular dependencies
    private final Set<Set<String>> circularDependencies;
    // circular, warnings, metrics
    private boolean partOfCycle;

    public PackageInfo(
            String packageName,
            Set<ClassInfo> classes,
            Set<String> dependsOn,
            Set<String> usedBy,
            Set<Set<String>> circularDependencies,
            boolean partOfCycle,
            Set<PackageWarning> warnings
    ) {
        this.packageName = packageName;
        this.classes = classes;
        this.dependsOn = dependsOn;
        this.usedBy = usedBy;
        this.circularDependencies=circularDependencies;
        this.partOfCycle=partOfCycle;
        this.warnings=warnings;
    }

    public String getPackageName() { return packageName; }
    public Set<ClassInfo> getClasses() { return classes; }
    public Set<String> getDependsOn() { return dependsOn; }
    public Set<String> getUsedBy() { return usedBy; }

    public Set<PackageWarning> getWarnings() {
        return warnings;
    }

    public Set<Set<String>> getCircularDependencies() {
        return circularDependencies;
    }

    public boolean isPartOfCycle() { return partOfCycle; }
    public void setPartOfCycle(boolean value) { this.partOfCycle = value; }
}

