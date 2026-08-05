package com.example.anuviya.platform.environment;

import com.example.anuviya.platform.environment.model.DiskInfo;
import com.example.anuviya.platform.environment.model.HardwareInfo;
import com.example.anuviya.platform.environment.model.MemorySnapshot;
import com.example.anuviya.platform.environment.model.NetworkStatus;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.Instant;

public class EnvironmentScanner {
    private final NetworkService networkService;

    public EnvironmentScanner(NetworkService networkService) {
        this.networkService = networkService;
    }

    public EnvironmentSnapshot scan() {
        String os = System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + ")";
        NetworkStatus netStatus = networkService.status();
        HardwareInfo hardware = scanHardware();
        DiskInfo disk = scanDisk();
        return new EnvironmentSnapshot(os, netStatus, hardware, disk, Instant.now());
    }

    private HardwareInfo scanHardware() {
        String cpuName = System.getenv("PROCESSOR_IDENTIFIER");
        if (cpuName == null) {
            cpuName = System.getProperty("os.arch");
        }

        double ramGb = 0;
        try {
            long totalMemoryBytes = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
            ramGb = Math.round((totalMemoryBytes / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0;
        } catch (Throwable t) {
            ramGb = 8.0; // fallback
        }

        String gpuName = "Generic Graphics Device";
        double vramGb = 0.0;
        boolean hasCompatibleGpu = false;

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            try {
                Process process = Runtime.getRuntime().exec("wmic path win32_VideoController get name, AdapterRAM");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    boolean firstLine = true;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || firstLine) {
                            if (!line.isEmpty()) {
                                firstLine = false;
                            }
                            continue;
                        }
                        // Format is usually: AdapterRAM   Name
                        // e.g.: 4293918720   NVIDIA GeForce RTX 3050 Laptop GPU
                        String[] parts = line.split("\\s{2,}");
                        if (parts.length >= 2) {
                            String ramPart = parts[0].trim();
                            String namePart = parts[1].trim();
                            gpuName = namePart;
                            try {
                                long bytes = Long.parseLong(ramPart);
                                // AdapterRAM can sometimes overflow to negative or be uint32
                                if (bytes < 0) {
                                    bytes = 4L * 1024 * 1024 * 1024; // fallback 4GB
                                }
                                vramGb = Math.round((bytes / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0;
                            } catch (NumberFormatException nfe) {
                                vramGb = 4.0; // fallback
                            }
                            break;
                        } else if (parts.length == 1) {
                            gpuName = parts[0].trim();
                            vramGb = 4.0;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore and use generic
            }
        } else if (osName.contains("mac")) {
            gpuName = "Apple Silicon GPU";
            vramGb = ramGb * 0.5; // Unified memory estimation
        }

        String gpuLower = gpuName.toLowerCase();
        if (gpuLower.contains("nvidia") || gpuLower.contains("rtx") || gpuLower.contains("geforce") || gpuLower.contains("radeon") || gpuLower.contains("silicon") || vramGb >= 4.0) {
            hasCompatibleGpu = true;
        }

        return new HardwareInfo(cpuName, ramGb, gpuName, vramGb, hasCompatibleGpu);
    }

    private DiskInfo scanDisk() {
        File workspaceDir = new File(System.getProperty("user.dir"));
        double totalGb = Math.round((workspaceDir.getTotalSpace() / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0;
        double freeGb = Math.round((workspaceDir.getFreeSpace() / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0;
        return new DiskInfo(totalGb, freeGb);
    }

    public MemorySnapshot readMemorySnapshot(String modelId) {
        long totalMb = 8192;
        long freeMb = 4096;
        try {
            com.sun.management.OperatingSystemMXBean os =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            totalMb = os.getTotalMemorySize() / (1024 * 1024);
            freeMb  = os.getFreeMemorySize()  / (1024 * 1024);
        } catch (Throwable t) {
            // fallback
        }

        final long[] warnMb = new long[] { 8L * 1024 };
        final long[] criticalMb = new long[] { 512L };

        if (modelId != null) {
            PackageRegistry.getInstance().get(modelId).ifPresent(pkg -> {
                if (pkg.requiredRamGb() > 0) warnMb[0] = pkg.requiredRamGb() * 1024L;
                if (pkg.criticalRamMb() > 0) criticalMb[0] = pkg.criticalRamMb();
            });
        }

        return new MemorySnapshot(totalMb, freeMb, warnMb[0], criticalMb[0]);
    }
}
