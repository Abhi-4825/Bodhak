package com.example.anuviya.analyzer.ai.evidence.model;

import java.util.Set;

public record PackageCycleEvidence(
        Set<String> packages,
        int cycleSize
) {}
