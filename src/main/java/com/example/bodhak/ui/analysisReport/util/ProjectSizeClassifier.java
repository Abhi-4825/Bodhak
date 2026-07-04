package com.example.bodhak.ui.analysisReport.util;


import com.example.bodhak.ui.analysisReport.model.ProjectSize;

public final class ProjectSizeClassifier {

    private ProjectSizeClassifier() {}

    public static ProjectSize classify(
            int entities,
            long loc
    ) {

        if (entities < 20 && loc < 2_000) {
            return ProjectSize.TINY;
        }

        if (entities < 100 && loc < 10_000) {
            return ProjectSize.SMALL;
        }

        if (entities < 500 && loc < 100_000) {
            return ProjectSize.MEDIUM;
        }

        if (entities < 2_000 && loc < 1_000_000) {
            return ProjectSize.LARGE;
        }

        return ProjectSize.ENTERPRISE;
    }

}
