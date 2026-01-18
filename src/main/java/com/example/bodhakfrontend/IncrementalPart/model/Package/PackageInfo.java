package com.example.bodhakfrontend.IncrementalPart.model.Package;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;


import java.util.HashSet;

import java.util.Set;


public class PackageInfo {

    private final String packageName;
    private  final Set<PackageWarning> warnings=new HashSet<>();
    // Classes inside this package
    private final Set<ClassInfo> classes=new HashSet<>();

    // Packages this package depends on
    private final Set<String> dependsOn=new HashSet<>();

    // Packages that depend on this package
    private final Set<String> usedBy=new HashSet<>();
    // cycles for Circular dependencies
    private final Set<Set<String>> circularDependencies=new HashSet<>();
    // circular, warnings, metrics
    private boolean partOfCycle;

    public PackageInfo(
            String packageName

    ) {
        this.packageName = packageName;

    }

    // getters
    public String getPackageName() { return packageName; }
    public Set<ClassInfo> getClasses() { return classes; }
    public Set<String> getDependsOn() { return dependsOn; }
    public Set<String> getUsedBy() { return usedBy; }
    public Set<Set<String>> getCircularDependencies() { return circularDependencies; }
    public Set<PackageWarning> getWarnings() { return warnings; }

    public boolean isPartOfCycle() { return partOfCycle; }
    public void setPartOfCycle(boolean value) { this.partOfCycle = value; }

    // 🔥 controlled update helpers
    public void setWarnings(Set<PackageWarning> newWarnings) {
        warnings.clear();
        warnings.addAll(newWarnings);
    }

    public void setCircularDependencies(Set<Set<String>> cycles) {
        circularDependencies.clear();
        circularDependencies.addAll(cycles);
        partOfCycle = !cycles.isEmpty();
    }

}

