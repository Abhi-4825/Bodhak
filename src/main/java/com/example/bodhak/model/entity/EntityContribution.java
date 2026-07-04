package com.example.bodhak.model.entity;

import java.util.Collections;
import java.util.Set;

/**
 * Language-neutral tag-based contribution descriptor.
 *
 * Each language plugin populates a set of string tags and stores
 * arbitrary metadata as key-value pairs.
 *
 * Examples:
 *   Java tags:   "has_main", "spring_boot", "javafx", "jpa_entity",
 *                "rest_controller", "test_class", "framework_root"
 *   Python tags: "has_main", "is_async", "is_dataclass", "flask_route",
 *                "django_view", "pytest_test", "framework_root"
 *
 * Rules that used to be hard-coded booleans (hasSpring, hasJavaFx …)
 * are now just tag checks: contribution.hasTag("spring_boot").
 */
public class EntityContribution {

    public static final EntityContribution EMPTY =
            new EntityContribution(Set.of());

    private final Set<String> tags;

    public EntityContribution(Set<String> tags) {
        this.tags = Set.copyOf(tags);
    }

    /** Returns true if the given tag is present. */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public Set<String> getTags() {
        return tags;
    }

    // ── Convenience helpers used by analysers ────────────────────────────────

    /** True for any language that has a "main" entry point. */
    public boolean hasMain()         { return hasTag("has_main");        }
    /** True when the entity is recognised as a test class/function. */
    public boolean isTest()          { return hasTag("test_class") || hasTag("pytest_test"); }
    /**
     * True when a framework manages instantiation (Spring components,
     * Django views, Flask routes…) — these entities are always "reachable".
     */
    public boolean isFrameworkRoot() { return hasTag("framework_root");  }

    @Override
    public String toString() { return "EntityContribution" + tags; }
}
