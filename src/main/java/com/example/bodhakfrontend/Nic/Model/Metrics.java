package com.example.bodhakfrontend.Nic.Model;

public class Metrics {
    private final long totalClass;
    private final long totalLoc;
    private final double averageLoc;
    private final int totalMethods;
    private final double averageMethodLoc;
    private final int totalDependency;
    private final int totalCircularDependencies;
    private final int totalUnusedClasses;
    private final long totalFanIn;


    public Metrics(long totalClass, long totalLoc, double averageLoc, int totalMethods, double averageMethodLoc, int totalDependency, int totalCircularDependencies, int totalUnusedClasses, long totalFanIn) {
        this.totalClass = totalClass;
        this.totalLoc = totalLoc;
        this.averageLoc = averageLoc;
        this.totalMethods = totalMethods;
        this.averageMethodLoc = averageMethodLoc;
        this.totalDependency = totalDependency;
        this.totalCircularDependencies = totalCircularDependencies;


        this.totalUnusedClasses = totalUnusedClasses;
        this.totalFanIn = totalFanIn;
    }

    public long getTotalLoc() {
        return totalLoc;
    }

    public int getTotalMethods() {
        return totalMethods;
    }

    public double getAverageMethodLoc() {
        return averageMethodLoc;
    }

    public int getTotalDependency() {
        return totalDependency;
    }
    public int getTotalCircularDependencies() {return totalCircularDependencies;}
    public int getTotalUnusedClasses() {return totalUnusedClasses;}
    public double getAverageLoc() {
        return averageLoc;
    }
    public long getTotalFanIn() {
        return totalFanIn;
    }

    public long getTotalClass() {
        return totalClass;
    }
}
