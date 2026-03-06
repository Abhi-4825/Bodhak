package com.example.bodhakfrontend.ui.Optimization;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Queue;

public class OptimizationPanel {

    private final BorderPane root;
    private final VBox logContainer;
    private final StackPane spinnerContainer;
    
    private final Queue<String> messageQueue = new LinkedList<>();
    private boolean typingInProgress = false;
    private Runnable onTypingFinished;

    public OptimizationPanel() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #121212;");

        // UI Components
        VBox centerContent = new VBox(30);
        centerContent.setAlignment(Pos.CENTER);

        // 1. Animated AI Spinner
        spinnerContainer = createSpinner();
        
        // 2. Pulsing Title
        Label title = new Label("Optimizing Architecture with Genetic Algorithm...");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8ab4f8;");
        FadeTransition titleFade = new FadeTransition(Duration.seconds(1.5), title);
        titleFade.setFromValue(0.4);
        titleFade.setToValue(1.0);
        titleFade.setCycleCount(Animation.INDEFINITE);
        titleFade.setAutoReverse(true);
        titleFade.play();

        // 3. Log Container
        logContainer = new VBox(5);
        logContainer.setAlignment(Pos.TOP_CENTER);
        logContainer.setMaxHeight(150);

        centerContent.getChildren().addAll(spinnerContainer, title, logContainer);
        
        root.setCenter(centerContent);
    }

    private StackPane createSpinner() {
        StackPane stack = new StackPane();
        
        Circle baseCircle = new Circle(40, Color.TRANSPARENT);
        baseCircle.setStroke(Color.web("#2d2d2d"));
        baseCircle.setStrokeWidth(4);
        
        Arc arc1 = createArc(40, 4, Color.web("#8ab4f8"), 0, 90);
        Arc arc2 = createArc(50, 2, Color.web("#00ff99"), 180, 120);
        Arc arc3 = createArc(30, 3, Color.web("#e6c06f"), 90, 60);

        rotateNode(arc1, Duration.seconds(2), true);
        rotateNode(arc2, Duration.seconds(3), false);
        rotateNode(arc3, Duration.seconds(1.5), true);

        stack.getChildren().addAll(baseCircle, arc1, arc2, arc3);
        return stack;
    }

    private Arc createArc(double radius, double strokeWidth, Color color, double startAngle, double length) {
        Arc arc = new Arc(0, 0, radius, radius, startAngle, length);
        arc.setType(ArcType.OPEN);
        arc.setStroke(color);
        arc.setStrokeWidth(strokeWidth);
        arc.setFill(Color.TRANSPARENT);
        return arc;
    }

    private void rotateNode(Node node, Duration duration, boolean clockwise) {
        RotateTransition rt = new RotateTransition(duration, node);
        rt.setByAngle(clockwise ? 360 : -360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();
    }

    public BorderPane getRoot() {
        return root;
    }

    //  Public method called by GA progress
    public void appendMessage(String message) {
        Platform.runLater(() -> {
            messageQueue.add(message);
            if (!typingInProgress) {
                processNextMessage();
            }
        });
    }

    private void processNextMessage() {
        String message = messageQueue.poll();
        if (message == null) {
            typingInProgress = false;
            
            // Artificial delay before finishing to ensure the success is visible
            PauseTransition pause = new PauseTransition(Duration.millis(800));
            pause.setOnFinished(event -> transitionToReport());
            pause.play();
            return;
        }
        
        typingInProgress = true;
        
        Label logLabel = new Label("> " + message);
        logLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-text-fill: #00ff99;");
        logLabel.setOpacity(0);
        
        logContainer.getChildren().add(logLabel);
        
        // Keep only last 5 logs
        if (logContainer.getChildren().size() > 6) {
            logContainer.getChildren().remove(0);
        }
        
        // Fade older logs
        int size = logContainer.getChildren().size();
        for(int i=0; i<size; i++) {
            Node node = logContainer.getChildren().get(i);
            // Lower opacity for older entries, bottom entry is 1.0 opacity
            double targetOpacity = (i + 1.0) / size;
            node.setOpacity(targetOpacity);
        }

        // Slight scroll up entrance
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), logLabel);
        tt.setFromY(10);
        tt.setToY(0);
        
        FadeTransition ft = new FadeTransition(Duration.millis(150), logLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        ParallelTransition pt = new ParallelTransition(logLabel, tt, ft);
        pt.setOnFinished(e -> {
            // small delay before next msg
            PauseTransition delay = new PauseTransition(Duration.millis(70));
            delay.setOnFinished(ev -> processNextMessage());
            delay.play();
        });
        pt.play();
    }
    
    private void transitionToReport() {
        if(onTypingFinished != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(600), root.getCenter());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> onTypingFinished.run());
            ft.play();
        }
    }

    public void setOnTypingFinished(Runnable onTypingFinished) {
        this.onTypingFinished = onTypingFinished;
    }

    public void replaceContent(Node node){
        root.setCenter(node);
        // Fade in new content
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}
