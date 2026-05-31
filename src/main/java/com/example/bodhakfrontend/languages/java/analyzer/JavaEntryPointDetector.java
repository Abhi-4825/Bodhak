package com.example.bodhakfrontend.languages.java.analyzer;

import com.example.bodhakfrontend.core.model.entity.EntityContribution;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.plugin.EntryPointDetector;

import java.util.*;

/**
 * Detects Java-specific entry points via EntityContribution tags.
 *
 * Adding a new Java framework: just emit a tag in the EntityInfoBuilder and
 * add a check here — no core model changes required.
 */
public class JavaEntryPointDetector implements EntryPointDetector {

    @Override
    public EntryPointInfo detect(List<EntityInfo> entities) {
        Set<String> flavors = new LinkedHashSet<>();
        List<EntryPointInfo.Entry> mainEntries = new ArrayList<>();
        List<EntryPointInfo.Entry> testEntries = new ArrayList<>();
        Set<EntryPointInfo.Entry> frameworkRoots = new HashSet<>();

        for (EntityInfo entity : entities) {
            if (!"java".equals(entity.getLanguage().toLowerCase())) continue;
            EntityContribution contrib = entity.getContribution();
            String name = entity.getEntityName();

            // ── Generic main ─────────────────────────────────────────────────
            if (contrib.hasMain()) {
                mainEntries.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_MAIN, "Main Entry"));
                flavors.add(EntryPointInfo.FLAVOR_CLI);
            }

            // ── Spring Boot ──────────────────────────────────────────────────
            if (contrib.hasTag("spring_boot")) {
                flavors.add("Spring Boot");
                if (contrib.hasMain()) {
                    mainEntries.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "Spring Boot App"));
                } else {
                    frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "Spring Boot Root"));
                }
            }

            // ── Spring components (controllers, services, repos) ─────────────
            if (contrib.hasTag("rest_controller") || contrib.hasTag("spring_component")) {
                flavors.add("Spring Boot");
                frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "Spring Component"));
            }

            // ── JavaFX ───────────────────────────────────────────────────────
            if (contrib.hasTag("javafx")) {
                flavors.add("JavaFX");
                if (contrib.hasMain()) {
                    mainEntries.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "JavaFX App"));
                } else {
                    frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "JavaFX Root"));
                }
            }

            // ── JPA / Hibernate ──────────────────────────────────────────────
            if (contrib.hasTag("jpa_entity")) {
                flavors.add("JPA / Hibernate");
                frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "JPA Entity"));
            }

            // ── Tests ───────────────────────────────────────────────────────
            if (contrib.isTest()) {
                flavors.add("Test Suite");
                testEntries.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_TEST, "JUnit Test"));
            }
        }

        EntryPointInfo.Entry primary = choosePrimary(mainEntries);

        Set<EntryPointInfo.Entry> secondary = new HashSet<>();
        for (EntryPointInfo.Entry e : mainEntries) {
            if (primary == null || !e.entityName().equals(primary.entityName())) {
                secondary.add(e);
            }
        }
        secondary.addAll(testEntries);

        EntryPointInfo epi = new EntryPointInfo(flavors, primary, secondary);
        epi.getFrameworkRoots().addAll(frameworkRoots);
        return epi;
    }

    private EntryPointInfo.Entry choosePrimary(List<EntryPointInfo.Entry> mains) {
        // Prefer framework-root mains (Spring Boot, JavaFX) over plain CLI
        return mains.stream()
                .filter(e -> e.kind().equals(EntryPointInfo.KIND_FRAMEWORK_ROOT))
                .findFirst()
                .orElse(mains.isEmpty() ? null : mains.get(0));
    }
}