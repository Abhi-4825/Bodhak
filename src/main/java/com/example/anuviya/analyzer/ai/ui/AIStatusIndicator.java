package com.example.anuviya.analyzer.ai.ui;

import com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus;
import com.example.anuviya.platform.state.PlatformState;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AIStatusIndicator extends HBox {
    private final Circle dot = new Circle(6);
    private final Label textLabel = new Label("AI Status");

    public AIStatusIndicator() {
        setSpacing(6);
        getStyleClass().add("ai-status-indicator");
        getChildren().addAll(dot, textLabel);

        // Bind/Observe PlatformState
        PlatformState.currentProperty().addListener((obs, oldState, newState) -> {
            if (newState != null) {
                Platform.runLater(() -> updateStatus(newState));
            }
        });

        // Initialize state if available
        if (PlatformState.getCurrent() != null) {
            updateStatus(PlatformState.getCurrent());
        } else {
            updateStatusOffline();
        }
    }

    private void updateStatus(PlatformState state) {
        var provider = state.getPreferredProvider();
        if (provider == null) {
            dot.setFill(Color.web("#ef4444")); // Red 🔴
            textLabel.setText("No AI Provider");
            Tooltip.install(this, new Tooltip("AI Status\nNo AI Provider installed on this system."));
        } else {
            RuntimeStatus status = provider.runtimeStatus();
            if (status == RuntimeStatus.READY) {
                if (state.getInstalledModels().isEmpty()) {
                    dot.setFill(Color.web("#eab308")); // Yellow 🟡
                    textLabel.setText("No Models");
                    Tooltip.install(this, new Tooltip("AI Status\nProvider: " + provider.info().displayName() + "\nStatus: Ready\nModels: None installed."));
                } else {
                    dot.setFill(Color.web("#22c55e")); // Green 🟢
                    var firstModel = state.getInstalledModels().get(0);
                    textLabel.setText("AI Ready (" + firstModel.displayName() + ")");
                    Tooltip.install(this, new Tooltip("AI Status\nProvider: " + provider.info().displayName() + "\nModel: " + firstModel.displayName() + "\nStatus: Ready"));
                }
            } else if (status == RuntimeStatus.STARTING) {
                dot.setFill(Color.web("#eab308")); // Yellow 🟡
                textLabel.setText("AI Starting...");
                Tooltip.install(this, new Tooltip("AI Status\nProvider: " + provider.info().displayName() + "\nStatus: Starting..."));
            } else {
                dot.setFill(Color.web("#6b7280")); // Gray ⚪
                textLabel.setText("AI Offline");
                Tooltip.install(this, new Tooltip("AI Status\nProvider: " + provider.info().displayName() + "\nStatus: Offline"));
            }
        }
    }

    private void updateStatusOffline() {
        dot.setFill(Color.web("#6b7280")); // Gray ⚪
        textLabel.setText("AI Scanning...");
        Tooltip.install(this, new Tooltip("AI Status\nScanning environment..."));
    }
}
