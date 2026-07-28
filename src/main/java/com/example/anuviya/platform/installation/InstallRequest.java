package com.example.anuviya.platform.installation;

import com.example.anuviya.platform.PackageCategory;

public record InstallRequest(
    String packageId,
    PackageCategory category,
    String version
) {}
