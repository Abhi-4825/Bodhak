package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.Models.ClassHealthInfo;
import com.example.bodhakfrontend.Models.ProjectHealthSummary;

import java.util.Map;

public class ProjectHealthAnalyzer {

    public ProjectHealthSummary summarize(
            Map<String, ClassHealthInfo> healthMap
    ) {

        int total = healthMap.size();
        int healthy = 0;
        int warnings = 0;
        int god = 0;
        int circular = 0;
        int coupled = 0;

        for (ClassHealthInfo info : healthMap.values()) {

            if (info.getWarnings().isEmpty()) {
                healthy++;
            } else {
                warnings++;
            }

            if (info.isGodClass()) god++;
            if (info.isInCircularDependency()) circular++;
            if (info.isHighlyCoupled()) coupled++;
        }

        return new ProjectHealthSummary(
                total,
                healthy,
                warnings,
                god,
                circular,
                coupled
        );
    }
}
