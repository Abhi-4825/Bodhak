package com.example.bodhakfrontend.engine.optimization.Model;

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
