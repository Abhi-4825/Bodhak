package com.example.anuviya.workspace.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WorkspaceState(
    WorkspaceProject currentProject,
    List<WorkspaceProject> recentProjects,
    List<WorkspaceProject> pinnedProjects,
    Map<UUID, ProjectIntelligence> intelligence,
    PlatformStatus platformStatus
) {}
