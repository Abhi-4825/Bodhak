package com.example.anuviya.orchestration.progress;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class LoadingPipelineView extends VBox {

    private final List<StageRow> stageRows = new ArrayList<>();
    private int currentStageIndex = 0;

    public LoadingPipelineView() {
        setSpacing(10);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: rgba(21, 28, 35, 0.4); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-background-radius: 8;");

        // Define the stages
        addStage("Discover Project");
        addStage("Scan Files");
        addStage("Parse Sources");
        addStage("Build AST");
        addStage("Resolve Symbols");
        addStage("Build Reference Database");
        addStage("Build Dependency Graph");
        addStage("Build Graph Index");
        addStage("Detect Technologies");
        addStage("Run Classification");
        addStage("Compute Metrics");
        addStage("Finalize Analysis");

        updateStageStates();
    }

    private void addStage(String name) {
        StageRow row = new StageRow(name);
        stageRows.add(row);
        getChildren().add(row);
    }

    public void setStage(String stageName) {
        int index = -1;
        for (int i = 0; i < stageRows.size(); i++) {
            if (stageRows.get(i).name.equalsIgnoreCase(stageName)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            currentStageIndex = index;
            updateStageStates();
        }
    }

    public void completeAll() {
        currentStageIndex = stageRows.size();
        updateStageStates();
    }

    private void updateStageStates() {
        for (int i = 0; i < stageRows.size(); i++) {
            StageRow row = stageRows.get(i);
            if (i < currentStageIndex) {
                row.setCompleted();
            } else if (i == currentStageIndex) {
                row.setActive();
            } else {
                row.setPending();
            }
        }
    }

    private static class StageRow extends HBox {
        private final String name;
        private final Label statusLabel = new Label();
        private final Label nameLabel = new Label();
        private final Circle pulseCircle = new Circle(4);
        private ScaleTransition pulseAnimation;

        public StageRow(String name) {
            this.name = name;
            setSpacing(10);
            setAlignment(Pos.CENTER_LEFT);

            nameLabel.setText(name);
            nameLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 11px;");

            statusLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold;");

            getChildren().addAll(statusLabel, pulseCircle, nameLabel);
            
            pulseCircle.setManaged(false);
            pulseCircle.setVisible(false);
        }

        public void setCompleted() {
            stopPulse();
            statusLabel.setText("✓");
            statusLabel.setStyle("-fx-text-fill: #00ff66; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold;");
            statusLabel.setManaged(true);
            statusLabel.setVisible(true);
            nameLabel.setStyle("-fx-text-fill: #dce3ec; -fx-font-family: 'Inter'; -fx-font-size: 11px; -fx-opacity: 0.85;");
            pulseCircle.setVisible(false);
            pulseCircle.setManaged(false);
        }

        public void setActive() {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            pulseCircle.setFill(Color.web("#00daf3"));
            pulseCircle.setVisible(true);
            pulseCircle.setManaged(true);
            nameLabel.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Inter'; -fx-font-size: 11px; -fx-font-weight: bold;");
            startPulse();
        }

        public void setPending() {
            stopPulse();
            statusLabel.setText("○");
            statusLabel.setStyle("-fx-text-fill: #607274; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
            statusLabel.setManaged(true);
            statusLabel.setVisible(true);
            nameLabel.setStyle("-fx-text-fill: #607274; -fx-font-family: 'Inter'; -fx-font-size: 11px;");
            pulseCircle.setVisible(false);
            pulseCircle.setManaged(false);
        }

        private void startPulse() {
            if (pulseAnimation == null) {
                pulseAnimation = new ScaleTransition(Duration.millis(600), pulseCircle);
                pulseAnimation.setFromX(1.0);
                pulseAnimation.setFromY(1.0);
                pulseAnimation.setToX(1.6);
                pulseAnimation.setToY(1.6);
                pulseAnimation.setAutoReverse(true);
                pulseAnimation.setCycleCount(Animation.INDEFINITE);
            }
            pulseAnimation.play();
        }

        private void stopPulse() {
            if (pulseAnimation != null) {
                pulseAnimation.stop();
            }
        }
    }
}
