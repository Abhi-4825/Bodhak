package com.example.bodhakfrontend.languages.python.analyzer;

import com.example.bodhakfrontend.core.model.entity.EntityContribution;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.plugin.EntryPointDetector;

import java.util.*;

/**
 * Detects Python-specific entry points via EntityContribution tags.
 *
 * Adding a new Python framework: emit a tag in PythonEntityInfoBuilder,
 * add a check here with string flavors — no core model changes required.
 */
public class PythonEntryPointDetector implements EntryPointDetector {

    @Override
    public EntryPointInfo detect(List<EntityInfo> entities) {
        Set<String> flavors = new LinkedHashSet<>();
        List<EntryPointInfo.Entry> mainEntries = new ArrayList<>();
        List<EntryPointInfo.Entry> testEntries = new ArrayList<>();
        Set<EntryPointInfo.Entry> frameworkRoots = new HashSet<>();

        for (EntityInfo entity : entities) {
            if (!"python".equals(entity.getLanguage().toLowerCase())) continue;
            EntityContribution contrib = entity.getContribution();
            String name = entity.getEntityName();

            // ── __main__ guard / entry script ────────────────────────────────
            if (contrib.hasTag("has_main")) {
                mainEntries.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_MAIN, "Python Script"));
                flavors.add("Python Script");
            }

            // ── Flask ────────────────────────────────────────────────────────
            if (contrib.hasTag("flask_route") || contrib.hasTag("flask_app")) {
                flavors.add("Flask");
                frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "Flask Route"));
            }

            // ── Django ───────────────────────────────────────────────────────
            if (contrib.hasTag("django_view") || contrib.hasTag("django_model")) {
                flavors.add("Django");
                frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "Django Component"));
            }

            // ── FastAPI ──────────────────────────────────────────────────────
            if (contrib.hasTag("fastapi_route") || contrib.hasTag("fastapi_app")) {
                flavors.add("FastAPI");
                frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "FastAPI Route"));
            }

            // ── Pytest ───────────────────────────────────────────────────────
            if (contrib.hasTag("pytest_test")) {
                flavors.add("Pytest");
                testEntries.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_TEST, "Pytest Test"));
            }

            // ── Celery tasks ─────────────────────────────────────────────────
            if (contrib.hasTag("celery_task")) {
                flavors.add("Celery");
                frameworkRoots.add(new EntryPointInfo.Entry(name, EntryPointInfo.KIND_FRAMEWORK_ROOT, "Celery Task"));
            }
        }

        EntryPointInfo.Entry primary = mainEntries.isEmpty() ? null : mainEntries.get(0);
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
}
