package com.example.bodhakfrontend.Nic.Model;

public enum Genes {

    DECOMPOSE_LONG_FUNCTION(0.85, 1.0, 1.0),

    REMOVE_UNUSED_CODE(1.0, 0.95, 1.0),

    IMPROVE_MODULARITY(1.0, 1.0, 0.85),

    EXTRACT_INTERFACE(1.0, 1.0, 0.90),

    SPLIT_CLASS(1.0, 1.0, 0.88),

    REDUCE_CIRCULAR_DEPENDENCY(1.0, 1.0, 0.80),

    INLINE_METHOD(0.92, 1.02, 1.0);


    private final double methodLengthFactor;
    private final double locFactor;
    private final double dependencyFactor;

    private Genes(double methodLengthFactor, double locFactor, double dependencyFactor) {
        this.methodLengthFactor = methodLengthFactor;
        this.locFactor = locFactor;
        this.dependencyFactor = dependencyFactor;
    }
    public double getMethodLengthFactor() {
        return methodLengthFactor;
    }

    public double getLocFactor() {
        return locFactor;
    }

    public double getDependencyFactor() {
        return dependencyFactor;
    }


}
