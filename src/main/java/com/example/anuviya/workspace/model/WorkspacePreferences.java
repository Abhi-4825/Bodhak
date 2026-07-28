package com.example.anuviya.workspace.model;

public record WorkspacePreferences(
    String theme,
    String lastSelectedModel,
    String lastOpenedProjectId,
    double sidebarWidth,
    double windowWidth,
    double windowHeight,
    boolean maximized
) {}
