package com.example.bodhakfrontend.Backend.models.Project;

import java.util.HashSet;
import java.util.Set;
public class EntryPointInfo {

    public enum ProjectFlavor {
        SPRING_BOOT,
        JAVAFX,
        CLI,
        TEST
    }

    public enum EntryKind {
        MAIN,
        SPRING_BOOT,
        JAVAFX,
        TEST
    }

    public record Entry(String className, EntryKind kind) {}

    private final Set<ProjectFlavor> projectFlavors;
    private final Entry primaryEntry;
    private final Set<Entry> secondaryEntries;
    private final Set<Entry> frameworkRoots = new HashSet<>();

    public EntryPointInfo(
            Set<ProjectFlavor> projectFlavors,
            Entry primaryEntry,
            Set<Entry> secondaryEntries
    ) {
        this.projectFlavors = projectFlavors;
        this.primaryEntry = primaryEntry;
        this.secondaryEntries = secondaryEntries;
    }

    public Set<Entry> getAllRoots() {
        Set<Entry> roots = new HashSet<>();
        if (primaryEntry != null) roots.add(primaryEntry);
        roots.addAll(secondaryEntries);
        roots.addAll(frameworkRoots);
        return roots;
    }

    public Entry getPrimaryEntry() {
        return primaryEntry;
    }
    public Set<Entry> getSecondaryEntries() {
        return secondaryEntries;
    }
    public Set<Entry> getFrameworkRoots() {
        return frameworkRoots;
    }
    public Set<ProjectFlavor> getProjectFlavors() {
        return projectFlavors;
    }
}








