package com.example.bodhak.context;



import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Builds and maintains the project-wide dependency graph.
 *
 * <h3>Thread-safety model</h3>
 * <ul>
 *   <li>{@code fileDependencies} is a {@link ConcurrentHashMap} — safe for
 *       concurrent per-file puts from multiple worker threads.</li>
 *   <li>{@link #recomputeGlobals()} is {@code synchronized} — only one
 *       recomputation runs at a time, preventing torn reads of the derived maps.</li>
 *   <li>The result of every recomputation is atomically published as an
 *       immutable {@link GraphSnapshot} via {@link #currentSnapshot}.
 *       Any thread can call {@link #snapshot()} lock-free at any time and
 *       receive a consistent, fully-computed view.</li>
 * </ul>
 *
 * <p>Callers must <em>never</em> mutate the maps returned by the getter methods —
 * they are intentionally kept for backward compat only and will be removed in a
 * future refactor. Prefer {@link #snapshot()} for all read-path consumers.
 */
public class DependencyGraph {

    private final CircularDependency circularDependency = new CircularDependency();

    // ── Mutable write-side (only mutated from background worker threads) ───────

    /** path → (entity → depends-on set).  ConcurrentHashMap for concurrent puts. */
    private final Map<Path, Map<String, Set<String>>> fileDependencies = new ConcurrentHashMap<>();

    // ── Derived caches (rebuilt under lock, then published as snapshot) ────────

    /** Published atomically after every recompute — read path is always lock-free. */
    private final AtomicReference<GraphSnapshot> currentSnapshot =
            new AtomicReference<>(GraphSnapshot.EMPTY);

    // ── Backward-compat getters (backed by last snapshot) ─────────────────────

    public DependencyGraph() {
    }

    @Deprecated
    public DependencyGraph(Object registry) {
        this();
    }

    // ── Write-side API ────────────────────────────────────────────────────────

    public void updateDependenciesForFile(Path file, Map<String, Set<String>> deps) {
        fileDependencies.put(file.toAbsolutePath().normalize(), deps);
        recomputeGlobals();
    }

    public void removeFile(Path file) {
        fileDependencies.remove(file.toAbsolutePath().normalize());
        recomputeGlobals();
    }

    // ── Snapshot API (preferred read path) ───────────────────────────────────

    /**
     * Returns the latest fully-computed, immutable graph snapshot.
     * Safe to call from any thread without synchronization.
     */
    public GraphSnapshot snapshot() {
        return currentSnapshot.get();
    }

    // ── Internal recomputation (synchronized to prevent torn state) ───────────

    private synchronized void recomputeGlobals() {
        Map<String, Set<String>> globalDeps = new HashMap<>();
        Map<String, Set<String>> reverseDeps = new HashMap<>();

        for (Map<String, Set<String>> map : fileDependencies.values()) {
            for (Map.Entry<String, Set<String>> e : map.entrySet()) {
                String fromEntity = e.getKey();
                Set<String> dependsOn = e.getValue();

                globalDeps.computeIfAbsent(fromEntity, k -> new HashSet<>()).addAll(dependsOn);

                for (String toEntity : dependsOn) {
                    reverseDeps.computeIfAbsent(toEntity, k -> new HashSet<>()).add(fromEntity);
                }
            }
        }

        Set<Set<String>> cycles = circularDependency.findCircularDependency(globalDeps);

        // Publish atomically — readers see either the old complete snapshot or the new one
        currentSnapshot.set(new GraphSnapshot(globalDeps, reverseDeps, cycles));
    }

    // ── Backward-compat getters (delegate to current snapshot) ───────────────

    /** @deprecated Prefer {@link #snapshot()} */
    @Deprecated
    public Map<String, Set<String>> getReverseDependencies() {
        return currentSnapshot.get().reverseDependencies();
    }

    /** @deprecated Prefer {@link #snapshot()} */
    @Deprecated
    public Map<Path, Map<String, Set<String>>> getFileDependencies() {
        return fileDependencies;
    }

    /** @deprecated Prefer {@link #snapshot()} */
    @Deprecated
    public Map<String, Set<String>> getGlobalDependencies() {
        return currentSnapshot.get().globalDependencies();
    }

    /** @deprecated Prefer {@link #snapshot()} */
    @Deprecated
    public Set<Set<String>> getCircularGroups() {
        return currentSnapshot.get().circularGroups();
    }


}
