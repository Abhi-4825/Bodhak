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
    private final int maxLoc;
    private final int minLoc;
    private final int maxMethodLoc;
    private final int minMethodLoc;
    private final int maxDeps;
    private final int minDeps;
    private final double averageDeps;


    public Metrics(long totalClass, long totalLoc, double averageLoc, int totalMethods, double averageMethodLoc, double averageDeps,int totalDependency, int totalCircularDependencies, int totalUnusedClasses, long totalFanIn, int maxLoc, int minLoc, int maxMethodLoc, int minMethodLoc, int maxDeps, int minDeps) {
        this.totalClass = totalClass;
        this.totalLoc = totalLoc;
        this.averageLoc = averageLoc;
        this.totalMethods = totalMethods;
        this.averageMethodLoc = averageMethodLoc;
        this.totalDependency = totalDependency;
        this.totalCircularDependencies = totalCircularDependencies;
        this.totalUnusedClasses = totalUnusedClasses;
        this.totalFanIn = totalFanIn;
        this.maxLoc = maxLoc;
        this.minLoc = minLoc;
        this.maxMethodLoc = maxMethodLoc;
        this.minMethodLoc = minMethodLoc;
        this.maxDeps = maxDeps;
        this.minDeps = minDeps;
        this.averageDeps = averageDeps;
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
    public int getMaxLoc() {return maxLoc;}
    public int getMinLoc() {return minLoc;}
    public int getMaxMethodLoc() {return maxMethodLoc;}
    public int getMinMethodLoc() {return minMethodLoc;}
    public int getMaxDeps() {return maxDeps;}
    public int getMinDeps() {return minDeps;}
    public double getAverageDeps() {return averageDeps;}


}
