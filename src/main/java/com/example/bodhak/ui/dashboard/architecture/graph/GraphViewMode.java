package com.example.bodhak.ui.dashboard.architecture.graph;

public enum GraphViewMode {
    ARCHITECTURE("Architecture"),
    DEPENDENCY("Dependency"),
    CYCLES("Cycles"),
    HOTSPOTS("Hotspots");

    private final String displayName;

    GraphViewMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
