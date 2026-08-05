package com.example.anuviya.ui.nav.workspace.impl;

import com.example.anuviya.analyzer.ai.analysis.AnalysisType;
import com.example.anuviya.analyzer.ai.service.AiAnalysisService;
import com.example.anuviya.analyzer.ai.service.AnalysisServiceRegistry;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.ui.nav.workspace.Workspace;
import com.example.anuviya.analyzer.ai.ui.NoProviderPane;
import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.environment.SystemEnvironmentManager;
import com.example.anuviya.platform.environment.model.MemorySnapshot;
import com.example.anuviya.platform.registry.domain.PackageRegistry;
import com.example.anuviya.platform.state.PlatformState;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Workspace for the DEFECTS tab.
 * Renamed from DefectWorkspace for IDE WorkspaceView architectural alignment.
 */
public class DefectView implements Workspace {
    private final AnalysisServiceRegistry registry = new AnalysisServiceRegistry();

    private static final String[] ANALYSIS_TYPES = {
            "Architecture Analysis",
            "Performance Analysis",
            "Scalability Analysis",
            "Security Analysis",
            "Maintainability Analysis"
    };

    private final BorderPane root;

    // Sidebar controls
    private final ToggleGroup analysisToggle = new ToggleGroup();
    private final ComboBox<String> modelSelector;
    private final ComboBox<String> scopeSelector;
    private final Button runButton;

    // Content area
    private final StackPane contentHolder;
    
    // Persistent Views
    private final VBox emptyStateView;
    private final StackPane contentStack = new StackPane();
    private final Map<AnalysisType, InvestigationPane> panes = new java.util.EnumMap<>(AnalysisType.class);

    // State
    private AnalysisEngine lastEngine;
    private String lastFindings;
    private AnalysisType selectedAnalysisType = AnalysisType.ARCHITECTURE;
    private Task<String> activeAnalysisTask;
    private final HBox runControlContainer = new HBox(8);

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // JSON DTOs
    public static class AnalysisResult {
        public String overallAssessment;
        public List<Defect> defects;
    }

    public static class Defect {
        public String defectType;
        public List<String> responsibleEntities;
        public String severity;
        public String explanation;
        public List<Object> possibleFixes;
    }

    public DefectView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0e1415;");

        // Model selector
        modelSelector = new ComboBox<>();
        styleComboBox(modelSelector);

        // Scope selector
        scopeSelector = new ComboBox<>();
        scopeSelector.getItems().addAll("Full Project", "Entry Points Only", "Critical Paths Only");
        scopeSelector.getSelectionModel().selectFirst();
        styleComboBox(scopeSelector);

        // Run button
        runButton = new Button("▶  Run Analysis");
        runButton.setMaxWidth(Double.MAX_VALUE);
        runButton.setStyle(buildRunButtonStyle(false));
        runButton.setOnMouseEntered(e -> {
            if (!runButton.isDisabled()) runButton.setStyle(buildRunButtonHoverStyle());
        });
        runButton.setOnMouseExited(e -> {
            if (!runButton.isDisabled()) runButton.setStyle(buildRunButtonStyle(false));
        });
        runButton.setOnAction(e -> handleRunAnalysis());
        runControlContainer.getChildren().add(runButton);

        // Content area setup
        contentHolder = new StackPane();
        contentHolder.setStyle("-fx-background-color: #0e1415;");

        emptyStateView = buildEmptyStateView();
        contentStack.getChildren().add(emptyStateView);

        for (AnalysisType type : AnalysisType.values()) {
            InvestigationPane pane = new InvestigationPane(type);
            pane.setVisible(false);
            pane.setManaged(false);
            panes.put(type, pane);
            contentStack.getChildren().add(pane);
        }

        contentHolder.getChildren().add(contentStack);
        switchView(emptyStateView);

        root.setLeft(buildSidebar());
        
        // Listen to platform state changes to update model dropdown and onboarding state
        PlatformState.currentProperty().addListener((obs, oldState, newState) -> {
            if (newState != null) {
                Platform.runLater(() -> {
                    rebuildModelSelector(newState);
                    checkAndShowOnboarding();
                });
            }
        });
        if (PlatformState.getCurrent() != null) {
            rebuildModelSelector(PlatformState.getCurrent());
        }

        // Check and show onboarding or content view
        checkAndShowOnboarding();
    }

    private void checkAndShowOnboarding() {
        if (isPlatformReady()) {
            root.setCenter(contentHolder);
        } else {
            // Render a beautiful, simple fallback pane instead of AIOnboardingFlowView
            VBox fallback = new VBox(20);
            fallback.setAlignment(Pos.CENTER);
            fallback.setStyle("-fx-background-color: #0e1415;");

            Label icon = new Label("⚪");
            icon.setStyle("-fx-font-size: 64px; -fx-text-fill: #374151;");

            Label title = new Label("AI Platform Not Configured");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

            Label desc = new Label("AI-powered defect analysis requires an active provider runtime (e.g. Ollama) and at least one installed model. Configure these in settings to get started.");
            desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #849494; -fx-line-spacing: 5px;");
            desc.setTextAlignment(TextAlignment.CENTER);
            desc.setMaxWidth(480);

            Button settingsBtn = new Button("⚙  Configure AI Settings");
            settingsBtn.setStyle("-fx-background-color: linear-gradient(to right, #4bf6ff, #8bfd91); -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 10 18; -fx-cursor: hand;");
            settingsBtn.setOnAction(e -> {
                com.example.anuviya.ui.settings.SettingsDialog.show();
                Platform.runLater(this::checkAndShowOnboarding);
            });

            fallback.getChildren().addAll(icon, title, desc, settingsBtn);
            root.setCenter(fallback);
        }
    }

    private boolean isPlatformReady() {
        PlatformState state = PlatformState.getCurrent();
        if (state == null) return false;
        
        // Check if any provider exists
        boolean hasProvider = state.getPreferredProvider() != null;
        // Check if at least one model is installed
        boolean hasModels = !state.getInstalledModels().isEmpty();
        
        return hasProvider && hasModels;
    }

    private void rebuildModelSelector(PlatformState state) {
        List<ModelInfo> models = state.getInstalledModels();
        modelSelector.getItems().clear();
        for (ModelInfo model : models) {
            modelSelector.getItems().add(model.displayName());
        }
        if (!modelSelector.getItems().isEmpty()) {
            modelSelector.getSelectionModel().selectFirst();
        } else {
            modelSelector.getItems().add("No models installed");
            modelSelector.getSelectionModel().selectFirst();
        }
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        if (engine == lastEngine) return;
        lastEngine = engine;

        if (engine == null) {
            lastFindings = null;
            switchView(emptyStateView);
            runButton.setDisable(true);
        } else {
            runButton.setDisable(false);
            if (lastFindings != null) {
                lastFindings = null;
                switchView(emptyStateView);
            }
        }
    }

    private void switchView(Node targetView) {
        for (Node child : contentStack.getChildren()) {
            if (child == targetView) {
                child.setVisible(true);
                child.setManaged(true);
                
                FadeTransition ft = new FadeTransition(Duration.millis(400), child);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            } else {
                child.setVisible(false);
                child.setManaged(false);
            }
        }
    }

    private void showPane(AnalysisType type) {
        InvestigationPane activePane = panes.get(type);
        if (activePane != null) {
            switchView(activePane);
        }
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(24));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #0c1011; -fx-border-color: transparent #1a2122 transparent transparent; -fx-border-width: 1;");

        Label title = new Label("INVESTIGATIONS");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #566465; -fx-letter-spacing: 1px;");

        VBox typeBox = new VBox(8);
        for (String typeLabel : ANALYSIS_TYPES) {
            ToggleButton btn = new ToggleButton(typeLabel.toUpperCase());
            btn.setToggleGroup(analysisToggle);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            
            boolean isEnabled = typeLabel.equals("Architecture Analysis") || typeLabel.equals("Scalability Analysis");
            if (isEnabled) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #849494; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 10 14; -fx-background-radius: 6; -fx-cursor: hand;");
            } else {
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #343f40; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 10 14; -fx-background-radius: 6; -fx-cursor: default;");
            }
            
            btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    btn.setStyle("-fx-background-color: #1a2122; -fx-text-fill: #4bf6ff; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 10 14; -fx-background-radius: 6;");
                    selectedAnalysisType = mapType(typeLabel);
                    showPane(selectedAnalysisType);
                } else {
                    if (isEnabled) {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #849494; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 10 14; -fx-background-radius: 6; -fx-cursor: hand;");
                    } else {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #343f40; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 10 14; -fx-background-radius: 6; -fx-cursor: default;");
                    }
                }
            });
            typeBox.getChildren().add(btn);
        }
        
        // Select first analysis type by default
        if (!typeBox.getChildren().isEmpty()) {
            ((ToggleButton) typeBox.getChildren().get(0)).setSelected(true);
        }

        VBox configBox = new VBox(12);
        Label configTitle = new Label("RUN CONFIGURATION");
        configTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #566465; -fx-letter-spacing: 1px;");

        VBox modelBox = new VBox(6);
        Label modelLbl = new Label("AI Model");
        modelLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #849494;");
        modelBox.getChildren().addAll(modelLbl, modelSelector);

        VBox scopeBox = new VBox(6);
        Label scopeLbl = new Label("Analysis Scope");
        scopeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #849494;");
        scopeBox.getChildren().addAll(scopeLbl, scopeSelector);

        configBox.getChildren().addAll(configTitle, modelBox, scopeBox);

        sidebar.getChildren().addAll(title, typeBox, divider(), configBox, divider(), runControlContainer);
        return sidebar;
    }

    private VBox buildEmptyStateView() {
        VBox empty = new VBox(20);
        empty.setAlignment(Pos.CENTER);
        empty.setStyle("-fx-background-color: #0e1415;");

        // SVG Hexagon Icon
        Label hexIcon = new Label("⬡");
        hexIcon.setStyle("-fx-font-size: 80px; -fx-text-fill: #1c2a2b;");
        
        // Subtle pulse animation
        ScaleTransition pulse = new ScaleTransition(Duration.millis(2000), hexIcon);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        Label heading = new Label("Anuviya AI");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label subheading = new Label("Choose an analysis type and run an investigation.");
        subheading.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494; -fx-line-spacing: 5px;");
        subheading.setTextAlignment(TextAlignment.CENTER);

        empty.getChildren().addAll(hexIcon, heading, subheading);
        return empty;
    }

    private String resolveModelId(String displayName) {
        if (displayName == null) return "qwen2.5-3b";
        return PackageRegistry.getInstance().all().stream()
            .filter(p -> p.displayName().equalsIgnoreCase(displayName) || p.id().equalsIgnoreCase(displayName))
            .map(ServicePackage::id)
            .findFirst()
            .orElseGet(() -> {
                String lower = displayName.toLowerCase();
                if (lower.contains("1.5b")) return "qwen2.5-1.5b";
                if (lower.contains("llama")) return "llama3.2-3b";
                if (lower.contains("phi")) return "phi4-mini";
                return "qwen2.5-3b";
            });
    }

    private void handleRunAnalysis() {
        if (lastEngine == null) return;

        InvestigationPane activePane = panes.get(selectedAnalysisType);
        if (activePane == null) return;

        String modelId = resolveModelId(modelSelector.getValue());
        MemorySnapshot mem = SystemEnvironmentManager.getInstance().readCurrentMemory(modelId);

        if (mem.isLowMemory()) {
            showLowMemoryDialog(mem, modelId, activePane);
            return;
        }

        startAnalysisTask(activePane);
    }

    private void showLowMemoryDialog(MemorySnapshot mem, String modelId, InvestigationPane activePane) {
        ServicePackage pkg = PackageRegistry.getInstance().get(modelId).orElse(null);
        Runnable proceed = mem.isCriticallyLow() ? null : () -> {
            startAnalysisTask(activePane);
        };

        Pane overlayTarget = null;
        if (root.getScene() != null && root.getScene().getRoot() instanceof Pane sceneRoot) {
            overlayTarget = sceneRoot;
        } else {
            overlayTarget = contentHolder;
        }

        LowMemoryAlertDialog dialog = new LowMemoryAlertDialog(
            mem, pkg, () -> {}, proceed, overlayTarget
        );
        dialog.show();
    }

    private void startAnalysisTask(InvestigationPane activePane) {
        runButton.setDisable(true);
        showRunningState();
        
        activePane.prepareForAnalysis(modelSelector.getValue());
        switchView(activePane);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                AnalysisContext ctx = lastEngine.getAnalysisContextManager().getCurrentContext();
                String model = modelSelector.getValue();
                AiAnalysisService service = registry.get(selectedAnalysisType);

                if (service == null) {
                    throw new IllegalStateException("Analysis type not implemented: " + selectedAnalysisType);
                }

                return service.analyze(ctx, model, activePane::addActivityEvent);
            }
        };

        activeAnalysisTask = task;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> ramWatcher = executor.scheduleAtFixedRate(() -> {
            String modelId = resolveModelId(modelSelector.getValue());
            MemorySnapshot current = SystemEnvironmentManager.getInstance().readCurrentMemory(modelId);
            if (current.isCriticallyLow() && task.isRunning()) {
                task.cancel(true);
                Platform.runLater(() -> {
                    activePane.handleAnalysisFailure(
                        "Analysis stopped: system memory critically low (" +
                        current.freePhysicalMb() + " MB free). Close other applications to free up RAM and retry."
                    );
                    resetRunButton();
                });
            }
        }, 10, 10, TimeUnit.SECONDS);

        task.setOnSucceeded(e -> {
            ramWatcher.cancel(false);
            executor.shutdown();
            String findings = task.getValue();
            Platform.runLater(() -> {
                activePane.finalizeAnalysis(findings, selectedAnalysisType);
                resetRunButton();

                var currentProj = com.example.anuviya.workspace.WorkspaceManager.getInstance().getCurrentProject();
                if (currentProj != null) {
                    UUID sessionId = UUID.randomUUID();
                    File sessionReportsDir = new File(
                        com.example.anuviya.workspace.WorkspaceKernel.getInstance().getWorkspaceRoot(),
                        "projects/" + currentProj.id().toString() + "/sessions/" + sessionId.toString() + "/reports"
                    );
                    if (!sessionReportsDir.exists()) {
                        sessionReportsDir.mkdirs();
                    }
                    File reportFile = new File(sessionReportsDir, selectedAnalysisType.toString().toLowerCase() + ".json");
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, String> reportData = new HashMap<>();
                        reportData.put("findings", findings);
                        mapper.writeValue(reportFile, reportData);
                    } catch (Exception ex) {
                        System.err.println("[DefectView] Failed to write report file: " + ex.getMessage());
                    }

                    com.example.anuviya.workspace.model.ReportReference reportRef = new com.example.anuviya.workspace.model.ReportReference(
                        selectedAnalysisType.toString(),
                        "reports/" + selectedAnalysisType.toString().toLowerCase() + ".json",
                        0,
                        0,
                        com.example.anuviya.workspace.model.HealthStatus.GOOD
                    );

                    com.example.anuviya.workspace.model.CacheReference cacheRef = new com.example.anuviya.workspace.model.CacheReference(
                        "compiler.cache",
                        "architecture.cache",
                        "scalability.cache",
                        "v2"
                    );

                    com.example.anuviya.workspace.model.AnalysisSession session = new com.example.anuviya.workspace.model.AnalysisSession(
                        sessionId,
                        currentProj.id(),
                        Instant.now(),
                        0,
                        currentProj.compilerVersion(),
                        currentProj.analysisVersion(),
                        "1.0",
                        null,
                        cacheRef,
                        List.of(reportRef),
                        List.of()
                    );

                    com.example.anuviya.workspace.model.ProjectIntelligence intel = new com.example.anuviya.workspace.model.ProjectIntelligence(
                        currentProj.id(),
                        sessionId,
                        selectedAnalysisType.toString().equalsIgnoreCase("ARCHITECTURE") ? com.example.anuviya.workspace.model.HealthStatus.GOOD : com.example.anuviya.workspace.model.HealthStatus.UNKNOWN,
                        selectedAnalysisType.toString().equalsIgnoreCase("SCALABILITY") ? com.example.anuviya.workspace.model.HealthStatus.GOOD : com.example.anuviya.workspace.model.HealthStatus.UNKNOWN,
                        0,
                        0,
                        0,
                        0,
                        0,
                        "Analysis completed successfully.",
                        Instant.now()
                    );

                    com.example.anuviya.workspace.WorkspaceManager.getInstance().recordAnalysisSession(session, intel);
                    com.example.anuviya.workspace.WorkspaceManager.getInstance().writeCacheEntry(currentProj.id(), new File(currentProj.location()), "compiler");
                    com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
                }
            });
        });

        task.setOnFailed(e -> {
            ramWatcher.cancel(false);
            executor.shutdown();
            Platform.runLater(() -> {
                Throwable ex = task.getException();
                if (ex != null) {
                    ex.printStackTrace();
                }
                
                // Check if failure is due to missing AI components or offline providers
                if (ex != null && (ex.getMessage() != null && (ex.getMessage().contains("No active AI provider") || 
                                  ex.getMessage().contains("No installed model") || 
                                  ex.getMessage().contains("offline")))) {
                    
                    String reqModel = (modelSelector != null && modelSelector.getValue() != null && !modelSelector.getValue().startsWith("No "))
                        ? modelSelector.getValue()
                        : "qwen2.5-3b";
                    NoProviderPane noProvider = new NoProviderPane(reqModel, 
                        () -> {
                            // complete callback: retry the analysis
                            Platform.runLater(() -> {
                                contentStack.getChildren().removeIf(node -> node instanceof NoProviderPane);
                                handleRunAnalysis();
                            });
                        },
                        () -> {
                            // cancel callback: reset view to empty state
                            Platform.runLater(() -> {
                                contentStack.getChildren().removeIf(node -> node instanceof NoProviderPane);
                                resetRunButton();
                                switchView(emptyStateView);
                            });
                        }
                    );
                    
                    contentStack.getChildren().add(noProvider);
                    switchView(noProvider);
                } else {
                    String error = "Analysis failed: " + (ex != null ? excMessage(ex) : "Unknown error");
                    activePane.handleAnalysisFailure(error);
                    resetRunButton();
                }
            });
        });

        task.setOnCancelled(e -> {
            ramWatcher.cancel(false);
            executor.shutdown();
        });

        new Thread(task, "Bodhak-AI-Analysis-Thread").start();
    }

    private String excMessage(Throwable ex) {
        if (ex.getMessage() != null) return ex.getMessage();
        return ex.getClass().getSimpleName();
    }

    private void resetRunButton() {
        activeAnalysisTask = null;
        showIdleState();
        runButton.setDisable(lastEngine == null);
        runButton.setText("▶  Run Analysis");
        runButton.setStyle(buildRunButtonStyle(false));
    }

    private void showRunningState() {
        runControlContainer.getChildren().clear();

        Button runningBtn = new Button("⏳ Running...");
        runningBtn.setDisable(true);
        runningBtn.setStyle("-fx-background-color: #2f3637; -fx-text-fill: #849494; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 12; -fx-font-size: 12px;");
        HBox.setHgrow(runningBtn, Priority.ALWAYS);
        runningBtn.setMaxWidth(Double.MAX_VALUE);

        Button stopBtn = new Button("⏹ Stop");
        stopBtn.setStyle("-fx-background-color: #ff4b4b; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 14; -fx-font-size: 12px; -fx-cursor: hand;");
        stopBtn.setOnAction(e -> handleStopAnalysis());

        runControlContainer.getChildren().addAll(runningBtn, stopBtn);
    }

    private void showIdleState() {
        runControlContainer.getChildren().clear();
        runControlContainer.getChildren().add(runButton);
    }

    private void handleStopAnalysis() {
        if (activeAnalysisTask != null && activeAnalysisTask.isRunning()) {
            activeAnalysisTask.cancel(true);
            InvestigationPane activePane = panes.get(selectedAnalysisType);
            if (activePane != null) {
                activePane.stopAnalysis();
            }
            resetRunButton();
        }
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private String buildRunButtonStyle(boolean disabled) {
        return "-fx-background-color: " + (disabled ? "#2f3637" : "linear-gradient(to right, #4bf6ff, #8bfd91)") + "; -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 10 16; -fx-cursor: " + (disabled ? "default" : "hand") + ";";
    }

    private String buildRunButtonHoverStyle() {
        return "-fx-background-color: linear-gradient(to right, #8bfd91, #4bf6ff); -fx-text-fill: #0e1415; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 10 16; -fx-cursor: hand;";
    }

    private void styleComboBox(ComboBox<?> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle("-fx-background-color: #1a2122; -fx-text-fill: #dde4e5; -fx-font-size: 12px; -fx-background-radius: 6; -fx-border-color: #242b2c; -fx-border-radius: 6; -fx-border-width: 1;");
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: #1a2122;");
        return r;
    }

    private AnalysisType mapType(String label) {
        return switch (label) {
            case "Architecture Analysis" -> AnalysisType.ARCHITECTURE;
            case "Scalability Analysis" -> AnalysisType.SCALABILITY;
            case "Performance Analysis" -> AnalysisType.PERFORMANCE;
            case "Maintainability Analysis" -> AnalysisType.MAINTAINABILITY;
            default -> AnalysisType.ARCHITECTURE;
        };
    }
}
