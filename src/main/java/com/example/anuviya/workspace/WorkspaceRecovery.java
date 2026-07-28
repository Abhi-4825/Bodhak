package com.example.anuviya.workspace;

import com.example.anuviya.workspace.model.WorkspaceProject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public final class WorkspaceRecovery {
    private static final WorkspaceRecovery INSTANCE = new WorkspaceRecovery();
    private final ObjectMapper mapper;

    private WorkspaceRecovery() {
        mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static WorkspaceRecovery getInstance() {
        return INSTANCE;
    }

    public void validate(File workspaceRoot, com.example.anuviya.workspace.store.WorkspaceStore store) {
        File projectsDir = new File(workspaceRoot, "projects");
        if (!projectsDir.exists()) return;

        File[] dirs = projectsDir.listFiles(File::isDirectory);
        if (dirs == null) return;

        for (File dir : dirs) {
            File metadataFile = new File(dir, "metadata.json");
            if (metadataFile.exists()) {
                try {
                    WorkspaceProject project = mapper.readValue(metadataFile, WorkspaceProject.class);
                    // Check if it's missing from the main store/registry
                    if (store.findProject(project.id()).isEmpty()) {
                        System.out.println("[WorkspaceRecovery] Restoring project to registry: " + project.name() + " (" + project.id() + ")");
                        store.saveProject(project);
                    }
                } catch (IOException e) {
                    System.err.println("[WorkspaceRecovery] Metadata corrupt for project folder: " 
                        + dir.getName() + ". Corruption detected, removing invalid metadata. Exception: " + e.getMessage());
                    metadataFile.delete();
                }
            }
        }
    }
}
