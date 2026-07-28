package com.example.anuviya.platform.environment.model;

public record HardwareInfo(
    String cpuName,
    double ramGb,
    String gpuName,
    double vramGb,
    boolean hasCompatibleGpu
) {}
