package com.example.bodhak.analyzer.ai.evidence.model;


import java.util.Set;

public record CircularDependencyEvidence(

        Set<String> entities,

        int cycleSize

) {
}
