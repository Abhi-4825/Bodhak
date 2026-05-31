package com.example.bodhakfrontend.engine.analyzer;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.Hotspot;

import java.util.*;

public class HotspotAnalyzer {

    // Thresholds
    private static final int LOC_THRESHOLD = 300;
    private static final int DEPENDS_ON_THRESHOLD = 5;
    private static final int WARNINGS_THRESHOLD = 1;

    public List<Hotspot> analyzeAll(List<EntityInfo> entities) {
        List<Hotspot> results = new ArrayList<>();
        for (EntityInfo entity : entities) {
            Hotspot hs = computeHotspot(entity);
            if (hs != null) {
                results.add(hs);
            }
        }
        // sort by severity score
        results.sort(Comparator.comparingDouble(Hotspot::getScore).reversed());
        return results;
    }

    public void updateHotspot(EntityInfo entity, List<Hotspot> currentHotspots) {
        currentHotspots.removeIf(h -> h.getEntity().getEntityName().equals(entity.getEntityName()));
        Hotspot newHs = computeHotspot(entity);
        if (newHs != null) {
            currentHotspots.add(newHs);
            currentHotspots.sort(Comparator.comparingDouble(Hotspot::getScore).reversed());
        }
    }

    private Hotspot computeHotspot(EntityInfo entity) {
        double locFactor = Math.max(0, (entity.getLinesOfCode() - LOC_THRESHOLD) / 100.0);
        double depFactor = Math.max(0, (entity.getDependsOn().size() - DEPENDS_ON_THRESHOLD) / 2.0);
        double warnFactor = Math.max(0, (entity.getWarnings().size() - WARNINGS_THRESHOLD) * 2.5);

        double score = locFactor + depFactor + warnFactor;

        if (score > 1.0) {
            return new Hotspot(entity,determineReason(locFactor, depFactor, warnFactor),score);
        }
        return null;
    }

    private Set<String> determineReason(double locFactor, double depFactor, double warnFactor) {
        Set<String> reasons = new HashSet<>();
        if (locFactor > 0) reasons.add("Large Source Size");
        if (depFactor > 0) reasons.add("High Coupling");
        if (warnFactor > 0) reasons.add("High Issues");
        return reasons;
    }
}
