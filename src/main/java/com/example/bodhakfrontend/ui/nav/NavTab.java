package com.example.bodhakfrontend.ui.nav;

/**
 * Top-level navigation tabs for Bodhak.
 *
 * Adding a new tab in the future = add one entry here.
 * Everything else adapts automatically.
 */
public enum NavTab {

    OVERVIEW     ("⬡  Overview"),
    ARCHITECTURE ("⬡  Architecture"),
    CODE_HEALTH  ("⬡  Code Health"),
    PERFORMANCE  ("⬡  Performance");

    private final String label;

    NavTab(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
