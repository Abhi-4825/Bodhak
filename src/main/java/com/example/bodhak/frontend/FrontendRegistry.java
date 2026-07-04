package com.example.bodhak.frontend;

import java.nio.file.Path;
import java.util.*;

/**
 * Registry mapping file extensions to their respective LanguageFrontend compiler frontends.
 */
public class FrontendRegistry {

    private final Map<String, LanguageFrontend> extensionMap = new HashMap<>();
    private final Map<String, LanguageFrontend> idMap = new HashMap<>();

    public void register(LanguageFrontend frontend) {
        idMap.put(frontend.getId().toLowerCase(), frontend);
        for (String ext : frontend.getSupportedExtensions()) {
            extensionMap.put(ext.toLowerCase(), frontend);
        }
    }

    public Optional<LanguageFrontend> forFile(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot == -1) {
            return Optional.empty();
        }
        String ext = name.substring(dot).toLowerCase();
        return Optional.ofNullable(extensionMap.get(ext));
    }

    public Optional<LanguageFrontend> forId(String languageId) {
        return Optional.ofNullable(idMap.get(languageId.toLowerCase()));
    }

    public Collection<LanguageFrontend> getAll() {
        return idMap.values();
    }
}
