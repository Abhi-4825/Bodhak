package com.example.bodhakfrontend.engine.analyzer;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.plugin.EntryPointDetector;
import com.example.bodhakfrontend.core.plugin.LanguagePlugin;
import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Aggregates EntryPoints across all supported language plugins.
 * Implements EntryPointDetector to maintain interface consistency.
 *
 * Primary selection priority:
 *   FRAMEWORK_ROOT > MAIN > SCRIPT > TEST
 */
public class GlobalEntryPointDetector implements EntryPointDetector {

    private final LanguagePluginRegistry registry;

    public GlobalEntryPointDetector(LanguagePluginRegistry registry) {
        this.registry = registry;
    }

    @Override
    public EntryPointInfo detect(List<EntityInfo> entities) {
        Set<String> allFlavors = new LinkedHashSet<>();
        EntryPointInfo.Entry primary = null;
        Set<EntryPointInfo.Entry> allSecondary = new HashSet<>();
        Set<EntryPointInfo.Entry> allFrameworkRoots = new HashSet<>();

        for (LanguagePlugin plugin : registry.getAllPlugins()) {
            EntryPointDetector detector = plugin.getEntryPointDetector();
            if (detector == null) continue;

            EntryPointInfo info = detector.detect(entities);
            if (info == null) continue;

            allFlavors.addAll(info.getProjectFlavors());
            allSecondary.addAll(info.getSecondaryEntries());
            allFrameworkRoots.addAll(info.getFrameworkRoots());

            // Upgrade primary logic: FRAMEWORK_ROOT wins over plain MAIN
            EntryPointInfo.Entry candidate = info.getPrimaryEntry();
            if (candidate == null) continue;

            if (primary == null) {
                primary = candidate;
            } else if (primary.kind().equals(EntryPointInfo.KIND_MAIN)
                    && candidate.kind().equals(EntryPointInfo.KIND_FRAMEWORK_ROOT)) {
                // Upgrade generic MAIN → specific framework root
                primary = candidate;
            }
        }

        if (allFlavors.isEmpty()) {
            allFlavors.add(EntryPointInfo.FLAVOR_UNKNOWN);
        }

        EntryPointInfo composite = new EntryPointInfo(allFlavors, primary, allSecondary);
        composite.getFrameworkRoots().addAll(allFrameworkRoots);
        return composite;
    }
}
