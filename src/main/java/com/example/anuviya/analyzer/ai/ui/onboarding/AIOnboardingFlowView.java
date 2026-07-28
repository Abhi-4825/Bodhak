package com.example.anuviya.analyzer.ai.ui.onboarding;

import com.example.anuviya.platform.onboarding.OnboardingContext;
import com.example.anuviya.platform.onboarding.OnboardingStep;
import com.example.anuviya.platform.onboarding.flows.AIOnboardingFlow;
import com.example.anuviya.platform.onboarding.flows.step.AISteps;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.util.List;

public class AIOnboardingFlowView extends VBox {
    private final Label titleLabel = new Label("Anuviya AI Onboarding");
    private final Label stepTitleLabel = new Label("Checking environment...");
    private final Label detailLabel = new Label("Validating system requirements.");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Button actionButton = new Button("Start Setup");
    private final Button skipButton = new Button("Skip AI Features");
    
    private final OnboardingContext context = new OnboardingContext();
    private AIOnboardingFlow flow;
    private final Runnable onCompleteCallback;

    public AIOnboardingFlowView(Runnable onCompleteCallback) {
        this.onCompleteCallback = onCompleteCallback;
        
        setAlignment(Pos.CENTER);
        setSpacing(24);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #0e1415;");

        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        stepTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
        detailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494;");
        detailLabel.setWrapText(true);
        detailLabel.setMaxWidth(500);

        progressBar.setPrefWidth(350);
        progressBar.setProgress(0);
        progressBar.setStyle("-fx-accent: #22c55e;");
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        actionButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 20; -fx-font-size: 14px; -fx-cursor: hand;");
        actionButton.setOnAction(e -> startFlow());

        skipButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #849494; -fx-font-weight: bold; -fx-cursor: hand;");
        skipButton.setOnAction(e -> {
            if (onCompleteCallback != null) onCompleteCallback.run();
        });

        getChildren().addAll(titleLabel, stepTitleLabel, detailLabel, progressBar, actionButton, skipButton);
        setupSteps();
    }

    private void setupSteps() {
        flow = new AIOnboardingFlow(List.of(
            new AISteps.SystemCheckStep(),
            new AISteps.ProviderSelectStep(),
            new AISteps.ProviderInstallStep(),
            new AISteps.ModelSelectStep(),
            new AISteps.ModelDownloadStep(),
            new AISteps.WarmupStep(),
            new AISteps.CompleteStep()
        ));
    }

    private void startFlow() {
        actionButton.setDisable(true);
        skipButton.setVisible(false);
        skipButton.setManaged(false);
        progressBar.setVisible(true);
        progressBar.setManaged(true);

        Thread thread = new Thread(() -> {
            flow.run(context,
                step -> {
                    Platform.runLater(() -> updateStepSuccess(step));
                },
                step -> {
                    Platform.runLater(() -> updateStepFailure(step));
                }
            );
        }, "bodhak-onboarding-thread");
        thread.setDaemon(true);
        thread.start();

        // Monitor progress via context polling
        Thread progressMonitor = new Thread(() -> {
            while (actionButton.isDisabled()) {
                Double progress = context.get("progress");
                if (progress != null) {
                    Platform.runLater(() -> progressBar.setProgress(progress));
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "bodhak-onboarding-progress-monitor");
        progressMonitor.setDaemon(true);
        progressMonitor.start();
    }

    private void updateStepSuccess(OnboardingStep step) {
        stepTitleLabel.setText("✓ " + step.displayName());
        if ("system-check".equals(step.id())) {
            detailLabel.setText("System check successful. Downloading runtime next.");
        } else if ("provider-install".equals(step.id())) {
            detailLabel.setText("AI provider installed successfully. Preparing recommended model.");
        } else if ("model-download".equals(step.id())) {
            detailLabel.setText("AI model downloaded successfully. Performing warm-up...");
        } else if ("warmup".equals(step.id())) {
            detailLabel.setText("Model loaded and warmed. System is fully operational.");
        } else if ("complete".equals(step.id())) {
            detailLabel.setText("AI Platform setup complete.");
            progressBar.setProgress(1.0);
            actionButton.setDisable(false);
            actionButton.setText("Finish & Proceed");
            actionButton.setOnAction(e -> {
                if (onCompleteCallback != null) onCompleteCallback.run();
            });
        }
    }

    private void updateStepFailure(OnboardingStep step) {
        stepTitleLabel.setText("❌ Setup Failed: " + step.displayName());
        stepTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        String failMsg = step.failureMessage(context);
        detailLabel.setText(failMsg != null ? failMsg : "An error occurred during onboarding.");
        
        actionButton.setDisable(false);
        actionButton.setText("Retry Step");
        actionButton.setOnAction(e -> {
            stepTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            startFlow();
        });
        skipButton.setVisible(true);
        skipButton.setManaged(true);
    }
}
