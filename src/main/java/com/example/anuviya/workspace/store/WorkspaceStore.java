package com.example.anuviya.workspace.store;

import com.example.anuviya.workspace.model.WorkspacePreferences;
import com.example.anuviya.workspace.model.WorkspaceProject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceStore {
    void saveProject(WorkspaceProject p);
    Optional<WorkspaceProject> findProject(UUID id);
    Optional<WorkspaceProject> findProjectByPath(String path);
    List<WorkspaceProject> listProjects();
    void deleteProject(UUID id);
    void savePreferences(WorkspacePreferences prefs);
    WorkspacePreferences loadPreferences();
}
