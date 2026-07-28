package com.example.anuviya.analyzer.ai.evidence.model;


import java.util.Set;

public record CircularDependencyEvidence(

        Set<String> entities,

        int cycleSize

) {
}
