package com.example.anuviya.model.project;

import java.util.List;
import java.util.Set;

/**
 * Immutable compiler context representation of all project surfaces.
 */
public record ProjectRootInfo(
    Set<String> detectedArchetypes,
    List<ProjectSurface> surfaces
) {
    public List<ProjectSurface> getByCapability(RootCapability capability) {
        return surfaces.stream()
            .filter(s -> s.capabilities().contains(capability))
            .toList();
    }
}
