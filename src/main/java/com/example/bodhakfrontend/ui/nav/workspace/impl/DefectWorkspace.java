package com.example.bodhakfrontend.ui.nav.workspace.impl;

import com.example.bodhakfrontend.ai.service.ArchitectureAnalysisService;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.ui.nav.workspace.Workspace;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Persistent workspace for the DEFECTS tab — AI-powered analysis centre.
 *
 * <h3>Layout</h3>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  LEFT SIDEBAR  │  RIGHT CONTENT AREA                           │
 * │                │                                               │
 * │  ○ Architecture│  (initially) Run an AI analysis to generate  │
 * │  ○ Performance │  project insights.                            │
 * │  ○ Scalability │                                               │
 * │  ○ Security    │  (after run) AI findings cards                │
 * │  ○ Maintain.   │                                               │
 * │                │                                               │
 * │  ─────────     │                                               │
 * │  Model:    [▼] │                                               │
 * │  Scope:    [▼] │                                               │
 * │  [Run AI]      │                                               │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h3>State preserved across navigation</h3>
 * <ul>
 *   <li>Selected analysis type</li>
 *   <li>AI findings / last response text</li>
 *   <li>Scroll position of the content area</li>
 *   <li>Model and scope selector values</li>
 * </ul>
 */
public class DefectWorkspace implements Workspace {

    // ── Analysis type labels ──────────────────────────────────────────────────
    private static final String[] ANALYSIS_TYPES = {
        "Architecture Analysis",
        "Performance Analysis",
        "Scalability Analysis",
        "Security Analysis",
        "Maintainability Analysis"
    };

    // ── Persistent root ───────────────────────────────────────────────────────
    private final BorderPane root;

    // ── Sidebar controls ──────────────────────────────────────────────────────
    private final ToggleGroup analysisToggle = new ToggleGroup();
    private final ComboBox<String> modelSelector;
    private final ComboBox<String> scopeSelector;
    private final Button runButton;

    // ── Content area ──────────────────────────────────────────────────────────
    private final StackPane   contentHolder;
    private final ScrollPane  contentScroll;

    // ── State ─────────────────────────────────────────────────────────────────
    private AnalysisEngine lastEngine;
    private String         lastFindings;        // AI result text — survives navigation
    private String         selectedAnalysisType = ANALYSIS_TYPES[0];

    // =========================================================================
    // ── Constructor ───────────────────────────────────────────────────────────
    // =========================================================================

    public DefectWorkspace() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0e1415;");

        // ── Model selector ───────────────────────────────────────────────────
        modelSelector = new ComboBox<>();
        modelSelector.getItems().addAll(
                "gemini-2.5-flash",
                "gemini-2.0-flash",
                "gemini-1.5-pro"
        );
        modelSelector.getSelectionModel().selectFirst();
        styleComboBox(modelSelector);

        // ── Scope selector ───────────────────────────────────────────────────
        scopeSelector = new ComboBox<>();
        scopeSelector.getItems().addAll(
                "Full Project",
                "Entry Points Only",
                "Critical Paths Only"
        );
        scopeSelector.getSelectionModel().selectFirst();
        styleComboBox(scopeSelector);

        // ── Run button ───────────────────────────────────────────────────────
        runButton = new Button("▶  Run AI Analysis");
        runButton.setMaxWidth(Double.MAX_VALUE);
        runButton.setStyle(buildRunButtonStyle(false));
        runButton.setOnMouseEntered(e -> {
            if (!runButton.isDisabled()) runButton.setStyle(buildRunButtonHoverStyle());
        });
        runButton.setOnMouseExited(e -> {
            if (!runButton.isDisabled()) runButton.setStyle(buildRunButtonStyle(false));
        });
        runButton.setOnAction(e -> handleRunAnalysis());

        // ── Content area ─────────────────────────────────────────────────────
        contentHolder = new StackPane();
        contentHolder.setStyle("-fx-background-color: #0e1415;");
        contentHolder.setPadding(new Insets(40));

        contentScroll = new ScrollPane(contentHolder);
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background-color: #0e1415; -fx-background: #0e1415;");
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        showEmptyContent();

        // ── Assemble layout ──────────────────────────────────────────────────
        root.setLeft(buildSidebar());
        root.setCenter(contentScroll);
    }

    // =========================================================================
    // ── Workspace API ─────────────────────────────────────────────────────────
    // =========================================================================

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        if (engine == lastEngine) return;
        lastEngine = engine;

        if (engine == null) {
            // Project unloaded — reset to empty state but keep UI structure
            lastFindings = null;
            showEmptyContent();
            runButton.setDisable(true);
        } else {
            runButton.setDisable(false);
            // If we had previous findings from the old project, clear them
            if (lastFindings != null) {
                lastFindings = null;
                showEmptyContent();
            }
        }
    }

    // =========================================================================
    // ── Sidebar construction ──────────────────────────────────────────────────
    // =========================================================================

    private Node buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);
        sidebar.setStyle("""
            -fx-background-color: #111819;
            -fx-border-color: transparent #1e2526 transparent transparent;
            -fx-border-width: 0 1 0 0;
            """);

        // ── Header ──────────────────────────────────────────────────────────
        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 20, 20, 20));

        Label title = new Label("AI Analysis");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label sub = new Label("Select analysis type");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #566465;");

        header.getChildren().addAll(title, sub);
        sidebar.getChildren().add(header);

        // Divider
        sidebar.getChildren().add(divider());

        // ── Analysis type toggle buttons ─────────────────────────────────────
        VBox typeList = new VBox(2);
        typeList.setPadding(new Insets(12, 10, 12, 10));

        String[] icons = {"🏛", "⚡", "📈", "🔒", "🔧"};
        boolean[] isPlaceholder = {false, false, false, true, true};

        for (int i = 0; i < ANALYSIS_TYPES.length; i++) {
            String type  = ANALYSIS_TYPES[i];
            String icon  = icons[i];
            boolean stub = isPlaceholder[i];
            ToggleButton btn = buildAnalysisTypeButton(icon + "  " + type, stub);
            if (i == 0) btn.setSelected(true);
            btn.setOnAction(e -> {
                selectedAnalysisType = type;
                // If we already have findings for a different type, clear them
                // (user explicitly chose a different analysis)
            });
            typeList.getChildren().add(btn);
        }
        sidebar.getChildren().add(typeList);

        // Divider + configuration section
        sidebar.getChildren().add(divider());

        VBox config = new VBox(14);
        config.setPadding(new Insets(16, 16, 16, 16));

        config.getChildren().addAll(
            labelledControl("Model", modelSelector),
            labelledControl("Scope", scopeSelector)
        );

        // Spacer to push button to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(config, spacer);

        // ── Run button ───────────────────────────────────────────────────────
        VBox runBox = new VBox();
        runBox.setPadding(new Insets(12, 16, 24, 16));
        runBox.getChildren().add(runButton);
        sidebar.getChildren().add(runBox);

        return sidebar;
    }

    private ToggleButton buildAnalysisTypeButton(String text, boolean isPlaceholder) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(analysisToggle);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        String baseStyle = """
            -fx-background-color: transparent;
            -fx-text-fill: %s;
            -fx-font-size: 12px;
            -fx-background-radius: 6;
            -fx-padding: 8 12;
            -fx-cursor: hand;
            -fx-alignment: CENTER_LEFT;
            """;

        String textColor = isPlaceholder ? "#566465" : "#849494";
        btn.setStyle(baseStyle.formatted(textColor));

        // Add "Coming Soon" indicator for placeholder items
        if (isPlaceholder) {
            btn.setText(text + "  ·  soon");
            btn.setDisable(true);
        }

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle("""
                    -fx-background-color: rgba(75,246,255,0.08);
                    -fx-text-fill: #4bf6ff;
                    -fx-font-size: 12px;
                    -fx-background-radius: 6;
                    -fx-padding: 8 12;
                    -fx-cursor: hand;
                    -fx-alignment: CENTER_LEFT;
                    -fx-border-color: transparent transparent transparent #4bf6ff;
                    -fx-border-width: 0 0 0 2;
                    """);
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

    // =========================================================================
    // ── Content area helpers ──────────────────────────────────────────────────
    // =========================================================================

    private void showEmptyContent() {
        VBox empty = new VBox(20);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80, 40, 80, 40));

        // Animated hexagon icon
        Label hexIcon = new Label("⬡");
        hexIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #1e2a2b;");

        // Pulse animation on the icon
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,     e -> hexIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #1e2a2b;")),
            new KeyFrame(Duration.millis(1200), e -> hexIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #2a3a3c;"))
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        Label heading = new Label("AI Analysis Ready");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #849494;");

        Label subheading = new Label(
                "Select an analysis type from the sidebar,\n" +
                "then click Run AI Analysis to generate insights.");
        subheading.setStyle("-fx-font-size: 13px; -fx-text-fill: #566465;");
        subheading.setTextAlignment(TextAlignment.CENTER);
        subheading.setWrapText(true);

        // Feature pills
        HBox pills = new HBox(10);
        pills.setAlignment(Pos.CENTER);
        pills.getChildren().addAll(
            pill("Dependency cycles"),
            pill("Coupling violations"),
            pill("Architecture risks")
        );

        empty.getChildren().addAll(hexIcon, heading, subheading, pills);
        contentHolder.getChildren().setAll(empty);
    }

    private void showLoadingContent() {
        VBox loading = new VBox(20);
        loading.setAlignment(Pos.CENTER);
        loading.setPadding(new Insets(80));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(48, 48);
        spinner.setStyle("-fx-accent: #4bf6ff;");

        Label msg = new Label("Analysing with " + modelSelector.getValue() + "…");
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #849494;");

        Label detail = new Label("This may take a few seconds for large projects.");
        detail.setStyle("-fx-font-size: 11px; -fx-text-fill: #566465;");

        loading.getChildren().addAll(spinner, msg, detail);
        contentHolder.getChildren().setAll(loading);
    }

    private void showFindingsContent(String findings) {
        VBox page = new VBox(24);
        page.setPadding(new Insets(0));

        // ── Result header ────────────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 4, 0));

        Label typeLabel = new Label(selectedAnalysisType);
        typeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label badge = new Label("AI Generated");
        badge.setStyle("""
            -fx-background-color: rgba(139,253,145,0.10);
            -fx-text-fill: #8bfd91;
            -fx-font-size: 10px;
            -fx-font-weight: bold;
            -fx-padding: 4 10;
            -fx-background-radius: 12;
            """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label modelBadge = new Label(modelSelector.getValue());
        modelBadge.setStyle("""
            -fx-background-color: rgba(75,246,255,0.08);
            -fx-text-fill: #4bf6ff;
            -fx-font-size: 10px;
            -fx-padding: 4 10;
            -fx-background-radius: 12;
            """);

        header.getChildren().addAll(typeLabel, badge, spacer, modelBadge);
        page.getChildren().add(header);

        // Divider
        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color: #1e2526;");
        page.getChildren().add(div);

        // ── Findings card ────────────────────────────────────────────────────
        VBox findingsCard = new VBox(0);
        findingsCard.setStyle("""
            -fx-background-color: #111819;
            -fx-background-radius: 10;
            -fx-border-color: #1e2526;
            -fx-border-radius: 10;
            -fx-border-width: 1;
            """);

        // Card header
        HBox cardHeader = new HBox(10);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(14, 20, 14, 20));
        cardHeader.setStyle("-fx-border-color: transparent transparent #1e2526 transparent; -fx-border-width: 0 0 1 0;");

        Label cardTitle = new Label("📋  Analysis Findings");
        cardTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        cardHeader.getChildren().add(cardTitle);
        findingsCard.getChildren().add(cardHeader);

        // Card content — scrollable text
        Label findingsLabel = new Label(findings);
        findingsLabel.setStyle("""
            -fx-font-family: 'JetBrains Mono', 'Cascadia Code', monospace;
            -fx-font-size: 12px;
            -fx-text-fill: #a8b5b5;
            -fx-padding: 20;
            """);
        findingsLabel.setWrapText(true);
        findingsCard.getChildren().add(findingsLabel);

        page.getChildren().add(findingsCard);

        // Fade in
        FadeTransition ft = new FadeTransition(Duration.millis(300), page);
        ft.setFromValue(0);
        ft.setToValue(1);

        contentHolder.getChildren().setAll(page);
        ft.play();
    }

    // =========================================================================
    // ── Run Analysis handler ──────────────────────────────────────────────────
    // =========================================================================

    private void handleRunAnalysis() {
        if (lastEngine == null) return;

        runButton.setDisable(true);
        runButton.setText("⏳  Analysing…");
        runButton.setStyle(buildRunButtonStyle(true));
        showLoadingContent();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                AnalysisContext ctx = new AnalysisContext(
                        lastEngine.getProjectInfo(),
                        lastEngine.getDependencyGraph(),
                        lastEngine.getProjectInfo().getEntities()
                );
                ArchitectureAnalysisService service = new ArchitectureAnalysisService();
                return service.analyze(ctx);
            }
        };

        task.setOnSucceeded(e -> {
            lastFindings = task.getValue();
            Platform.runLater(() -> {
                showFindingsContent(lastFindings);
                resetRunButton();
            });
        });

        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            lastFindings = "Analysis failed: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            showFindingsContent(lastFindings);
            resetRunButton();
        }));

        new Thread(task, "Bodhak-AI-Analysis-Thread").start();
    }

    private void resetRunButton() {
        runButton.setDisable(lastEngine == null);
        runButton.setText("▶  Run AI Analysis");
        runButton.setStyle(buildRunButtonStyle(false));
    }

    // =========================================================================
    // ── Style helpers ─────────────────────────────────────────────────────────
    // =========================================================================

    private String buildRunButtonStyle(boolean disabled) {
        return """
            -fx-background-color: %s;
            -fx-text-fill: #0e1415;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-cursor: %s;
            """.formatted(
                disabled ? "#2f3637" : "linear-gradient(to right, #4bf6ff, #8bfd91)",
                disabled ? "default" : "hand"
        );
    }

    private String buildRunButtonHoverStyle() {
        return """
            -fx-background-color: linear-gradient(to right, #8bfd91, #4bf6ff);
            -fx-text-fill: #0e1415;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-cursor: hand;
            """;
    }

    private void styleComboBox(ComboBox<?> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle("""
            -fx-background-color: #1a2122;
            -fx-text-fill: #dde4e5;
            -fx-font-size: 12px;
            -fx-background-radius: 6;
            -fx-border-color: #242b2c;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            """);
    }

    private Label pill(String text) {
        Label l = new Label(text);
        l.setStyle("""
            -fx-background-color: rgba(75,246,255,0.07);
            -fx-text-fill: #4bf6ff;
            -fx-font-size: 10px;
            -fx-padding: 4 12;
            -fx-background-radius: 20;
            """);
        return l;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: #1a2122;");
        return r;
    }
}
