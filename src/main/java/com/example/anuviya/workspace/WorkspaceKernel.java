package com.example.anuviya.workspace;

import com.example.anuviya.workspace.store.JsonWorkspaceStore;
import com.example.anuviya.workspace.store.WorkspaceStore;

import java.io.File;

public final class WorkspaceKernel {
    private static final WorkspaceKernel INSTANCE = new WorkspaceKernel();
    private final File workspaceRoot;

    private WorkspaceKernel() {
        String userHome = System.getProperty("user.home");
        this.workspaceRoot = new File(userHome, ".anuviya/workspace");
    }

    public static WorkspaceKernel getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        WorkspaceStore store = new JsonWorkspaceStore(workspaceRoot);
        WorkspaceRecovery.getInstance().validate(workspaceRoot, store);
        WorkspaceManager.getInstance().initialize(store, workspaceRoot);
    }

    public File getWorkspaceRoot() {
        return workspaceRoot;
    }
}
