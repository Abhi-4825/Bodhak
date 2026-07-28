package com.example.anuviya.workspace;

import com.example.anuviya.workspace.event.WorkspaceEvent;
import com.example.anuviya.workspace.event.WorkspaceEventBus;
import com.example.anuviya.workspace.model.*;
import com.example.anuviya.workspace.store.WorkspaceStore;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class ProjectManager {
    private final WorkspaceStore store;

    public ProjectManager(WorkspaceStore store) {
        this.store = store;
    }

    public WorkspaceProject registerProject(File folder) {
        String absolutePath = folder.getAbsolutePath();
        Optional<WorkspaceProject> existingOpt = store.findProjectByPath(absolutePath);

        WorkspaceProject project;
        if (existingOpt.isPresent()) {
            WorkspaceProject existing = existingOpt.get();
            project = new WorkspaceProject(
                existing.id(),
                existing.name(),
                existing.location(),
                existing.language(),
                existing.buildTool(),
                existing.projectType(),
                existing.compilerVersion(),
                existing.analysisVersion(),
                existing.created(),
                Instant.now(), // lastOpened
                existing.lastAnalysis(),
                existing.latestSessionId(),
                existing.fingerprint(),
                existing.pinned(),
                existing.favorite(),
                ProjectStatus.OPEN
            );
        } else {
            // New project registration
            UUID id = UUID.randomUUID();
            String name = folder.getName();
            
            // Basic project detection supporting Java, Kotlin, and Python
            String buildTool = "Maven";
            String language = "Java";
            String projectType = "Console Application";

            boolean hasPythonFiles = hasFilesWithExtension(folder, ".py");
            boolean hasRequirements = new File(folder, "requirements.txt").exists();
            boolean hasPyProject = new File(folder, "pyproject.toml").exists();
            boolean hasPipfile = new File(folder, "Pipfile").exists();
            boolean hasSetupPy = new File(folder, "setup.py").exists();

            if (hasPythonFiles || hasRequirements || hasPyProject || hasPipfile || hasSetupPy) {
                language = "Python";
                projectType = "Python Application";
                if (hasPyProject) {
                    buildTool = "Poetry";
                } else if (hasPipfile) {
                    buildTool = "Pipenv";
                } else if (hasSetupPy) {
                    buildTool = "Setuptools";
                } else {
                    buildTool = "pip";
                }
            } else if (new File(folder, "src/main/kotlin").exists() || hasFilesWithExtension(folder, ".kt")) {
                language = "Kotlin";
                if (new File(folder, "build.gradle").exists() || new File(folder, "build.gradle.kts").exists()) {
                    buildTool = "Gradle";
                }
                projectType = "Kotlin Console Application";
            } else {
                language = "Java";
                if (new File(folder, "build.gradle").exists() || new File(folder, "build.gradle.kts").exists()) {
                    buildTool = "Gradle";
                    projectType = "Gradle Java Application";
                } else if (new File(folder, "pom.xml").exists()) {
                    buildTool = "Maven";
                    projectType = "Spring Boot REST API";
                } else {
                    buildTool = "Maven";
                    projectType = "Java Application";
                }
            }

            ProjectFingerprint fingerprint = new ProjectFingerprint(
                id.toString(),
                buildTool,
                language,
                countSourceFiles(folder),
                generateProjectHash(folder)
            );

            project = new WorkspaceProject(
                id,
                name,
                absolutePath,
                language,
                buildTool,
                projectType,
                "24", // compilerVersion
                "1.0", // analysisVersion
                Instant.now(), // created
                Instant.now(), // lastOpened
                null, // lastAnalysis
                null, // latestSessionId
                fingerprint,
                false, // pinned
                false, // favorite
                ProjectStatus.OPEN
            );
        }

        store.saveProject(project);
        WorkspaceEventBus.getInstance().publish(new WorkspaceEvent.ProjectRegistered(project));
        return project;
    }

    public void touchLastOpened(UUID id) {
        Optional<WorkspaceProject> pOpt = store.findProject(id);
        if (pOpt.isPresent()) {
            WorkspaceProject p = pOpt.get();
            WorkspaceProject updated = new WorkspaceProject(
                p.id(), p.name(), p.location(), p.language(), p.buildTool(), p.projectType(),
                p.compilerVersion(), p.analysisVersion(), p.created(), Instant.now(), p.lastAnalysis(),
                p.latestSessionId(), p.fingerprint(), p.pinned(), p.favorite(), ProjectStatus.OPEN
            );
            store.saveProject(updated);
            WorkspaceEventBus.getInstance().publish(new WorkspaceEvent.ProjectOpened(updated));
        }
    }

    public List<WorkspaceProject> getRecentProjects(int limit) {
        return store.listProjects().stream()
            .filter(p -> p.status() != ProjectStatus.ARCHIVED)
            .sorted(Comparator.comparing(WorkspaceProject::lastOpened, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public List<WorkspaceProject> getPinnedProjects() {
        return store.listProjects().stream()
            .filter(p -> p.pinned() || p.status() == ProjectStatus.PINNED)
            .collect(Collectors.toList());
    }

    public List<WorkspaceProject> getArchivedProjects() {
        return store.listProjects().stream()
            .filter(p -> p.status() == ProjectStatus.ARCHIVED)
            .collect(Collectors.toList());
    }

    public List<WorkspaceProject> getOpenProjects() {
        return store.listProjects().stream()
            .filter(p -> p.status() == ProjectStatus.OPEN)
            .collect(Collectors.toList());
    }

    public void setPinned(UUID id, boolean pinned) {
        Optional<WorkspaceProject> pOpt = store.findProject(id);
        if (pOpt.isPresent()) {
            WorkspaceProject p = pOpt.get();
            WorkspaceProject updated = new WorkspaceProject(
                p.id(), p.name(), p.location(), p.language(), p.buildTool(), p.projectType(),
                p.compilerVersion(), p.analysisVersion(), p.created(), p.lastOpened(), p.lastAnalysis(),
                p.latestSessionId(), p.fingerprint(), pinned, p.favorite(), p.status()
            );
            store.saveProject(updated);
            WorkspaceEventBus.getInstance().publish(new WorkspaceEvent.WorkspaceUpdated());
        }
    }

    public void setStatus(UUID id, ProjectStatus status) {
        Optional<WorkspaceProject> pOpt = store.findProject(id);
        if (pOpt.isPresent()) {
            WorkspaceProject p = pOpt.get();
            WorkspaceProject updated = new WorkspaceProject(
                p.id(), p.name(), p.location(), p.language(), p.buildTool(), p.projectType(),
                p.compilerVersion(), p.analysisVersion(), p.created(), p.lastOpened(), p.lastAnalysis(),
                p.latestSessionId(), p.fingerprint(), p.pinned(), p.favorite(), status
            );
            store.saveProject(updated);
            WorkspaceEventBus.getInstance().publish(new WorkspaceEvent.WorkspaceUpdated());
        }
    }

    public void deleteProject(UUID id) {
        store.deleteProject(id);
        // Delete project metadata.json from local disk so it is not recovered
        File projectDir = new File(store.listProjects().isEmpty() ? new File(System.getProperty("user.home"), ".anuviya/workspace") : new File(System.getProperty("user.home"), ".anuviya/workspace"), "projects/" + id.toString());
        File metadataFile = new File(projectDir, "metadata.json");
        if (metadataFile.exists()) {
            metadataFile.delete();
        }
        WorkspaceEventBus.getInstance().publish(new WorkspaceEvent.WorkspaceUpdated());
    }

    private boolean hasFilesWithExtension(File dir, String ext) {
        if (!dir.exists() || !dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files == null) return false;
        for (File f : files) {
            if (f.isDirectory()) {
                String name = f.getName().toLowerCase();
                if (name.equals("target") || name.equals("build") || name.startsWith(".") || name.equals("venv") || name.equals("node_modules")) {
                    continue;
                }
                if (hasFilesWithExtension(f, ext)) {
                    return true;
                }
            } else if (f.getName().toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private long countSourceFiles(File folder) {
        long count = 0;
        File[] files = folder.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            if (f.isDirectory()) {
                String name = f.getName().toLowerCase();
                if (name.equals("target") || name.equals("build") || name.startsWith(".") || name.equals("venv") || name.equals("node_modules")) {
                    continue;
                }
                count += countSourceFiles(f);
            } else {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".java") || name.endsWith(".kt") || name.endsWith(".py")) {
                    count++;
                }
            }
        }
        return count;
    }

    private String generateProjectHash(File folder) {
        File buildFile = new File(folder, "pom.xml");
        if (!buildFile.exists()) {
            buildFile = new File(folder, "build.gradle");
        }
        if (!buildFile.exists()) {
            buildFile = new File(folder, "pyproject.toml");
        }
        if (!buildFile.exists()) {
            buildFile = new File(folder, "requirements.txt");
        }
        if (buildFile.exists()) {
            return "sha256:" + String.format("%08x", buildFile.length() + buildFile.lastModified());
        }
        return "sha256:" + UUID.randomUUID().toString().substring(0, 8);
    }
}
