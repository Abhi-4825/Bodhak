package com.example.anuviya.context;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An immutable, point-in-time snapshot of the project dependency graph.
 *
 * <p>This record is produced by {@link DependencyGraph#snapshot()} after every
 * recomputation. It can be passed freely across threads without synchronization
 * because every field is an unmodifiable view or a primitive-equivalent value.
 *
 * <p>The {@link com.example.anuviya.orchestration.incremental.EntityViewModelBuilder}
 * and the UI layer consume this instead of reaching into the live, mutable graph.
 */
public record GraphSnapshot(
        /** entity → set of entity names it depends on */
        Map<String, Set<String>> globalDependencies,
        /** entity → set of entity names that depend on it */
        Map<String, Set<String>> reverseDependencies,
        /** Tarjan SCC groups with size > 1 or self-loops */
        Set<Set<String>> circularGroups
) {
    /** Canonical empty snapshot — used before the first analysis completes. */
    public static final GraphSnapshot EMPTY = new GraphSnapshot(
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptySet()
    );

    /** Defensive constructor — wraps every map in an unmodifiable view. */
    public GraphSnapshot {
        globalDependencies  = Collections.unmodifiableMap(globalDependencies);
        reverseDependencies = Collections.unmodifiableMap(reverseDependencies);
        circularGroups      = Collections.unmodifiableSet(circularGroups);
    }
}
