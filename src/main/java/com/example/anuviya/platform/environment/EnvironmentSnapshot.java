package com.example.anuviya.platform.environment;

import com.example.anuviya.platform.environment.model.DiskInfo;
import com.example.anuviya.platform.environment.model.HardwareInfo;
import com.example.anuviya.platform.environment.model.NetworkStatus;

import java.time.Instant;

public record EnvironmentSnapshot(
    String os,
    NetworkStatus networkStatus,
    HardwareInfo hardware,
    DiskInfo disk,
    Instant timestamp
) {}
