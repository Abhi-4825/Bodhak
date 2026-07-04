package com.example.bodhak.ui.dependencyExplorer.components;

import com.fxgraph.graph.PannableCanvas;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class CustomViewportGestures {

    private final PannableCanvas canvas;
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;

    private boolean spacePressed = false;

    public CustomViewportGestures(PannableCanvas canvas) {
        this.canvas = canvas;
    }

    public void setSpacePressed(boolean spacePressed) {
        this.spacePressed = spacePressed;
    }

    private boolean isCellNode(Node node) {
        Node current = node;
        while (current != null) {
            if (current.getStyleClass().contains("dd-node-focus") || current.getStyleClass().contains("dd-node-normal")) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
        return event -> {
            // Ignore left-clicks on cell nodes to allow node dragging
            if (event.getTarget() instanceof Node) {
                if (isCellNode((Node) event.getTarget())) {
                    return;
                }
            }
            
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = canvas.getTranslateX();
            translateAnchorY = canvas.getTranslateY();
            event.consume();
        };
    }

    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
        return event -> {
            // Ignore drags on cell nodes to allow node dragging
            if (event.getTarget() instanceof Node) {
                if (isCellNode((Node) event.getTarget())) {
                    return;
                }
            }

            double newTranslateX = translateAnchorX + event.getSceneX() - mouseAnchorX;
            double newTranslateY = translateAnchorY + event.getSceneY() - mouseAnchorY;
            
            canvas.setTranslateX(newTranslateX);
            canvas.setTranslateY(newTranslateY);
            event.consume();
        };
    }

    public EventHandler<ScrollEvent> getOnScrollEventHandler() {
        return event -> {
            // Scroll-wheel zooming is disabled as per user request
            // Just keep the zoom toolbar (+/-) buttons
            event.consume();
        };
    }
}
