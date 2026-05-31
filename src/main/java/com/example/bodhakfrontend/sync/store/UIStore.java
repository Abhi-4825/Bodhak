package com.example.bodhakfrontend.sync.store;

import com.example.bodhakfrontend.core.model.incremental.EntityViewModel;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.GraphSnapshot;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.nio.file.Path;
import java.util.List;

/**
 * Per-project single source of truth for all JavaFX-observable UI state.
 *
 * <h3>Thread-safety contract</h3>
 * <strong>All mutations to this class must occur on the JavaFX Application Thread.</strong>
 * The {@link com.example.bodhakfrontend.sync.bus.UpdateDispatcher} guarantees
 * this by invoking handlers exclusively inside {@code Platform.runLater()}.
 *
 * <h3>Consumers</h3>
 * UI components bind directly to the properties and observable collections exposed
 * by this store. They never hold a reference to {@link ProjectInfo} directly —
 * they observe the {@link #projectInfoProperty()} instead.
 *
 * <h3>Lifecycle</h3>
 * One {@code UIStore} instance is created per loaded project. When a new project
 * is opened, the old store is discarded and a fresh one is wired in.
 */
public final class UIStore {

    // ── Project-level summary ─────────────────────────────────────────────────

    /** The most recently computed project analysis result. Never null after project load. */
    private final ObjectProperty<ProjectInfo> projectInfo =
            new SimpleObjectProperty<>(this, "projectInfo", null);

    // ── Entity list ───────────────────────────────────────────────────────────

    /** Live list of all entity view-models — suitable for direct UI binding. */
    private final ObservableList<EntityViewModel> entities =
            FXCollections.observableArrayList();

    /** Name → EntityViewModel lookup — suitable for overview/dependency panels. */
    private final ObservableMap<String, EntityViewModel> entityMap =
            FXCollections.observableHashMap();

    // ── Dependency graph ──────────────────────────────────────────────────────

    /** Latest immutable graph snapshot. Dependency graph panel observes this. */
    private final ObjectProperty<GraphSnapshot> graphSnapshot =
            new SimpleObjectProperty<>(this, "graphSnapshot", GraphSnapshot.EMPTY);

    // ── Log panel ─────────────────────────────────────────────────────────────

    /** Append-only log entry list. Max capped to prevent unbounded growth. */
    private final ObservableList<LogEntry> logs =
            FXCollections.observableArrayList();

    private static final int MAX_LOG_ENTRIES = 5_000;

    // ── Progress ─────────────────────────────────────────────────────────────

    private final DoubleProperty progressFraction =
            new SimpleDoubleProperty(this, "progressFraction", -1.0);

    private final StringProperty progressLabel =
            new SimpleStringProperty(this, "progressLabel", "");

    // ── File tree dirty paths ─────────────────────────────────────────────────

    /** Directories that need tree refresh — handlers add, tree controller clears. */
    private final ObservableList<Path> dirtyTreePaths =
            FXCollections.observableArrayList();

    // ── Mutation API (called only from handler classes on FX thread) ──────────

    public void setProjectInfo(ProjectInfo info) {
        projectInfo.set(info);
    }

    public void setGraphSnapshot(GraphSnapshot snapshot) {
        graphSnapshot.set(snapshot);
    }

    public void addEntities(List<EntityViewModel> vms) {
        for (EntityViewModel vm : vms) {
            entities.add(vm);
            entityMap.put(vm.getEntityName(), vm);
        }
    }

    public void removeEntities(List<String> entityNames) {
        entityNames.forEach(name -> {
            entityMap.remove(name);
            entities.removeIf(vm -> vm.getEntityName().equals(name));
        });
    }

    public void updateEntity(EntityViewModel vm) {
        entityMap.put(vm.getEntityName(), vm);
        // The list contains the same object reference, so observable properties
        // fire automatically when vm.update() is called.
    }

    public void appendLog(String level, String message) {
        if (logs.size() >= MAX_LOG_ENTRIES) {
            logs.remove(0); // drop oldest
        }
        logs.add(new LogEntry(level, message));
    }

    public void setProgress(double fraction, String label) {
        progressFraction.set(fraction);
        progressLabel.set(label);
    }

    public void markTreeDirty(Path directory) {
        if (!dirtyTreePaths.contains(directory)) {
            dirtyTreePaths.add(directory);
        }
    }

    public void clearDirtyPath(Path directory) {
        dirtyTreePaths.remove(directory);
    }

    public void reset() {
        projectInfo.set(null);
        entities.clear();
        entityMap.clear();
        graphSnapshot.set(GraphSnapshot.EMPTY);
        logs.clear();
        dirtyTreePaths.clear();
        progressFraction.set(-1.0);
        progressLabel.set("");
    }

    // ── Read-only observable accessors (for UI binding) ───────────────────────

    public ReadOnlyObjectProperty<ProjectInfo> projectInfoProperty() {
        return projectInfo;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo.get();
    }

    public ObservableList<EntityViewModel> getEntities() {
        return FXCollections.unmodifiableObservableList(entities);
    }

    public ObservableMap<String, EntityViewModel> getEntityMap() {
        return FXCollections.unmodifiableObservableMap(entityMap);
    }

    public ReadOnlyObjectProperty<GraphSnapshot> graphSnapshotProperty() {
        return graphSnapshot;
    }

    public ObservableList<LogEntry> getLogs() {
        return FXCollections.unmodifiableObservableList(logs);
    }

    public ReadOnlyDoubleProperty progressFractionProperty() {
        return progressFraction;
    }

    public ReadOnlyStringProperty progressLabelProperty() {
        return progressLabel;
    }

    public ObservableList<Path> getDirtyTreePaths() {
        return dirtyTreePaths;
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    /** A single line in the log panel. */
    public record LogEntry(String level, String message) {}
}
