package com.example.bodhakfrontend.ui.nav.workspace.impl;

import com.example.bodhakfrontend.ai.event.AnalysisEvent;
import com.example.bodhakfrontend.ai.event.AnalysisEventType;
import com.example.bodhakfrontend.ai.model.AnalysisType;
import com.example.bodhakfrontend.ai.service.AiAnalysisService;
import com.example.bodhakfrontend.ai.service.AnalysisServiceRegistry;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.ui.nav.workspace.Workspace;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.List;
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

    // for making the screen scroll to Result head
    private Label resultsAnchor;

    // Sidebar controls
    private final ToggleGroup analysisToggle = new ToggleGroup();
    private final ComboBox<String> modelSelector;
    private final ComboBox<String> scopeSelector;
    private final Button runButton;

    // Content area
    private final StackPane contentHolder;
    
    // Persistent Views
    private final VBox emptyStateView;
    private final ScrollPane investigationSessionView;
    
    // UI elements inside investigationSessionView
    private final VBox sessionContainer;
    private final VBox timelineFeed;
    private final VBox resultsDashboardView;
    
    // Real-Time Status Footer
    private final HBox statusFooter;
    private final Label statusLabel;
    
    // Typing state for model inference
    private TypingMessageBubble currentModelBubble;

    // Event Queue for pacing
    private final Queue<AnalysisEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private Timeline eventPoller;

    // State
    private AnalysisEngine lastEngine;
    private String lastFindings;
    private AnalysisType selectedAnalysisType = AnalysisType.ARCHITECTURE;

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
        public List<String> possibleFixes;
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

        // Content area setup
        contentHolder = new StackPane();
        contentHolder.setStyle("-fx-background-color: #0e1415;");

        emptyStateView = buildEmptyStateView();
        
        timelineFeed = new VBox(20);
        timelineFeed.setMaxWidth(800);
        timelineFeed.setAlignment(Pos.TOP_LEFT);
        
        statusFooter = new HBox(8);
        statusFooter.setAlignment(Pos.CENTER_LEFT);
        statusFooter.setPadding(new Insets(10, 0, 10, 40));
        statusLabel = new Label("● Initializing analysis...");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #566465; -fx-font-style: italic;");
        statusFooter.getChildren().add(statusLabel);
        
        resultsDashboardView = new VBox(24);
        resultsDashboardView.setMaxWidth(1000);
        resultsDashboardView.setAlignment(Pos.TOP_LEFT);
        resultsDashboardView.setVisible(false);
        resultsDashboardView.setManaged(false);

        sessionContainer = new VBox(20);
        sessionContainer.setPadding(new Insets(40, 80, 80, 80));
        sessionContainer.setAlignment(Pos.TOP_CENTER);
        sessionContainer.getChildren().addAll(timelineFeed, statusFooter, resultsDashboardView);
        
        investigationSessionView = new ScrollPane(sessionContainer);
        investigationSessionView.setFitToWidth(true);
        investigationSessionView.setStyle("-fx-background-color: transparent; -fx-background: #0e1415;");
        investigationSessionView.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        contentHolder.getChildren().addAll(emptyStateView, investigationSessionView);
        
        // Setup event poller
        eventPoller = new Timeline(new KeyFrame(Duration.millis(600), e -> {
            AnalysisEvent event = eventQueue.poll();
            if (event != null) {
                processEvent(event);
            }
        }));
        eventPoller.setCycleCount(Timeline.INDEFINITE);

        // Initial state
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
        for (Node child : contentHolder.getChildren()) {
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
        runBox.getChildren().add(runButton);

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

    private void addActivityEvent(AnalysisEvent event) {
        if (event.type() == AnalysisEventType.MODEL_STREAM_CHUNK) {
            Platform.runLater(() -> {
                flushQueue(); // Ensure MODEL event is processed before appending
                if (currentModelBubble != null) {
                    currentModelBubble.appendChunk(event.details());
                }
            });
            return;
        }
        eventQueue.add(event);
    }
    
    private void flushQueue() {
        while (!eventQueue.isEmpty()) {
            processEvent(eventQueue.poll());
        }
    }

    private void processEvent(AnalysisEvent event) {
        Node node;
        if (event.type() == AnalysisEventType.INFO) {
            TypingMessageBubble aiBubble = new TypingMessageBubble("🤖", "#4bf6ff", "rgba(75,246,255,0.15)", investigationSessionView, false);
            aiBubble.startTyping(event.title());
            node = aiBubble;
            statusLabel.setText("● Building evidence...");
        } else if (event.type() == AnalysisEventType.MODEL) {
            currentModelBubble = new TypingMessageBubble("🧠", "#8bfd91", "rgba(139,253,145,0.15)", investigationSessionView, true);
            currentModelBubble.startTyping(modelSelector.getValue() + " reasoning...\n\n");
            
            VBox modelGroup = new VBox(16);
            modelGroup.getChildren().addAll(buildSystemCard(event), currentModelBubble);
            node = modelGroup;
            statusLabel.setText("● Receiving response tokens...");
        } else if (event.type() == AnalysisEventType.SUCCESS || event.type() == AnalysisEventType.ERROR) {
            node = buildSystemCard(event);
            statusLabel.setText("✓ Analysis complete");
        } else {
            node = buildSystemCard(event);
            statusLabel.setText("● Computing metrics...");
        }
        
        node.setTranslateY(15);
        node.setOpacity(0);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
        tt.setToY(0);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(1);
        
        timelineFeed.getChildren().add(node);
        tt.play();
        ft.play();
        
        // Auto scroll
        Platform.runLater(() -> investigationSessionView.setVvalue(1.0));
    }

    private Node buildSystemCard(AnalysisEvent event) {
        HBox container = new HBox();
        container.setPadding(new Insets(0, 0, 0, 40)); 
        
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #111819; -fx-border-color: #1e2526; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;");
        card.setMaxWidth(500);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        String icon = "•";
        String color = "#849494";
        if (event.type() == AnalysisEventType.METRIC) { icon = "📊"; color = "#4bf6ff"; }
        else if (event.type() == AnalysisEventType.FINDING) { icon = "🔍"; color = "#ffd166"; }
        else if (event.type() == AnalysisEventType.MODEL) { icon = "⚡"; color = "#8bfd91"; }
        else if (event.type() == AnalysisEventType.SUCCESS) { icon = "✓"; color = "#06d6a0"; }
        else if (event.type() == AnalysisEventType.ERROR) { icon = "✕"; color = "#ff4b4b"; }

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
        
        Label titleLbl = new Label(event.title());
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        
        header.getChildren().addAll(iconLbl, titleLbl);
        card.getChildren().add(header);
        
        if (event.details() != null && !event.details().isBlank()) {
            Label detailsLbl = new Label(event.details());
            detailsLbl.setWrapText(true);
            detailsLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #849494; -fx-line-spacing: 4px;");
            card.getChildren().add(detailsLbl);
        }

        container.getChildren().add(card);
        return container;
    }

    // ── Run Analysis ──────────────────────────────────────────────────────────

    private void handleRunAnalysis() {
        if (lastEngine == null) return;

        runButton.setDisable(true);
        runButton.setText("⏳  Analysing…");
        runButton.setStyle(buildRunButtonStyle(true));
        
        timelineFeed.getChildren().clear();
        resultsDashboardView.getChildren().clear();
        resultsDashboardView.setVisible(false);
        resultsDashboardView.setManaged(false);
        currentModelBubble = null;
        eventQueue.clear();
        statusLabel.setText("● Initializing analysis...");
        statusFooter.setVisible(true);
        statusFooter.setManaged(true);
        
        switchView(investigationSessionView);
        eventPoller.play();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                AnalysisContext ctx = new AnalysisContext(
                        lastEngine.getProjectInfo(),
                        lastEngine.getDependencyGraph(),
                        lastEngine.getProjectInfo().getEntities()
                );
                String model = modelSelector.getValue();
                AiAnalysisService service = registry.get(selectedAnalysisType);

                if (service == null) {
                    throw new IllegalStateException("Analysis type not implemented: " + selectedAnalysisType);
                }

                return service.analyze(ctx, model, DefectWorkspace.this::addActivityEvent);
            }
        };

        task.setOnSucceeded(e -> {
            lastFindings = task.getValue();
            Platform.runLater(() -> {
                flushQueue();
                eventPoller.stop();
                if (currentModelBubble != null) {
                    currentModelBubble.finishStreaming();
                }
                
                statusLabel.setText("✓ Analysis Complete - Generating findings dashboard...");
                
                // Wait 1 second before showing results
                Timeline delay = new Timeline(new KeyFrame(Duration.millis(1000), ev -> {
                    statusFooter.setVisible(false);
                    statusFooter.setManaged(false);
                    
                    Label transitionLbl = new Label("✓ Analysis Completed");
                    transitionLbl.setStyle("-fx-text-fill: #06d6a0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 20 40;");
                    timelineFeed.getChildren().add(transitionLbl);

                    populateResultsView(lastFindings);
                    
                    resultsDashboardView.setVisible(true);
                    resultsDashboardView.setManaged(true);


                    
                    FadeTransition fade = new FadeTransition(Duration.millis(800), resultsDashboardView);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    
                    TranslateTransition translate = new TranslateTransition(Duration.millis(800), resultsDashboardView);
                    translate.setFromY(40);
                    translate.setToY(0);
                    
                    ParallelTransition pt = new ParallelTransition(fade, translate);
                    PauseTransition pause =
                            new PauseTransition(
                                    Duration.millis(300)
                            );

                    pause.setOnFinished(event -> {

                        scrollToResults();

                        pt.play();
                    });

                    pause.play();





                    
                    // Smooth auto scroll to findings
                    
                    resetRunButton();
                }));
                delay.play();
            });
        });

        task.setOnFailed(e -> Platform.runLater(() -> {
            flushQueue();
            eventPoller.stop();
            if (currentModelBubble != null) {
                currentModelBubble.finishStreaming();
            }
            Throwable ex = task.getException();
            lastFindings = "Analysis failed: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            
            processEvent(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.ERROR, "Analysis Failed", lastFindings));
            
            statusFooter.setVisible(false);
            statusFooter.setManaged(false);
            
            populateResultsView(lastFindings);
            resultsDashboardView.setVisible(true);
            resultsDashboardView.setManaged(true);
            
            resetRunButton();
        }));

        new Thread(task, "Bodhak-AI-Analysis-Thread").start();
    }

    private void resetRunButton() {
        runButton.setDisable(lastEngine == null);
        runButton.setText("▶  Run Analysis");
        runButton.setStyle(buildRunButtonStyle(false));
    }

    // ── Structured Results UI ─────────────────────────────────────────────────

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }

    private void populateResultsView(String findings) {
        resultsDashboardView.getChildren().clear();
        resultsAnchor = new Label();
        resultsAnchor.setManaged(false);
        resultsAnchor.setVisible(false);

        resultsDashboardView.getChildren().add(resultsAnchor);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 0, 10, 0));

        Label typeLabel = new Label(selectedAnalysisType.name().replace("_", " ") + " RESULTS");
        typeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label badge = new Label("AI Generated");
        badge.setStyle("-fx-background-color: rgba(139,253,145,0.10); -fx-text-fill: #8bfd91; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");

        header.getChildren().addAll(typeLabel, badge);
        resultsDashboardView.getChildren().add(header);

        // Try parsing JSON
        try {
            String jsonContent = cleanJson(findings);
            AnalysisResult result = mapper.readValue(jsonContent, AnalysisResult.class);

            if (result != null && result.defects != null && !result.defects.isEmpty()) {
                // Summary Cards Layout
                HBox summaryLayout = new HBox(20);
                summaryLayout.getChildren().addAll(
                    createSummaryCard("Total Defects", String.valueOf(result.defects.size()), "#dde4e5"),
                    createSummaryCard("High Severity", String.valueOf(result.defects.stream().filter(d -> "HIGH".equalsIgnoreCase(d.severity) || "CRITICAL".equalsIgnoreCase(d.severity)).count()), "#ff4b4b"),
                    createSummaryCard("Medium Severity", String.valueOf(result.defects.stream().filter(d -> "MEDIUM".equalsIgnoreCase(d.severity)).count()), "#ffd166")
                );
                resultsDashboardView.getChildren().add(summaryLayout);

                // Defect Cards
                VBox defectsList = new VBox(16);
                for (Defect defect : result.defects) {
                    defectsList.getChildren().add(createDefectCard(defect));
                }
                resultsDashboardView.getChildren().add(defectsList);
            } else {
                Label noDefects = new Label("No structured defects found. The AI returned an empty or invalid format.");
                noDefects.setStyle("-fx-text-fill: #849494; -fx-font-size: 14px;");
                resultsDashboardView.getChildren().add(noDefects);
            }
        } catch (Exception ex) {
            // Fallback to text if JSON parsing fails
            Label errorLabel = new Label("Could not parse structured results. Displaying raw output.");
            errorLabel.setStyle("-fx-text-fill: #ff4b4b; -fx-font-size: 12px;");
            resultsDashboardView.getChildren().add(errorLabel);
            ex.printStackTrace();
            System.out.println("RAW RESPONSE");
            System.out.println(findings);
        }

        // Developer JSON Section
        TitledPane devPane = new TitledPane();
        devPane.setText("Developer Mode: Raw JSON Response");
        devPane.setExpanded(false);
        devPane.setStyle("-fx-base: #111819; -fx-box-border: #1e2526; -fx-text-fill: #849494; -fx-font-size: 12px;");
        
        Label rawLabel = new Label(findings);
        rawLabel.setWrapText(true);
        rawLabel.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-text-fill: #a8b5b5; -fx-padding: 10; -fx-font-size: 11px;");
        
        ScrollPane rawScroll = new ScrollPane(rawLabel);
        rawScroll.setFitToWidth(true);
        rawScroll.setPrefHeight(200);
        rawScroll.setStyle("-fx-background: #0b0f10; -fx-border-color: #1e2526;");
        
        devPane.setContent(rawScroll);
        resultsDashboardView.getChildren().add(devPane);
    }

    private VBox createSummaryCard(String title, String value, String valueColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 24, 16, 24));
        card.setStyle("-fx-background-color: #111819; -fx-background-radius: 8; -fx-border-color: #1e2526; -fx-border-radius: 8; -fx-border-width: 1;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #566465; -fx-font-weight: bold; -fx-letter-spacing: 0.5;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + valueColor + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox createDefectCard(Defect defect) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #111819; -fx-background-radius: 8; -fx-border-color: #1e2526; -fx-border-radius: 8; -fx-border-width: 1;");

        // Header (Type + Severity)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label typeLabel = new Label(defect.defectType != null ? defect.defectType : "Unknown Defect");
        typeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        String sevColor = "#a8b5b5";
        if (defect.severity != null) {
            if ("HIGH".equalsIgnoreCase(defect.severity) || "CRITICAL".equalsIgnoreCase(defect.severity)) sevColor = "#ff4b4b";
            else if ("MEDIUM".equalsIgnoreCase(defect.severity)) sevColor = "#ffd166";
            else if ("LOW".equalsIgnoreCase(defect.severity)) sevColor = "#06d6a0";
        }

        Label sevBadge = new Label(defect.severity != null ? defect.severity.toUpperCase() : "UNKNOWN");
        sevBadge.setStyle("-fx-background-color: " + sevColor + "20; -fx-text-fill: " + sevColor + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(typeLabel, sevBadge, spacer);

        // Explanation
        Label explanation = new Label(defect.explanation != null ? defect.explanation : "No explanation provided.");
        explanation.setWrapText(true);
        explanation.setStyle("-fx-font-size: 13px; -fx-text-fill: #a8b5b5; -fx-line-spacing: 4px;");

        card.getChildren().addAll(header, explanation);

        // Entities
        if (defect.responsibleEntities != null && !defect.responsibleEntities.isEmpty()) {
            VBox entitiesBox = new VBox(4);
            Label entTitle = new Label("Responsible Entities:");
            entTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849494;");
            
            FlowPane flow = new FlowPane(8, 8);
            for (String entity : defect.responsibleEntities) {
                Label eLbl = new Label(entity);
                eLbl.setStyle("-fx-background-color: #1a2122; -fx-text-fill: #4bf6ff; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4; -fx-border-color: #242b2c; -fx-border-radius: 4;");
                flow.getChildren().add(eLbl);
            }
            entitiesBox.getChildren().addAll(entTitle, flow);
            card.getChildren().add(entitiesBox);
        }

        // Fixes
        if (defect.possibleFixes != null && !defect.possibleFixes.isEmpty()) {
            VBox fixesBox = new VBox(4);
            Label fixesTitle = new Label("Suggested Fixes:");
            fixesTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849494;");
            fixesBox.getChildren().add(fixesTitle);
            
            for (String fix : defect.possibleFixes) {
                Label fixLbl = new Label("• " + fix);
                fixLbl.setWrapText(true);
                fixLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8bfd91;");
                fixesBox.getChildren().add(fixLbl);
            }
            card.getChildren().add(fixesBox);
        }

        return card;
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
    private void scrollToResults() {

        Platform.runLater(() -> {

            sessionContainer.applyCss();
            sessionContainer.layout();

            double contentHeight =
                    sessionContainer.getHeight();

            double viewportHeight =
                    investigationSessionView
                            .getViewportBounds()
                            .getHeight();

            double resultsY =
                    resultsDashboardView.getLayoutY();

            double scrollableHeight =
                    contentHeight - viewportHeight;

            if (scrollableHeight <= 0) {
                return;
            }

            double targetVvalue =
                    resultsY / scrollableHeight;

            targetVvalue =
                    Math.max(
                            0.0,
                            Math.min(1.0, targetVvalue)
                    );

            System.out.println(
                    "resultsY=" + resultsY +
                            ", contentHeight=" + contentHeight +
                            ", viewportHeight=" + viewportHeight +
                            ", target=" + targetVvalue
            );

            Timeline timeline =
                    new Timeline(
                            new KeyFrame(
                                    Duration.seconds(1.2),
                                    new KeyValue(
                                            investigationSessionView.vvalueProperty(),
                                            targetVvalue,
                                            Interpolator.EASE_BOTH
                                    )
                            )
                    );

            timeline.play();
        });
    }
}
