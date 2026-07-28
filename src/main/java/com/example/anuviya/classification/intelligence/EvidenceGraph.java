package com.example.anuviya.classification.intelligence;

import java.util.*;

public class EvidenceGraph {
    private final List<Evidence> allEvidence = new ArrayList<>();
    private final Map<EvidenceType, List<Evidence>> byType = new HashMap<>();

    public void addEvidence(Evidence e) {
        allEvidence.add(e);
        byType.computeIfAbsent(e.type(), k -> new ArrayList<>()).add(e);
    }

    public List<Evidence> getAllEvidence() {
        return Collections.unmodifiableList(allEvidence);
    }

    public List<Evidence> getEvidenceByType(EvidenceType type) {
        return Collections.unmodifiableList(byType.getOrDefault(type, List.of()));
    }

    public boolean hasEvidence(EvidenceType type, String pattern) {
        return byType.getOrDefault(type, List.of()).stream()
            .anyMatch(e -> e.pattern().equalsIgnoreCase(pattern));
    }

    public boolean hasEvidenceMatching(EvidenceType type, Collection<String> patterns) {
        if (patterns == null || patterns.isEmpty()) return false;
        List<Evidence> list = byType.getOrDefault(type, List.of());
        for (Evidence e : list) {
            for (String pattern : patterns) {
                if (e.pattern().equalsIgnoreCase(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Optional<Evidence> findEvidence(EvidenceType type, String pattern) {
        return byType.getOrDefault(type, List.of()).stream()
            .filter(e -> e.pattern().equalsIgnoreCase(pattern))
            .findFirst();
    }
}
