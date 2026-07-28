package com.example.anuviya.ui.startup;

import com.example.anuviya.analyzer.ai.platform.AIPlatform;
import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;
import com.example.anuviya.analyzer.ai.platform.provider.ProviderRegistry;
import com.example.anuviya.platform.discovery.DiscoveryService;
import com.example.anuviya.platform.registry.RegistryManager;
import com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus;
import com.example.anuviya.platform.environment.SystemEnvironmentManager;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StartupOverlay extends StackPane {

    private final VBox listContainer = new VBox(14);
    private final HBox progressContainer = new HBox(6);
    private final Label dynamicStatusLabel = new Label("Building dependency graph services...");
    private final List<BootStepItem> stepItems = new ArrayList<>();
    private final StackPane parentStack;

    // Platform Info Grids
    private final Label platformVal = new Label("Detecting...");
    private final Label compilerVal = new Label("v1.2.0");
    private final Label compilerStatus = new Label("OFFLINE");
    private final Label aiRuntimeVal = new Label("Detecting...");
    private final Label aiRuntimeStatus = new Label("OFFLINE");
    private final Label modelsVal = new Label("None");
    private final Label workspaceStatus = new Label("INITIALIZING");
    private final Circle workspacePulse = new Circle(4, Color.web("#FF4B4B"));

    private static class BootStepItem extends HBox {
        final Label iconLabel = new Label("○");
        final Label textLabel;
        final Label statusLabel = new Label("");

        BootStepItem(String text) {
            super(14);
            setAlignment(Pos.CENTER_LEFT);
            textLabel = new Label(text);

            iconLabel.setPrefWidth(20);
            iconLabel.setAlignment(Pos.CENTER);
            
            iconLabel.getStyleClass().add("boot-icon-pending");
            textLabel.getStyleClass().add("boot-text-pending");
            statusLabel.getStyleClass().add("boot-status-pending");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            getChildren().addAll(iconLabel, textLabel, spacer, statusLabel);
        }

        void markRunning() {
            Platform.runLater(() -> {
                iconLabel.setText("⟳");
                iconLabel.getStyleClass().clear();
                iconLabel.getStyleClass().addAll("label", "boot-icon-running");
                
                textLabel.getStyleClass().clear();
                textLabel.getStyleClass().addAll("label", "boot-text-running");
                
                statusLabel.setText("RUNNING");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().addAll("label", "boot-status-running");
                
                // Add spin animation
                RotateTransition rt = new RotateTransition(Duration.millis(1000), iconLabel);
                rt.setByAngle(360);
                rt.setCycleCount(Animation.INDEFINITE);
                rt.setInterpolator(Interpolator.LINEAR);
                rt.play();
                
                // Active pulsing text glow simulation
                FadeTransition ft = new FadeTransition(Duration.millis(800), textLabel);
                ft.setFromValue(0.6);
                ft.setToValue(1.0);
                ft.setCycleCount(Animation.INDEFINITE);
                ft.setAutoReverse(true);
                ft.play();
            });
        }

        void markCompleted() {
            Platform.runLater(() -> {
                iconLabel.setText("✓");
                iconLabel.setRotate(0);
                iconLabel.getStyleClass().clear();
                iconLabel.getStyleClass().addAll("label", "boot-icon-completed");
                
                textLabel.getStyleClass().clear();
                textLabel.getStyleClass().addAll("label", "boot-text-completed");
                
                statusLabel.setText("DONE");
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().addAll("label", "boot-status-completed");
                
                // Subtle scale pop on check
                ScaleTransition st = new ScaleTransition(Duration.millis(200), iconLabel);
                st.setFromX(0.7);
                st.setFromY(0.7);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        }
    }

    private final Runnable onComplete;

    public StartupOverlay(StackPane parentStack, Runnable onComplete) {
        this.parentStack = parentStack;
        this.onComplete = onComplete;
        
        // Load stylesheet resource
        String stylesheetPath = getClass().getResource("/styles/startup.css").toExternalForm();
        this.getStylesheets().add(stylesheetPath);
        
        getStyleClass().add("startup-overlay");

        // Translucent cyber panel container
        VBox cardContainer = new VBox(20);
        cardContainer.setAlignment(Pos.TOP_CENTER);
        cardContainer.setMaxWidth(520);
        cardContainer.setMinWidth(520);
        cardContainer.setPadding(new Insets(40));
        
        cardContainer.getStyleClass().add("startup-card");

        // Header: Brand Title & Subtitle
        Label brandTitle = new Label("ANUVIYA");
        brandTitle.getStyleClass().add("brand-title");
        
        Label brandSub = new Label("Understand Code, Build better");
        brandSub.getStyleClass().add("brand-subtitle");
        
        VBox headerBox = new VBox(6);
        headerBox.setAlignment(Pos.CENTER);
        
        javafx.scene.image.ImageView startupIcon = com.example.anuviya.ui.helper.IconHelper.createLogoImageView(48);
        if (startupIcon != null) {
            headerBox.getChildren().add(startupIcon);
        }
        headerBox.getChildren().addAll(brandTitle, brandSub);

        // Segmented Progress Bar Container
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setPrefHeight(3);
        progressContainer.setMaxWidth(Double.MAX_VALUE);
        for (int i = 0; i < 6; i++) {
            Region seg = new Region();
            seg.setPrefHeight(3);
            HBox.setHgrow(seg, Priority.ALWAYS);
            seg.getStyleClass().add("progress-segment-inactive");
            progressContainer.getChildren().add(seg);
        }

        // Timeline Step Items
        String[] steps = {
            "Initializing Compiler Kernel",
            "Loading Registry System",
            "Building Analysis Pipeline",
            "Preparing Platform Services",
            "Discovering AI Runtime",
            "Finalizing Workspace"
        };

        listContainer.setPadding(new Insets(10, 10, 10, 10));
        for (String step : steps) {
            BootStepItem item = new BootStepItem(step);
            stepItems.add(item);
            listContainer.getChildren().add(item);
        }

        // Dynamic Status Terminal Label
        dynamicStatusLabel.getStyleClass().add("terminal-status");
        
        // Parallax and fade animations for dynamic status transitions
        FadeTransition statusFade = new FadeTransition(Duration.millis(1500), dynamicStatusLabel);
        statusFade.setFromValue(0.5);
        statusFade.setToValue(1.0);
        statusFade.setCycleCount(Animation.INDEFINITE);
        statusFade.setAutoReverse(true);
        statusFade.play();

        // Footer Metadata Info Grid (2x2)
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(12);
        infoGrid.setVgap(12);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        infoGrid.getColumnConstraints().addAll(col1, col2);

        // Panel blocks builder
        Pane platformPane = buildInfoBlock("PLATFORM", platformVal, null);
        Pane compilerPane = buildInfoBlock("COMPILER", compilerVal, compilerStatus);
        Pane runtimePane = buildInfoBlock("AI RUNTIME", aiRuntimeVal, aiRuntimeStatus);
        Pane modelsPane = buildInfoBlock("MODELS", modelsVal, null);

        infoGrid.add(platformPane, 0, 0);
        infoGrid.add(compilerPane, 1, 0);
        infoGrid.add(runtimePane, 0, 1);
        infoGrid.add(modelsPane, 1, 1);

        // Workspace Status Wide Panel
        HBox statusPanel = new HBox(10);
        statusPanel.setAlignment(Pos.CENTER_LEFT);
        statusPanel.setPadding(new Insets(12));
        statusPanel.getStyleClass().add("status-panel");

        Label wsLabel = new Label("WORKSPACE STATUS");
        wsLabel.getStyleClass().add("status-panel-label");

        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);

        workspaceStatus.getStyleClass().add("status-panel-val-initializing");
        
        statusPanel.getChildren().addAll(workspacePulse, wsLabel, statusSpacer, workspaceStatus);

        // Version decorators at the absolute bottom
        HBox decorators = new HBox();
        decorators.setPadding(new Insets(10, 10, 0, 10));
        
        Label osBuildLbl = new Label("OS_BUILD: 22631.3296");
        osBuildLbl.getStyleClass().add("decorator-label");
        
        Region decSpacer = new Region();
        HBox.setHgrow(decSpacer, Priority.ALWAYS);
        
        Label authUserLbl = new Label("AUTH_USER: SYSTEM_ADMIN");
        authUserLbl.getStyleClass().add("decorator-label");
        
        decorators.getChildren().addAll(osBuildLbl, decSpacer, authUserLbl);

        cardContainer.getChildren().addAll(headerBox, progressContainer, listContainer, dynamicStatusLabel, infoGrid, statusPanel, decorators);
        
        getChildren().add(cardContainer);
        setAlignment(cardContainer, Pos.CENTER);
    }

    public void startSequence() {
        Task<Void> bootTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Discover OS environment details
                Platform.runLater(() -> {
                    String osName = System.getProperty("os.name");
                    if (osName.toLowerCase().contains("win")) platformVal.setText("Windows");
                    else if (osName.toLowerCase().contains("mac")) platformVal.setText("macOS");
                    else platformVal.setText("Linux");
                });

                // Step 1: Initializing Workspace and Compiler Kernel
                updateStep(0, "Initializing workspace system...", 1);
                com.example.anuviya.workspace.WorkspaceKernel.getInstance().initialize();
                Thread.sleep(650);

                // Step 2: Loading Registry System
                updateStep(1, "Loading registry packages catalog...", 2);
                RegistryManager.getInstance().loadAll();
                Platform.runLater(() -> {
                    compilerStatus.setText("READY");
                    compilerStatus.getStyleClass().clear();
                    compilerStatus.getStyleClass().addAll("label", "info-block-status-ready");
                });
                Thread.sleep(550);

                // Step 3: Building Analysis Pipeline
                updateStep(2, "Building incremental dependency graph...", 3);
                Thread.sleep(600);

                // Step 4: Preparing Platform Services
                updateStep(3, "Initializing live telemetry scanning daemon...", 4);
                SystemEnvironmentManager.getInstance().startMonitoring();
                ProviderRegistry.getInstance().discover();
                Thread.sleep(500);

                // Step 5: Discovering AI Runtime
                updateStep(4, "Scanning local machine for active AI runtimes...", 5);
                
                // Auto start provider if configured
                boolean autoStart = getAutoStartPreference();
                AIProvider activeProvider = ProviderRegistry.getInstance().getActive();
                if (autoStart && activeProvider != null) {
                    if (activeProvider.runtimeStatus() == RuntimeStatus.OFFLINE) {
                        activeProvider.start();
                    }
                }
                
                List<AIProvider> providers = DiscoveryService.getInstance().discoverInstalledProviders();
                AIProvider preferred = null;
                if (!providers.isEmpty()) {
                    preferred = providers.get(0);
                }
                
                final AIProvider finalPreferred = preferred;
                Platform.runLater(() -> {
                    if (finalPreferred != null) {
                        aiRuntimeVal.setText(finalPreferred.info().displayName());
                        aiRuntimeStatus.setText("READY");
                        aiRuntimeStatus.getStyleClass().clear();
                        aiRuntimeStatus.getStyleClass().addAll("label", "info-block-status-ready");
                    } else {
                        aiRuntimeVal.setText("None");
                        aiRuntimeStatus.setText("OFFLINE");
                        aiRuntimeStatus.getStyleClass().clear();
                        aiRuntimeStatus.getStyleClass().addAll("label", "info-block-status-offline");
                    }
                });
                Thread.sleep(650);

                // Step 6: Finalizing Workspace
                updateStep(5, "Resolving active package models...", 6);
                if (preferred != null) {
                    List<ModelInfo> models = DiscoveryService.getInstance().discoverInstalledModels(preferred);
                    Platform.runLater(() -> {
                        if (!models.isEmpty()) {
                            modelsVal.setText(models.get(0).displayName());
                        } else {
                            modelsVal.setText("None");
                        }
                    });
                }
                
                // Let AIPlatform rebuild state with initial facts
                AIPlatform.getInstance().rebuildState();
                
                Platform.runLater(() -> {
                    workspacePulse.setFill(Color.web("#22c55e"));
                    workspaceStatus.setText("READY");
                    workspaceStatus.getStyleClass().clear();
                    workspaceStatus.getStyleClass().addAll("label", "status-panel-val-ready");
                });
                Thread.sleep(500);

                // Complete
                Platform.runLater(() -> {
                    stepItems.get(5).markCompleted();
                    dynamicStatusLabel.setText("System ready.");
                });
                Thread.sleep(400);

                return null;
            }
        };

        bootTask.setOnSucceeded(e -> {
            // Smooth fade-out transition
            FadeTransition ft = new FadeTransition(Duration.millis(500), this);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(evt -> {
                parentStack.getChildren().remove(this);
                if (onComplete != null) {
                    onComplete.run();
                }
            });
            ft.play();
        });

        new Thread(bootTask, "Bodhak-Boot-Thread").start();
    }

    private void updateStep(int idx, String status, int activeSegments) {
        if (idx > 0) {
            stepItems.get(idx - 1).markCompleted();
        }
        stepItems.get(idx).markRunning();
        Platform.runLater(() -> {
            dynamicStatusLabel.setText(status);
            // Highlight complete segments
            for (int i = 0; i < progressContainer.getChildren().size(); i++) {
                Region seg = (Region) progressContainer.getChildren().get(i);
                seg.getStyleClass().clear();
                if (i < activeSegments) {
                    seg.getStyleClass().add("progress-segment-active");
                } else {
                    seg.getStyleClass().add("progress-segment-inactive");
                }
            }
        });
    }

    private Pane buildInfoBlock(String label, Label valueLabel, Label statusLabel) {
        VBox block = new VBox(4);
        block.setPadding(new Insets(10, 12, 10, 12));
        
        block.getStyleClass().add("info-block");

        Label name = new Label(label);
        name.getStyleClass().add("info-block-label");

        valueLabel.getStyleClass().add("info-block-val");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().add(name);

        if (statusLabel != null) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            statusLabel.getStyleClass().add("info-block-status-offline");
            topRow.getChildren().addAll(spacer, statusLabel);
        }

        block.getChildren().addAll(topRow, valueLabel);
        return block;
    }

    private boolean getAutoStartPreference() {
        String userHome = System.getProperty("user.home");
        File config = new File(userHome, ".gemini/antigravity/onboarding.properties");
        if (config.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(config)) {
                props.load(fis);
                String val = props.getProperty("ai.provider.autoStart");
                if (val != null) {
                    return Boolean.parseBoolean(val);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return true;
    }
}
