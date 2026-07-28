package com.example.anuviya.compiler.root;

import com.example.anuviya.compiler.symbol.Symbol;
import com.example.anuviya.model.project.Evidence;
import com.example.anuviya.model.project.RootCapability;
import java.util.*;

/**
 * Aggregates raw evidence contributed by all scanners for candidate project surfaces.
 */
public class EvidenceAccumulator {
    private final Map<Symbol, Candidate> candidates = new HashMap<>();

    public static class Candidate {
        public final EnumSet<RootCapability> capabilities = EnumSet.noneOf(RootCapability.class);
        public final Set<Evidence> evidenceList = new HashSet<>();
        public final Set<String> archetypeEvidence = new HashSet<>();
    }

    public synchronized void contribute(Symbol symbol, RootCapability cap, Evidence evidence, String archetype) {
        Candidate candidate = candidates.computeIfAbsent(symbol, s -> new Candidate());
        candidate.capabilities.add(cap);
        candidate.evidenceList.add(evidence);
        if (archetype != null) {
            candidate.archetypeEvidence.add(archetype);
        }
    }

    public synchronized Map<Symbol, Candidate> getCandidates() {
        return Collections.unmodifiableMap(new HashMap<>(candidates));
    }
}
