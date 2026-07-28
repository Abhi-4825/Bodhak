package com.example.anuviya.ui.nav;

/**
 * Top-level navigation tabs for Bodhak.
 *
 * Adding a new tab in the future = add one entry here.
 * Everything else adapts automatically.
 */
public enum NavTab {

    WORKSPACE            ("⬡  Workspace"),
    ANALYSIS_REPORT      ("⬡  Analysis Report"),
    DEPENDENCY_EXPLORER  ("⬡  Dependency Explorer"),
    DEFECTS              ("⬡  Defects"),
    RUNTIME_VALIDATION   ("⬡  Runtime Validation");

    private final String label;

    NavTab(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
