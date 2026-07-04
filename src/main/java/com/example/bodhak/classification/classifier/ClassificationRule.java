package com.example.bodhak.classification.classifier;

import com.example.bodhak.classification.Capability;
import com.example.bodhak.classification.ProjectType;
import com.example.bodhak.classification.capability.CapabilityProfile;

import java.util.Set;

/**
 * A single rule that maps a capability pattern to a project type.
 *
 * @param projectType             The project type this rule can infer
 * @param requiredCapabilities    ALL of these must be present
 * @param boostingCapabilities    If present, increase confidence
 * @param conflictingCapabilities If present, decrease confidence
 * @param baseConfidence          Starting confidence when required caps are met
 * @param priority                Higher priority rules are evaluated first
 */
public record ClassificationRule(
        ProjectType projectType,
        Set<Capability> requiredCapabilities,
        Set<Capability> boostingCapabilities,
        Set<Capability> conflictingCapabilities,
        double baseConfidence,
        int priority
) {
    /**
     * Evaluate this rule against a capability profile.
     * Returns confidence > 0 if the rule matches, 0 otherwise.
     */
    public double evaluate(CapabilityProfile profile) {
        // All required capabilities must be present
        for (Capability req : requiredCapabilities) {
            if (!profile.hasCapability(req)) return 0.0;
        }

        double confidence = baseConfidence;

        // Boost for each supporting capability present
        for (Capability boost : boostingCapabilities) {
            if (profile.hasCapability(boost)) {
                confidence += 0.1 * profile.getConfidence(boost);
            }
        }

        // Reduce for conflicting capabilities
        for (Capability conflict : conflictingCapabilities) {
            if (profile.hasCapability(conflict)) {
                confidence -= 0.1;
            }
        }

        return Math.max(0.0, Math.min(1.0, confidence));
    }
}
