package com.example.anuviya.classification.intelligence;

import java.util.List;
import java.util.Map;

public record DetectedTechnology(
    String id,
    String displayName,
    String family, // framework, library, database, messaging, etc.
    String category,
    List<Evidence> matchedEvidence,
    List<String> capabilities,
    String version,
    double confidence,
    Map<String, String> metadata
) {}
