package com.example.anuviya.workspace.store;

import com.example.anuviya.workspace.model.WorkspacePreferences;
import com.example.anuviya.workspace.model.WorkspaceProject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class JsonWorkspaceStore implements WorkspaceStore {
    private final File workspaceDir;
    private final File registryFile;
    private final File prefsFile;
    private final ObjectMapper mapper;

    public JsonWorkspaceStore(File workspaceDir) {
        this.workspaceDir = workspaceDir;
        if (!this.workspaceDir.exists()) {
            this.workspaceDir.mkdirs();
        }
        this.registryFile = new File(workspaceDir, "workspace.json");
        this.prefsFile = new File(workspaceDir, "preferences.json");
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Write Instants as ISO-8601 strings, not numeric timestamps
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Don't fail if JSON has extra fields (forward-compatible schema changes)
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // JavaTimeModule: required for Instant, LocalDate, etc.
        this.mapper.registerModule(new JavaTimeModule());
        // ParameterNamesModule: lets Jackson bind record constructor parameters.
        // Requires -parameters javac flag (set in pom.xml).
        this.mapper.registerModule(new ParameterNamesModule());
    }

    private synchronized Map<UUID, WorkspaceProject> loadRegistry() {
        if (!registryFile.exists()) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(registryFile, new TypeReference<Map<UUID, WorkspaceProject>>() {});
        } catch (IOException e) {
            System.err.println("[WorkspaceStore] Registry corrupt — wiping and starting fresh. Cause: " + e.getMessage());
            // Self-heal: delete the corrupt file so next write succeeds cleanly
            if (registryFile.exists()) registryFile.delete();
            return new HashMap<>();
        }
    }

    private synchronized void saveRegistry(Map<UUID, WorkspaceProject> registry) {
        try {
            mapper.writeValue(registryFile, registry);
        } catch (IOException e) {
            System.err.println("[WorkspaceStore] Failed to save workspace registry: " + e.getMessage());
        }
    }

    @Override
    public synchronized void saveProject(WorkspaceProject p) {
        Map<UUID, WorkspaceProject> registry = loadRegistry();
        registry.put(p.id(), p);
        saveRegistry(registry);

        // Also save a copy inside the project folder
        File projectDir = new File(workspaceDir, "projects/" + p.id().toString());
        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }
        File metaFile = new File(projectDir, "metadata.json");
        try {
            mapper.writeValue(metaFile, p);
        } catch (IOException e) {
            System.err.println("[WorkspaceStore] Failed to save project metadata copy: " + e.getMessage());
        }
    }

    @Override
    public synchronized Optional<WorkspaceProject> findProject(UUID id) {
        return Optional.ofNullable(loadRegistry().get(id));
    }

    @Override
    public synchronized Optional<WorkspaceProject> findProjectByPath(String path) {
        if (path == null) return Optional.empty();
        String normalizedPath = new File(path).getAbsolutePath();
        return loadRegistry().values().stream()
            .filter(p -> new File(p.location()).getAbsolutePath().equals(normalizedPath))
            .findFirst();
    }

    @Override
    public synchronized List<WorkspaceProject> listProjects() {
        return new ArrayList<>(loadRegistry().values());
    }

    @Override
    public synchronized void deleteProject(UUID id) {
        Map<UUID, WorkspaceProject> registry = loadRegistry();
        registry.remove(id);
        saveRegistry(registry);
    }

    @Override
    public synchronized void savePreferences(WorkspacePreferences prefs) {
        try {
            mapper.writeValue(prefsFile, prefs);
        } catch (IOException e) {
            System.err.println("[WorkspaceStore] Failed to save preferences: " + e.getMessage());
        }
    }

    @Override
    public synchronized WorkspacePreferences loadPreferences() {
        if (!prefsFile.exists()) {
            return new WorkspacePreferences("DARK", null, null, 18.0, 1024.0, 768.0, false);
        }
        try {
            return mapper.readValue(prefsFile, WorkspacePreferences.class);
        } catch (IOException e) {
            System.err.println("[WorkspaceStore] Preferences corrupt, using defaults. Cause: " + e.getMessage());
            if (prefsFile.exists()) prefsFile.delete();
            return new WorkspacePreferences("DARK", null, null, 18.0, 1024.0, 768.0, false);
        }
    }
}
