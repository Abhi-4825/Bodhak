package com.example.bodhakfrontend.Models;

import java.util.ArrayList;
import java.util.List;

public class EntryPointInfo {
    public enum ProjectType {
        SPRING_BOOT,
        JAVAFX,
        PLAIN_JAVA
    }

    private ProjectType projectType;
    private String primaryEntry;
    private List<String> secondaryEntries = new ArrayList<>();

    // getters + constructor

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
}

