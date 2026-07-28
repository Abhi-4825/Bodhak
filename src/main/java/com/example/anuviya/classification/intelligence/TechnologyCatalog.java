package com.example.anuviya.classification.intelligence;

import java.util.List;
import java.util.Optional;

public record TechnologyCatalog(
    List<DetectedTechnology> detectedTechnologies
) {
    public boolean hasTechnology(String id) {
        return detectedTechnologies.stream()
            .anyMatch(t -> t.id().equalsIgnoreCase(id));
    }

    public Optional<DetectedTechnology> getTechnology(String id) {
        return detectedTechnologies.stream()
            .filter(t -> t.id().equalsIgnoreCase(id))
            .findFirst();
    }

    public static TechnologyCatalog empty() {
        return new TechnologyCatalog(List.of());
    }
}
