package com.example.bodhak.ui.nav.workspace.impl;

import com.example.bodhak.analyzer.ai.event.AnalysisEvent;
import com.example.bodhak.analyzer.ai.event.AnalysisEventType;
import com.example.bodhak.analyzer.ai.analysis.AnalysisType;
import com.example.bodhak.analyzer.ai.service.AiAnalysisService;
import com.example.bodhak.analyzer.ai.service.AnalysisServiceRegistry;
import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.orchestration.AnalysisEngine;
import com.example.bodhak.ui.nav.workspace.Workspace;

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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefectWorkspace implements Workspace {
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
        public List<Defect> defects;
    }

    public static class Defect {
        public String defectType;
        public List<String> responsibleEntities;
        public String severity;
        public String explanation;
        public List<Object> possibleFixes;
    }

    public DefectWorkspace() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0e1415;");

        // Model selector
        modelSelector = new ComboBox<>();
        modelSelector.getItems().addAll("qwen2.5-coder:1.5b", "qwen2.5:3b");
        modelSelector.getSelectionModel().selectFirst();
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
            InvestigationPane pane = new InvestigationPane();
            pane.setVisible(false);
            pane.setManaged(false);
            panes.put(type, pane);
            contentStack.getChildren().add(pane);
        }

        contentHolder.getChildren().add(contentStack);

        switchView(emptyStateView);

        root.setLeft(buildSidebar());
        root.setCenter(contentHolder);
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
            if (activePane.hasRun()) {
                switchView(activePane);
            } else {
                switchView(emptyStateView);
            }
        }
    }

    // ── Layout construction ──────────────────────────────────────────────────

    private Node buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);
        sidebar.setStyle("-fx-background-color: #111819; -fx-border-color: transparent #1e2526 transparent transparent; -fx-border-width: 0 1 0 0;");

        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 20, 20, 20));
        Label title = new Label("AI Analysis");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        Label sub = new Label("Select analysis type");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #566465;");
        header.getChildren().addAll(title, sub);
        sidebar.getChildren().addAll(header, divider());

        VBox typeList = new VBox(2);
        typeList.setPadding(new Insets(12, 10, 12, 10));
        String[] icons = {"🏛", "⚡", "📈", "🔒", "🔧"};
        boolean[] isPlaceholder = {false, false, false, true, true};

        for (int i = 0; i < ANALYSIS_TYPES.length; i++) {
            String type = ANALYSIS_TYPES[i];
            String icon = icons[i];
            boolean stub = isPlaceholder[i];
            ToggleButton btn = buildAnalysisTypeButton(icon + "  " + type, stub);
            if (i == 0) btn.setSelected(true);
            btn.setOnAction(e -> {
                selectedAnalysisType = mapType(type);
                showPane(selectedAnalysisType);
            });
            typeList.getChildren().add(btn);
        }
        sidebar.getChildren().addAll(typeList, divider());

        VBox config = new VBox(14);
        config.setPadding(new Insets(16, 16, 16, 16));
        config.getChildren().addAll(labelledControl("Model", modelSelector), labelledControl("Scope", scopeSelector));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox runBox = new VBox();
        runBox.setPadding(new Insets(12, 16, 24, 16));
        runBox.getChildren().add(runControlContainer);

        sidebar.getChildren().addAll(config, spacer, runBox);
        return sidebar;
    }

    private ToggleButton buildAnalysisTypeButton(String text, boolean isPlaceholder) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(analysisToggle);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        String baseStyle = "-fx-background-color: transparent; -fx-text-fill: %s; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
        String textColor = isPlaceholder ? "#566465" : "#849494";
        btn.setStyle(baseStyle.formatted(textColor));

        if (isPlaceholder) {
            btn.setText(text + "  ·  soon");
            btn.setDisable(true);
        }

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle("-fx-background-color: rgba(75,246,255,0.08); -fx-text-fill: #4bf6ff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent transparent transparent #4bf6ff; -fx-border-width: 0 0 0 2;");
            } else {
                btn.setStyle(baseStyle.formatted(isPlaceholder ? "#566465" : "#849494"));
            }
        });
        return btn;
    }

    private Node labelledControl(String labelText, Node control) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText.toUpperCase());
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #566465; -fx-font-weight: bold; -fx-letter-spacing: 0.5;");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    // ── Persistent Views Construction ─────────────────────────────────────────

    private VBox buildEmptyStateView() {
        VBox empty = new VBox(20);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80, 40, 80, 40));

        Label hexIcon = new Label("⬡");
        hexIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #1e2a2b;");

        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, e -> hexIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #1e2a2b;")),
            new KeyFrame(Duration.millis(1200), e -> hexIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #2a3a3c;"))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        Label heading = new Label("Bodhak AI");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label subheading = new Label("Choose an analysis type and run an investigation.");
        subheading.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494; -fx-line-spacing: 5px;");
        subheading.setTextAlignment(TextAlignment.CENTER);

        empty.getChildren().addAll(hexIcon, heading, subheading);
        return empty;
    }

    private void handleRunAnalysis() {
        if (lastEngine == null) return;

        InvestigationPane activePane = panes.get(selectedAnalysisType);
        if (activePane == null) return;

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

        task.setOnSucceeded(e -> {
            String findings = task.getValue();
            Platform.runLater(() -> {
                activePane.finalizeAnalysis(findings, selectedAnalysisType);
                resetRunButton();
            });
        });

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
            String error = "Analysis failed: " + (ex != null && ex.getMessage() != null ? ex.getMessage() : (ex != null ? ex.getClass().getSimpleName() : "Unknown error"));
            activePane.handleAnalysisFailure(error);
            resetRunButton();
        }));

        new Thread(task, "Bodhak-AI-Analysis-Thread").start();
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
