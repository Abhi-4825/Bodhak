package com.example.anuviya.ui.workspace;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class WorkspaceContainer extends StackPane {

    private final EditorWorkspace editorWorkspace;
    private final AnalysisWorkspaceView analysisWorkspace;

    public WorkspaceContainer(EditorWorkspace editorWorkspace, AnalysisWorkspaceView analysisWorkspace) {
        this.editorWorkspace = editorWorkspace;
        this.analysisWorkspace = analysisWorkspace;

        // Initial state
        analysisWorkspace.setOpacity(0);
        analysisWorkspace.setVisible(false);
        analysisWorkspace.setManaged(false);

        this.getChildren().addAll(editorWorkspace, analysisWorkspace);
    }

    public void switchToAnalysisMode(SplitPane splitPane, Node dependenciesView, Node methodsView, Node healthView) {
        analysisWorkspace.setOverviewContent(dependenciesView, methodsView, healthView);

        // Prepare for animation
        analysisWorkspace.setVisible(true);
        analysisWorkspace.setManaged(true);
        
        // Fade in Analysis, Fade out Editor
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), analysisWorkspace);
        fadeIn.setToValue(1.0);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), editorWorkspace);
        fadeOut.setToValue(0.0);
        
        // Animate SplitPane to hide the right sidebar (assumes 3 items in SplitPane)
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(350),
                        new KeyValue(splitPane.getDividers().get(0).positionProperty(), 0.18),
                        new KeyValue(splitPane.getDividers().get(1).positionProperty(), 0.98) // Collapse right
                )
        );

        fadeOut.setOnFinished(e -> {
            editorWorkspace.setVisible(false);
            editorWorkspace.setManaged(false);
        });

        fadeIn.play();
        fadeOut.play();
        timeline.play();
    }

    public void switchToEditorMode(SplitPane splitPane) {
        // Prepare for animation
        editorWorkspace.setVisible(true);
        editorWorkspace.setManaged(true);

        // Fade in Editor, Fade out Analysis
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), editorWorkspace);
        fadeIn.setToValue(1.0);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), analysisWorkspace);
        fadeOut.setToValue(0.0);

        // Restore SplitPane
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(350),
                        new KeyValue(splitPane.getDividers().get(0).positionProperty(), 0.18),
                        new KeyValue(splitPane.getDividers().get(1).positionProperty(), 0.58) // Restore right
                )
        );

        fadeOut.setOnFinished(e -> {
            analysisWorkspace.setVisible(false);
            analysisWorkspace.setManaged(false);
        });

        fadeIn.play();
        fadeOut.play();
        timeline.play();
    }
}
