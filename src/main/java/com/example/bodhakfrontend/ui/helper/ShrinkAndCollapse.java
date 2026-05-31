package com.example.bodhakfrontend.ui.helper;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class ShrinkAndCollapse {
    public void enterFocusedAnalysisMode(
            SplitPane splitPane,
            BorderPane analysisWorkspace,
            BorderPane rightPanel,
            Node overviewContent
    ) {

        analysisWorkspace.setCenter(overviewContent);

        analysisWorkspace.setVisible(true);
        analysisWorkspace.setManaged(true);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(350),
                        new KeyValue(splitPane.getDividers().get(0).positionProperty(), 0.18),
                        new KeyValue(splitPane.getDividers().get(1).positionProperty(), 0.98)
                )
        );

        timeline.play();
    }
    public void exitFocusedAnalysisMode(
            SplitPane splitPane,
            BorderPane analysisWorkspace
    ) {

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(350),
                        new KeyValue(splitPane.getDividers().get(0).positionProperty(), 0.18),
                        new KeyValue(splitPane.getDividers().get(1).positionProperty(), 0.58)
                )
        );

        timeline.setOnFinished(e -> {
            analysisWorkspace.setVisible(false);
            analysisWorkspace.setManaged(false);
        });

        timeline.play();
    }
}
