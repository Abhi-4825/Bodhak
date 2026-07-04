package com.example.bodhak.ui.analysisReport.uiComponent;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Base class for all detailed inspector content panels.
 */
public abstract class AnalysisInspectorContent extends VBox {

    public AnalysisInspectorContent() {
        getStyleClass().add("ar-inspector-content");
        setFillWidth(true);
    }

    /** Returns the visual title of this inspector section. */
    public abstract String getTitle();

    /** Returns the icon node for this inspector. */
    public abstract Node getIcon();

    /** Returns the confidence string (e.g. "85% CONFIDENCE"), or null if none. */
    public abstract String getConfidenceText();

    /** Hooks to bind the panel reactively to the dashboard state. */
    public abstract void bindToState(AnalysisReportState state);
}
