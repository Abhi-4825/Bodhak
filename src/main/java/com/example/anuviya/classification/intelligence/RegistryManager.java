package com.example.anuviya.classification.intelligence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

public class RegistryManager {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
    private final List<TechnologyDef> technologies = new ArrayList<>();

    public RegistryManager() {
        loadRegistry();
    }

    private void loadRegistry() {
        try {
            // Standard built-in resources
            List<String> filesToLoad = List.of(
                "registry/technologies/spring-boot.yaml",
                "registry/technologies/lombok.yaml",
                "registry/technologies/logback.yaml",
                "registry/technologies/junit.yaml",
                "registry/technologies/java-desktop.yaml",
                "registry/technologies/java-cli.yaml",
                "registry/technologies/react.yaml",
                "registry/technologies/fastapi.yaml"
            );

            for (String file : filesToLoad) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(file)) {
                    if (is != null) {
                        TechnologyDef def = MAPPER.readValue(is, TechnologyDef.class);
                        validate(def);
                        technologies.add(def);
                    } else {
                        System.err.println("Warning: Registry file not found in classpath: " + file);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading registry file " + file + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load built-in technology registry: " + e.getMessage());
        }
    }

    private void validate(TechnologyDef def) {
        if (def.getId() == null || def.getId().isBlank()) {
            throw new IllegalArgumentException("Technology registry entry missing id");
        }
        if (def.getDisplayName() == null || def.getDisplayName().isBlank()) {
            throw new IllegalArgumentException("Technology registry entry missing displayName for id: " + def.getId());
        }
        if (def.getFamily() == null || def.getFamily().isBlank()) {
            throw new IllegalArgumentException("Technology registry entry missing family for id: " + def.getId());
        }
    }

    public void loadExternalRegistry(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) return;
        try (var stream = Files.newDirectoryStream(dir, "*.yaml")) {
            for (Path path : stream) {
                try (InputStream is = Files.newInputStream(path)) {
                    TechnologyDef def = MAPPER.readValue(is, TechnologyDef.class);
                    validate(def);
                    technologies.removeIf(t -> t.getId().equalsIgnoreCase(def.getId()));
                    technologies.add(def);
                } catch (Exception e) {
                    System.err.println("Error loading external registry file " + path + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load external registry: " + e.getMessage());
        }
    }

    public List<TechnologyDef> getTechnologies() {
        return Collections.unmodifiableList(technologies);
    }
}
