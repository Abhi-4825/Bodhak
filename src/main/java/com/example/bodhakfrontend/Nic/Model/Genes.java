package com.example.bodhakfrontend.Nic.Model;

public enum Genes {

    DECOMPOSE_LONG_FUNCTION("Break Large Functions/Methods",0.85, 1.0, 1.0),

    REMOVE_UNUSED_CODE("Remove Dead Codes",1.0, 0.95, 1.0),

    IMPROVE_MODULARITY("Improve Class Structure",1.0, 1.0, 0.85),

    EXTRACT_INTERFACE("Introduce Interfaces",1.0, 1.0, 0.90),

    SPLIT_CLASS("Split large classes",1.0, 1.0, 0.88),

    REDUCE_CIRCULAR_DEPENDENCY("Fix Circular Dependencies",1.0, 1.0, 0.80),

    INLINE_METHOD("Inline Small Functions/Methods",0.92, 1.02, 1.0);

    private final String dispalyName;
    private final double methodLengthFactor;
    private final double locFactor;
    private final double dependencyFactor;

    private Genes(String dispalyName,double methodLengthFactor, double locFactor, double dependencyFactor) {
        this.dispalyName = dispalyName;
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

    public String getDispalyName() {return dispalyName;}


}
