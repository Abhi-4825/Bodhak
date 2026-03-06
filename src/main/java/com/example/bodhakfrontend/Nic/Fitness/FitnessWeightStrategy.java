package com.example.bodhakfrontend.Nic.Fitness;

import com.example.bodhakfrontend.Nic.Model.Metrics;

public class FitnessWeightStrategy {
    public static FitnessWeight deriveWeights(Metrics metrics) {
        double locWeight = 1;
        double methodWeight = 1;
        double dependencyWeight = 1;
        double avgCoupling =
                metrics.getTotalDependency()
                        / (double) metrics.getTotalClass();
        if (avgCoupling > 10) {
            dependencyWeight = 2;
        }
        if (metrics.getAverageMethodLoc() > 20) {
            methodWeight = 2;
        }
        if (metrics.getTotalLoc() > 10000) {
            locWeight = 1.5;
        }
        return new FitnessWeight(
                locWeight,
                methodWeight,
                dependencyWeight
        );
    }
}

