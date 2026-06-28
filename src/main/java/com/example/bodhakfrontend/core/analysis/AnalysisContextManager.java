package com.example.bodhakfrontend.core.analysis;

import com.example.bodhakfrontend.ui.ProjectAnalysis.state.ProjectAnalysisState;
import javafx.application.Platform;

public class AnalysisContextManager {

    private volatile AnalysisContext currentContext;
    private final ProjectAnalysisState analysisState;

    public AnalysisContextManager(ProjectAnalysisState analysisState) {
        this.analysisState = analysisState;
    }

    public AnalysisContext getCurrentContext() {
        return currentContext;
    }

    public void replace(AnalysisContext context) {

        if (context == null) {
            return;
        }

        this.currentContext = context;
        Platform.runLater(()->{analysisState.update(context);});

    }

    public ProjectAnalysisState getAnalysisState() {
        return analysisState;
    }
}
