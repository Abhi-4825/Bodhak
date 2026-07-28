package com.example.anuviya.workspace;

import com.example.anuviya.workspace.model.*;
import com.example.anuviya.workspace.store.WorkspaceStore;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public final class WorkspaceManager {
    private static final WorkspaceManager INSTANCE = new WorkspaceManager();

    private WorkspaceStore store;
    private ProjectManager projectManager;
    private AnalysisSessionManager sessionManager;
    private CacheManager cacheManager;
    private WorkspaceProject currentProject;

    private WorkspaceManager() {}

    public WorkspaceProject getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(WorkspaceProject currentProject) {
        this.currentProject = currentProject;
    }

    public static WorkspaceManager getInstance() {
        return INSTANCE;
    }

    public void initialize(WorkspaceStore store, File workspaceRoot) {
        this.store = store;
        this.projectManager = new ProjectManager(store);
        this.sessionManager = new AnalysisSessionManager(workspaceRoot);
        this.cacheManager = new CacheManager(workspaceRoot);
    }

    public WorkspaceStore getStore() {
        return store;
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }

    public AnalysisSessionManager getSessionManager() {
        return sessionManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    // Façade helper delegations
    public WorkspaceProject openProject(File folder) {
        return projectManager.registerProject(folder);
    }

    public void touchOpened(UUID id) {
        projectManager.touchLastOpened(id);
    }

    public void recordAnalysisSession(AnalysisSession session, ProjectIntelligence intel) {
        sessionManager.saveSession(session);
        sessionManager.saveIntelligence(intel);
        
        Optional<WorkspaceProject> pOpt = store.findProject(session.projectId());
        if (pOpt.isPresent()) {
            WorkspaceProject p = pOpt.get();
            WorkspaceProject updated = new WorkspaceProject(
                p.id(), p.name(), p.location(), p.language(), p.buildTool(), p.projectType(),
                p.compilerVersion(), p.analysisVersion(), p.created(), p.lastOpened(),
                session.timestamp(), session.sessionId(), p.fingerprint(), p.pinned(), p.favorite(), p.status()
            );
            store.saveProject(updated);
        }
    }

    public boolean isCacheValid(UUID id, File root, String key) {
        return cacheManager.isCacheValid(id, root, key);
    }

    public void writeCacheEntry(UUID id, File root, String key) {
        cacheManager.writeCacheEntry(id, root, key);
    }

    public List<WorkspaceProject> getRecentProjects() {
        if (projectManager == null) return List.of();
        return projectManager.getRecentProjects(50);
    }

    public Map<LocalDate, List<AnalysisSession>> getTimeline(UUID projectId) {
        return sessionManager.getTimeline(projectId);
    }

    public WorkspacePreferences loadPreferences() {
        return store.loadPreferences();
    }

    public void savePreferences(WorkspacePreferences prefs) {
        store.savePreferences(prefs);
    }
}
