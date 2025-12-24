package com.example.bodhakfrontend.Models;


import java.util.HashSet;
import java.util.Set;


public class PackageInfo {

    private final String packageName;
    private  Set<PackageWarning> warnings=new HashSet<>();
    // Classes inside this package
    private final Set<String> classes;

    // Packages this package depends on
    private final Set<String> dependsOn;

    // Packages that depend on this package
    private final Set<String> usedBy;
    // cycles for Circular dependencies
    private Set<Set<String>> circularDependencies=new HashSet<>();
    // (Later) circular, warnings, metrics
    private boolean partOfCycle;

    public PackageInfo(
            String packageName,
            Set<String> classes,
            Set<String> dependsOn,
            Set<String> usedBy
    ) {
        this.packageName = packageName;
        this.classes = classes;
        this.dependsOn = dependsOn;
        this.usedBy = usedBy;
    }

    public String getPackageName() { return packageName; }
    public Set<String> getClasses() { return classes; }
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

