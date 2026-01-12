package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.Models.ClassHealthInfo;
import com.example.bodhakfrontend.Models.IssueType;
import com.example.bodhakfrontend.Models.ProjectHealthSummary;
import com.example.bodhakfrontend.projectAnalysis.warning.WarningBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectHealthAnalyzer {

    public ProjectHealthSummary summarize(
            Map<String, ClassHealthInfo> healthMap
    ) {
        WarningBuilder warningBuilder = new WarningBuilder();

        int total = healthMap.size();
        int healthy = 0;
        int warnings = 0;
        int god = 0;
        int circular = 0;
        int coupled = 0;

        for (ClassHealthInfo info : healthMap.values()) {
               Set<IssueType> issues = info.getIssueTypes();
            if (issues.isEmpty()) {
                healthy++;
           } else {
                  warnings++;
            }

            if (issues.contains(IssueType.GOD_CLASS)) god++;
            if (issues.contains(IssueType.CIRCULAR_DEPENDENCY)) circular++;
          if (issues.contains(IssueType.HIGH_COUPLING)) coupled++;
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
