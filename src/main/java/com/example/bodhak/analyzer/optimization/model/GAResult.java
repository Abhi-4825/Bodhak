package com.example.bodhak.analyzer.optimization.model;

import java.util.List;

public record GAResult(
        Metrics beforeMetrics,
        Metrics afterMetrics,
        Chromosome bestChromosome,
        int generationsExecuted,
        boolean convergedEarly,
        double finalFitness,
        List<Double> fitnessHistory
) {
}
