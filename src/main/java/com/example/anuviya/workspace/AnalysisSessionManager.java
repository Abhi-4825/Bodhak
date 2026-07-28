package com.example.anuviya.workspace;

import com.example.anuviya.workspace.event.WorkspaceEvent;
import com.example.anuviya.workspace.event.WorkspaceEventBus;
import com.example.anuviya.workspace.model.AnalysisSession;
import com.example.anuviya.workspace.model.ProjectIntelligence;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public final class AnalysisSessionManager {
    private final File workspaceDir;
    private final ObjectMapper mapper;

    public AnalysisSessionManager(File workspaceDir) {
        this.workspaceDir = workspaceDir;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.findAndRegisterModules();
    }

    public void saveSession(AnalysisSession session) {
        File sessionsDir = new File(workspaceDir, "projects/" + session.projectId().toString() + "/sessions/" + session.sessionId().toString());
        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs();
        }
        File sessionFile = new File(sessionsDir, "session.json");
        try {
            mapper.writeValue(sessionFile, session);
            WorkspaceEventBus.getInstance().publish(new WorkspaceEvent.SessionSaved(session));
        } catch (IOException e) {
            System.err.println("[AnalysisSessionManager] Failed to save session: " + e.getMessage());
        }
    }

    public void saveIntelligence(ProjectIntelligence intel) {
        File projectDir = new File(workspaceDir, "projects/" + intel.projectId().toString());
        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }
        File intelFile = new File(projectDir, "intelligence.json");
        try {
            mapper.writeValue(intelFile, intel);
        } catch (IOException e) {
            System.err.println("[AnalysisSessionManager] Failed to save intelligence: " + e.getMessage());
        }
    }

    public Optional<ProjectIntelligence> getIntelligence(UUID projectId) {
        File intelFile = new File(workspaceDir, "projects/" + projectId.toString() + "/intelligence.json");
        if (!intelFile.exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(intelFile, ProjectIntelligence.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<AnalysisSession> getLatestSession(UUID projectId) {
        return getAllSessions(projectId).stream().findFirst();
    }

    public List<AnalysisSession> getAllSessions(UUID projectId) {
        File sessionsDir = new File(workspaceDir, "projects/" + projectId.toString() + "/sessions");
        if (!sessionsDir.exists()) {
            return new ArrayList<>();
        }
        File[] dirs = sessionsDir.listFiles(File::isDirectory);
        if (dirs == null) return new ArrayList<>();

        List<AnalysisSession> sessions = new ArrayList<>();
        for (File dir : dirs) {
            File sessionFile = new File(dir, "session.json");
            if (sessionFile.exists()) {
                try {
                    sessions.add(mapper.readValue(sessionFile, AnalysisSession.class));
                } catch (IOException e) {
                    // Ignore corrupted sessions
                }
            }
        }
        sessions.sort(Comparator.comparing(AnalysisSession::timestamp, Comparator.reverseOrder()));
        return sessions;
    }

    public Map<LocalDate, List<AnalysisSession>> getTimeline(UUID projectId) {
        Map<LocalDate, List<AnalysisSession>> timeline = getAllSessions(projectId).stream()
            .collect(Collectors.groupingBy(
                s -> s.timestamp().atZone(ZoneId.systemDefault()).toLocalDate(),
                () -> new TreeMap<LocalDate, List<AnalysisSession>>(Comparator.reverseOrder()),
                Collectors.toList()
            ));
        return timeline;
    }

    public void deleteOldSessions(UUID projectId, int keepLast) {
        List<AnalysisSession> sessions = getAllSessions(projectId);
        if (sessions.size() <= keepLast) return;
        for (int i = keepLast; i < sessions.size(); i++) {
            File sessionDir = new File(workspaceDir, "projects/" + projectId.toString() + "/sessions/" + sessions.get(i).sessionId().toString());
            deleteDirectory(sessionDir);
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
