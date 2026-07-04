package com.example.bodhak.classification.intelligence;

import com.example.bodhak.classification.Capability;
import com.example.bodhak.classification.capability.CapabilityProfile;

import java.util.*;

public class CapabilityResolver {
    public CapabilityProfile resolve(TechnologyCatalog catalog) {
        Map<Capability, Double> capabilities = new EnumMap<>(Capability.class);
        int totalEvidenceCount = 0;

        for (DetectedTechnology tech : catalog.detectedTechnologies()) {
            totalEvidenceCount += tech.matchedEvidence().size();
            for (String capName : tech.capabilities()) {
                try {
                    Capability cap = Capability.valueOf(capName);
                    capabilities.merge(cap, tech.confidence(), Math::max);
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown capability: " + capName + " in technology " + tech.id());
                }
            }
        }

        return new CapabilityProfile(capabilities, totalEvidenceCount);
    }
}
