package com.example.anuviya.analyzer.ai.platform.model;

import java.util.ArrayList;
import java.util.List;

public record ModelInfo(
    String id,
    String displayName,
    double sizeGb,
    List<ModelCapability> capabilities,
    boolean installed
) {
    public ModelInfo {
        if (capabilities == null) {
            capabilities = new ArrayList<>();
        }
    }
}
