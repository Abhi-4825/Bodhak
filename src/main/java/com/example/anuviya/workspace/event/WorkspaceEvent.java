package com.example.anuviya.workspace.event;

import com.example.anuviya.workspace.model.AnalysisSession;
import com.example.anuviya.workspace.model.ProjectIntelligence;
import com.example.anuviya.workspace.model.WorkspaceProject;

import java.util.UUID;

public sealed interface WorkspaceEvent {
    record ProjectRegistered(WorkspaceProject project) implements WorkspaceEvent {}
    record ProjectOpened(WorkspaceProject project) implements WorkspaceEvent {}
    record AnalysisCompleted(UUID projectId, AnalysisSession session, ProjectIntelligence intelligence) implements WorkspaceEvent {}
    record SessionSaved(AnalysisSession session) implements WorkspaceEvent {}
    record CacheInvalidated(UUID projectId) implements WorkspaceEvent {}
    record WorkspaceUpdated() implements WorkspaceEvent {}
}
