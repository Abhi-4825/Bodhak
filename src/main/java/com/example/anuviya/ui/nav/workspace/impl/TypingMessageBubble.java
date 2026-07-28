package com.example.anuviya.ui.nav.workspace.impl;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class TypingMessageBubble extends HBox {
    private final Label textLabel;
    private final StringBuilder fullText = new StringBuilder();
    private int charIndex = 0;
    private Timeline typingTimeline;
    private final ScrollPane parentScroll;
    private boolean isStreamingMode;

    private final javafx.scene.control.ProgressIndicator spinner;

    public TypingMessageBubble(String icon, String color, String bgColor, ScrollPane parentScroll, boolean isStreamingMode) {
        super(12);
        this.parentScroll = parentScroll;
        this.isStreamingMode = isStreamingMode;
        setAlignment(Pos.TOP_LEFT);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(28, 28);
        avatar.setMinSize(28, 28);
        avatar.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 6;");
        Label avatarIcon = new Label(icon);
        avatarIcon.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");
        avatar.getChildren().add(avatarIcon);

        textLabel = new Label();
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #dde4e5; -fx-padding: 4 0 0 0; -fx-line-spacing: 4px;");

        spinner = new javafx.scene.control.ProgressIndicator();
        spinner.setPrefSize(14, 14);
        spinner.setMinSize(14, 14);
        spinner.setMaxSize(14, 14);
        spinner.setStyle("-fx-progress-color: " + color + "; -fx-background-color: transparent;");
        spinner.setVisible(isStreamingMode);
        spinner.setManaged(isStreamingMode);

        javafx.scene.layout.VBox contentArea = new javafx.scene.layout.VBox(6);
        contentArea.setAlignment(Pos.TOP_LEFT);
        
        HBox spinnerBox = new HBox(8);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);
        spinnerBox.getChildren().add(spinner);

        contentArea.getChildren().addAll(textLabel, spinnerBox);

        getChildren().addAll(avatar, contentArea);

        typingTimeline = new Timeline(new KeyFrame(Duration.millis(30), e -> {
            boolean changed = false;
            if (charIndex < fullText.length()) {
                // Adaptive speed: catch up if we fall behind
                int step = Math.max(1, (fullText.length() - charIndex) / 10);
                charIndex = Math.min(fullText.length(), charIndex + step);
                changed = true;
            } 
            
            String displayText = fullText.substring(0, charIndex);
            if (!this.isStreamingMode && charIndex >= fullText.length()) {
                typingTimeline.stop();
            }

            if (changed) {
                textLabel.setText(displayText);
                if (this.parentScroll != null) {
                    this.parentScroll.setVvalue(1.0);
                }
            }
        }));
        typingTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void startTyping(String text) {
        Platform.runLater(() -> {
            fullText.setLength(0);
            fullText.append(text);
            charIndex = 0;
            textLabel.setText("");
            typingTimeline.play();
        });
    }

    public void appendChunk(String chunk) {
        Platform.runLater(() -> {
            fullText.append(chunk);
            if (typingTimeline.getStatus() != Timeline.Status.RUNNING) {
                typingTimeline.play();
            }
        });
    }

    public void finishStreaming() {
        Platform.runLater(() -> {
            this.isStreamingMode = false;
            spinner.setVisible(false);
            spinner.setManaged(false);
            textLabel.setText(fullText.toString());
            typingTimeline.stop();
        });
    }
}
