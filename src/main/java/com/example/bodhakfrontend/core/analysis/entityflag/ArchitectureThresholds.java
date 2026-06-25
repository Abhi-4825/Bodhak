package com.example.bodhakfrontend.core.analysis.entityflag;

public record ArchitectureThresholds(

        int minGodClassLoc,

        int minGodClassMethods,

        int minGodClassFields,

        int minHighCoupling,

        int minFanIn,

        int minFanOut

) {

    public static ArchitectureThresholds defaults() {

        return new ArchitectureThresholds(

                200, // LOC
                15,  // Methods
                8,   // Fields

                10,  // Coupling

                8,   // FanIn
                8    // FanOut
        );
    }
}
