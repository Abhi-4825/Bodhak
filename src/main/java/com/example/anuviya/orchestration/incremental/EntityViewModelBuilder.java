package com.example.anuviya.orchestration.incremental;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.EntityViewModel;
import com.example.anuviya.context.DependencyGraph;
import com.example.anuviya.context.GraphSnapshot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains the per-project map of entity name → {@link EntityViewModel}.
 *
 * <h3>Thread-safety</h3>
 * <ul>
 *   <li>{@code vmMap} is a {@link ConcurrentHashMap} — concurrent puts/removes
 *       from background worker threads are safe.</li>
 *   <li>{@link EntityViewModel#update(EntityInfo)} writes JavaFX observable
 *       properties — callers must ensure this is invoked on the FX thread, or
 *       that the ViewModel is not yet attached to the scene graph.</li>
 *   <li>{@link #refreshDependencies()} reads an immutable {@link GraphSnapshot}
 *       published by the {@link DependencyGraph} — no lock required.</li>
 * </ul>
 */
public class EntityViewModelBuilder {

    private final DependencyGraph dependencyGraph;

    /**
     * ConcurrentHashMap — safe for concurrent reads/writes across the watcher
     * thread (puts/removes) and the FX thread (reads for rendering).
     */
    private final ConcurrentHashMap<String, EntityViewModel> vmMap = new ConcurrentHashMap<>();

    public EntityViewModelBuilder(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    // ── Bulk build (called once after full initial analysis) ─────────────────

    public Map<String, EntityViewModel> initialBuild(List<EntityInfo> entities) {
        vmMap.clear();
        for (EntityInfo info : entities) {
            vmMap.computeIfAbsent(info.getEntityName(), k -> new EntityViewModel(info));
        }
        refreshDependencies();
        return vmMap;
    }

    // ── Incremental updates ───────────────────────────────────────────────────

    public void onFileCreate(List<EntityInfo> entities) {
        for (EntityInfo info : entities) {
            vmMap.computeIfAbsent(info.getEntityName(), k -> new EntityViewModel(info));
        }
        refreshDependencies();
    }

    public void onFileModify(List<EntityInfo> oldEntities, List<EntityInfo> newEntities) {
        // Build a lookup for the new set
        Map<String, EntityInfo> newMap = new java.util.HashMap<>();
        for (EntityInfo e : newEntities) newMap.put(e.getEntityName(), e);

        // Remove entities that no longer exist
        for (EntityInfo old : oldEntities) {
            if (!newMap.containsKey(old.getEntityName())) {
                vmMap.remove(old.getEntityName());
            }
        }

        // Update or create ViewModels for every new entity
        for (EntityInfo info : newEntities) {
            vmMap.compute(info.getEntityName(), (k, existing) -> {
                if (existing == null) return new EntityViewModel(info);
                existing.update(info);
                return existing;
            });
        }

        refreshDependencies();
    }

    public void onFileDelete(List<EntityInfo> entities) {
        for (EntityInfo info : entities) {
            vmMap.remove(info.getEntityName());
        }
        refreshDependencies();
    }

    // ── Read API ──────────────────────────────────────────────────────────────

    /** Returns the live (concurrent) view-model map. Read-only for UI consumers. */
    public Map<String, EntityViewModel> getViewModelMap() {
        return vmMap;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Reads the latest immutable {@link GraphSnapshot} from the graph — no lock
     * required because the snapshot itself is immutable.
     *
     * <p>Currently a placeholder for future dependency-overlay logic on ViewModels.
     * The snapshot data is already embedded in each {@link EntityInfo} via
     * {@link com.example.anuviya.orchestration.AnalysisEngine#}.
     */
    @SuppressWarnings("unused")
    private void refreshDependencies() {
        GraphSnapshot snap = dependencyGraph.snapshot();
        // Reserved for future ViewModel-level dependency overlays.
        // The snapshot is consumed here; adding reactive overlays later
        // does not require changing any caller.
    }
}
