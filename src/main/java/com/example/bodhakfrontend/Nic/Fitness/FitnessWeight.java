package com.example.bodhakfrontend.Nic.Fitness;

public class FitnessWeight {

    private final double locWeight;
    private final double methodWeight;
    private final double dependencyWeight;

    public FitnessWeight(double locWeight,
                          double methodWeight,
                          double dependencyWeight) {

        double sum = locWeight + methodWeight + dependencyWeight;

        if (sum == 0) {
            throw new IllegalArgumentException("Weights sum cannot be zero");
        }

        this.locWeight = locWeight / sum;
        this.methodWeight = methodWeight / sum;
        this.dependencyWeight = dependencyWeight / sum;
    }

    public double getLocWeight() { return locWeight; }
    public double getMethodWeight() { return methodWeight; }
    public double getDependencyWeight() { return dependencyWeight; }
}
