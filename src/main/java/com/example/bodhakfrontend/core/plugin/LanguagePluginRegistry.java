package com.example.bodhakfrontend.core.plugin;

import java.nio.file.Path;
import java.util.*;

/**
 * Central registry for all registered language plugins.
 *
 * This is the ONLY place where language-to-plugin mapping is maintained.
 * It completely replaces the old factory trio:
 *   ClassInfoBuilderFactory, ClassNameExtractorFactory, ClassDependsOnFactory.
 *
 * Usage:
 *   LanguagePluginRegistry registry = new LanguagePluginRegistry();
 *   registry.register(new JavaLanguagePlugin(...));
 *   registry.register(new PythonLanguagePlugin());
 *   // To add Kotlin: registry.register(new KotlinLanguagePlugin());
 */
public class LanguagePluginRegistry {
    private final Map<String, LanguagePlugin> byExtension = new LinkedHashMap<>();
    private final Map<String, LanguagePlugin> byId        = new LinkedHashMap<>();
    public void register(LanguagePlugin plugin) {
        byId.put(plugin.getId().toLowerCase(), plugin);
        for (String ext : plugin.getSupportedExtensions()) {
            byExtension.put(ext.toLowerCase(), plugin);
        }
    }

    public Optional<LanguagePlugin> forFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) return Optional.empty();
        return Optional.ofNullable(byExtension.get(name.substring(dot)));
    }

    public Optional<LanguagePlugin> forId(String id) {
        return Optional.ofNullable(byId.get(id.toLowerCase()));
    }

    public boolean supports(Path path) {
        return forFile(path).isPresent();
    }

    public Collection<LanguagePlugin> getAllPlugins() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public Set<String> registeredIds() {
        return Collections.unmodifiableSet(byId.keySet());
    }
}
