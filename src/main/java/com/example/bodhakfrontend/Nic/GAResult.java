package com.example.bodhakfrontend.Nic;

import com.example.bodhakfrontend.Nic.Model.Chromosome;
import com.example.bodhakfrontend.Nic.Model.Metrics;

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
