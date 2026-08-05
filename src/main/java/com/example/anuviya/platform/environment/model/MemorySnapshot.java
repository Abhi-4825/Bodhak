package com.example.anuviya.platform.environment.model;

public record MemorySnapshot(
    long totalPhysicalMb,
    long freePhysicalMb,
    long warningThresholdMb,
    long criticalThresholdMb
) {
    /** Free RAM is below the warning threshold — show dialog, user may proceed. */
    public boolean isLowMemory() {
        return freePhysicalMb < warningThresholdMb;
    }

    /** Free RAM is so low the analysis MUST NOT run. Disable "Analyze Anyway". */
    public boolean isCriticallyLow() {
        return freePhysicalMb < criticalThresholdMb;
    }
}
