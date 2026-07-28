package com.example.anuviya.platform;

import com.example.anuviya.analyzer.ai.platform.model.ProviderKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ServicePackage(
    String id,
    String displayName,
    String description,
    PackageCategory category,
    String version,
    String license,
    double sizeGb,
    int contextLength,
    int requiredRamGb,
    int requiredVramGb,
    List<String> capabilities,
    List<String> recommendedTasks,
    double preferredTemperature,
    double preferredTopP,
    String homepage,
    ProviderKind kind, // for AI providers
    Map<String, InstallerDef> installerByOs, // OS name (windows, linux, mac) to InstallerDef
    Map<String, DownloadDef> downloadByRuntime, // runtime ID (ollama, lmstudio) to DownloadDef
    List<DependencyDef> dependencies
) {
    public ServicePackage {
        if (capabilities == null) capabilities = new ArrayList<>();
        if (recommendedTasks == null) recommendedTasks = new ArrayList<>();
        if (installerByOs == null) installerByOs = new HashMap<>();
        if (downloadByRuntime == null) downloadByRuntime = new HashMap<>();
        if (dependencies == null) dependencies = new ArrayList<>();
    }

    public record InstallerDef(
        String type,
        String url,
        String sha256,
        List<String> silentArgs,
        String verifyExecutable,
        String verifyCommand
    ) {}

    public record DownloadDef(
        String command,
        String repository
    ) {}

    public record DependencyDef(
        String id,
        String versionConstraint
    ) {}
}
