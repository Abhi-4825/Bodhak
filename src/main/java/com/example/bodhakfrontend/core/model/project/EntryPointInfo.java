package com.example.bodhakfrontend.core.model.project;

import java.util.HashSet;
import java.util.Set;

/**
 * Information about project entry points, completely language-neutral.
 *
 * Framework types and entry kinds are now plain Strings, allowing any
 * plugin to define arbitrary frameworks (e.g. "Spring Boot", "Django",
 * "Express.js", "FastAPI", "Qt") without touching core model code.
 *
 * Standard tag conventions:
 *   Kinds:   "MAIN", "TEST", "FRAMEWORK_ROOT", "SCRIPT"
 *   Flavors: "CLI", "Spring Boot", "JavaFX", "Django", "Flask",
 *             "FastAPI", "Pytest", "Python Script", "Library", ...
 */
public class EntryPointInfo {

    // ── Entry record ──────────────────────────────────────────────────────────
    /**
     * @param entityName  fully-qualified or simple name of the root entity
     * @param kind        arbitrary kind tag, e.g. "MAIN", "FRAMEWORK_ROOT", "TEST"
     * @param label       human-readable label for the UI, e.g. "Spring Boot App"
     */
    public record Entry(String entityName, String kind, String label) {}

    // ── Fields ────────────────────────────────────────────────────────────────
    private final Set<String> projectFlavors;       // e.g. "Spring Boot", "Django"
    private final Entry primaryEntry;
    private final Set<Entry> secondaryEntries;
    private final Set<Entry> frameworkRoots = new HashSet<>();

    public EntryPointInfo(Set<String> projectFlavors,
                          Entry primaryEntry,
                          Set<Entry> secondaryEntries) {
        this.projectFlavors   = Set.copyOf(projectFlavors);
        this.primaryEntry     = primaryEntry;
        this.secondaryEntries = Set.copyOf(secondaryEntries);
    }

    public Set<Entry> getAllRoots() {
        Set<Entry> all = new HashSet<>();
        if (primaryEntry != null) all.add(primaryEntry);
        all.addAll(secondaryEntries);
        all.addAll(frameworkRoots);
        return all;
    }

    public Entry getPrimaryEntry()              { return primaryEntry;     }
    public Set<Entry> getSecondaryEntries()     { return secondaryEntries; }
    public Set<Entry> getFrameworkRoots()       { return frameworkRoots;   }
    public Set<String> getProjectFlavors()      { return projectFlavors;   }

    /**
     * Returns a comma-separated string of flavors for display, e.g. "CLI, Spring Boot".
     * Falls back to "Unknown" if no flavors are detected.
     */
    public String getFlavorSummary() {
        if (projectFlavors.isEmpty()) return "Unknown";
        return String.join(", ", projectFlavors);
    }

    /** Returns an empty/unknown sentinel. */
    public static EntryPointInfo unknown() {
        return new EntryPointInfo(Set.of("Unknown"), null, Set.of());
    }

    // Standard kind constants for plugins to use — not exhaustive, just conventions.
    public static final String KIND_MAIN           = "MAIN";
    public static final String KIND_TEST           = "TEST";
    public static final String KIND_FRAMEWORK_ROOT = "FRAMEWORK_ROOT";
    public static final String KIND_SCRIPT         = "SCRIPT";

    // Standard flavor constants — plugins may also use their own strings freely.
    public static final String FLAVOR_CLI          = "CLI";
    public static final String FLAVOR_LIBRARY      = "Library";
    public static final String FLAVOR_UNKNOWN      = "Unknown";
}
