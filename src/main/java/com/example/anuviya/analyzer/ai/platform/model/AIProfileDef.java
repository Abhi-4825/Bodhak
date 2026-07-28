package com.example.anuviya.analyzer.ai.platform.model;

import java.util.ArrayList;
import java.util.List;

public record AIProfileDef(
    String id,
    String displayName,
    String description,
    List<ModelCapability> requiredCapabilities,
    int minContext,
    double preferredTemperature,
    double preferredTopP,
    List<String> recommendedModels,
    String outputType,
    String outputSchema
) {
    public AIProfileDef {
        if (requiredCapabilities == null) requiredCapabilities = new ArrayList<>();
        if (recommendedModels == null) recommendedModels = new ArrayList<>();
    }
}
