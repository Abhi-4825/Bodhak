package com.example.bodhakfrontend.Models.PackageAnalysis;

import java.util.*;

public class EntryPointInfo {
    public enum ProjectType {
        SPRING_BOOT,
        JAVAFX,
        PLAIN_JAVA
    }

    private ProjectType projectType;
    private String primaryEntry;
    private  List<String> secondaryEntries;
    private final Set<String> frameworkRoots=new HashSet<>();

    public EntryPointInfo(ProjectType projectType, String primaryEntry, List<String> secondaryEntries) {
        this.projectType = projectType;
        this.primaryEntry = primaryEntry;
        this.secondaryEntries = secondaryEntries;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public List<String> getSecondaryEntries() {
        return secondaryEntries;
    }

    public String getPrimaryEntry() {
        return primaryEntry;
    }

    public void addFrameworkRoot(String classsName) {
        frameworkRoots.add(classsName);
    }
    public Set<String> getFrameworkRoots() {
        return Collections.unmodifiableSet(frameworkRoots);
    }
    public Set<String> getAllRoots() {
        Set<String> roots = new HashSet<>();
        if(primaryEntry != null) {
            roots.add(primaryEntry);
        }
        roots.addAll(secondaryEntries);
        roots.addAll(frameworkRoots);
        return roots;

    }




}



