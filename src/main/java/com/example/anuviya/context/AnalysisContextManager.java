package com.example.anuviya.context;

import com.example.anuviya.context.state.AppState;
import javafx.application.Platform;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages the current active AnalysisContext, updating registered workspaces.
 * Completely decoupled from JavaFX/UI dependencies.
 */
public class AnalysisContextManager {

    private final AtomicReference<AnalysisContext> currentContext = new AtomicReference<>();
    private final AppState appState;

    public AnalysisContextManager(AppState appState) {
        this.appState = appState;
    }

    public AnalysisContext getCurrentContext() {
        return currentContext.get();
    }

    public void replace(AnalysisContext context) {
        if (context == null) {
            return;
        }

        this.currentContext.set(context);
        Platform.runLater(() -> {
            appState.update(context);
        });
    }

    public AppState getAppState() {
        return appState;
    }
}
