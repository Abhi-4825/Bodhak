package com.example.bodhakfrontend.ui.nav.workspace.impl;

import com.example.bodhakfrontend.ai.event.AnalysisEvent;
import com.example.bodhakfrontend.ai.event.AnalysisEventType;
import com.example.bodhakfrontend.ai.model.AnalysisType;
import com.example.bodhakfrontend.ui.nav.workspace.impl.DefectWorkspace.AnalysisResult;
import com.example.bodhakfrontend.ui.nav.workspace.impl.DefectWorkspace.Defect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InvestigationPane extends ScrollPane {

    private final VBox sessionContainer;
    private final VBox timelineFeed;
    private final VBox resultsDashboardView;
    private final HBox statusFooter;
    private final Label statusLabel;
    
    private TypingMessageBubble currentModelBubble;
    private final Queue<AnalysisEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final Timeline eventPoller;
    
    private String runningModel;
    private Label resultsAnchor;
    private boolean hasRun = false;

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public InvestigationPane() {
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: #0e1415;");
        setHbarPolicy(ScrollBarPolicy.NEVER);

        timelineFeed = new VBox(20);
        timelineFeed.setMaxWidth(800);
        timelineFeed.setAlignment(Pos.TOP_LEFT);

        statusFooter = new HBox(8);
        statusFooter.setAlignment(Pos.CENTER_LEFT);
        statusFooter.setPadding(new Insets(10, 0, 10, 40));
        statusFooter.setVisible(false);
        statusFooter.setManaged(false);
        
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

        setContent(sessionContainer);

        // Setup event poller
        eventPoller = new Timeline(new KeyFrame(Duration.millis(600), e -> {
            AnalysisEvent event = eventQueue.poll();
            if (event != null) {
                processEvent(event);
            }
        }));
        eventPoller.setCycleCount(Timeline.INDEFINITE);
    }

    public boolean hasRun() {
        return hasRun;
    }

    public void prepareForAnalysis(String model) {
        this.hasRun = true;
        this.runningModel = model;
        timelineFeed.getChildren().clear();
        resultsDashboardView.getChildren().clear();
        resultsDashboardView.setVisible(false);
        resultsDashboardView.setManaged(false);
        currentModelBubble = null;
        eventQueue.clear();
        statusLabel.setText("● Initializing analysis...");
        statusFooter.setVisible(true);
        statusFooter.setManaged(true);
        eventPoller.play();
    }

    public void stopAnalysis() {
        eventPoller.stop();
        if (currentModelBubble != null) {
            currentModelBubble.finishStreaming();
        }
        statusLabel.setText("✕ Analysis stopped by user");
        processEvent(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.ERROR, "Analysis Stopped", "The operation was cancelled by the user."));
        statusFooter.setVisible(false);
        statusFooter.setManaged(false);
    }

    public void finalizeAnalysis(String findings, AnalysisType type) {
        flushQueue();
        eventPoller.stop();
        if (currentModelBubble != null) {
            currentModelBubble.finishStreaming();
        }

        statusFooter.setVisible(false);
        statusFooter.setManaged(false);

        Label transitionLbl = new Label("✓ Analysis Completed");
        transitionLbl.setStyle("-fx-text-fill: #06d6a0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 20 40;");
        timelineFeed.getChildren().add(transitionLbl);

        populateResultsView(findings, type);

        resultsDashboardView.setVisible(true);
        resultsDashboardView.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(800), resultsDashboardView);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(800), resultsDashboardView);
        translate.setFromY(40);
        translate.setToY(0);

        ParallelTransition pt = new ParallelTransition(fade, translate);
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(event -> {
            scrollToResults();
            pt.play();
        });
        pause.play();
    }

    public void handleAnalysisFailure(String errorMessage) {
        flushQueue();
        eventPoller.stop();
        if (currentModelBubble != null) {
            currentModelBubble.finishStreaming();
        }

        processEvent(new AnalysisEvent(System.currentTimeMillis(), AnalysisEventType.ERROR, "Analysis Failed", errorMessage));

        statusFooter.setVisible(false);
        statusFooter.setManaged(false);

        populateResultsView(errorMessage, null);
        resultsDashboardView.setVisible(true);
        resultsDashboardView.setManaged(true);
    }

    public void addActivityEvent(AnalysisEvent event) {
        if (event.type() == AnalysisEventType.MODEL_STREAM_CHUNK) {
            Platform.runLater(() -> {
                flushQueue();
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
            TypingMessageBubble aiBubble = new TypingMessageBubble("🤖", "#4bf6ff", "rgba(75,246,255,0.15)", this, false);
            aiBubble.startTyping(event.title());
            node = aiBubble;
            statusLabel.setText("● Building evidence...");
        } else if (event.type() == AnalysisEventType.MODEL) {
            currentModelBubble = new TypingMessageBubble("🧠", "#8bfd91", "rgba(139,253,145,0.15)", this, true);
            currentModelBubble.startTyping(runningModel + " reasoning...\n\n");

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

        Platform.runLater(() -> setVvalue(1.0));
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

    private void populateResultsView(String findings, AnalysisType type) {
        resultsDashboardView.getChildren().clear();
        resultsAnchor = new Label();
        resultsAnchor.setManaged(false);
        resultsAnchor.setVisible(false);
        resultsDashboardView.getChildren().add(resultsAnchor);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 0, 10, 0));

        String titleText = type != null ? type.name().replace("_", " ") + " RESULTS" : "ANALYSIS RESULTS";
        Label typeLabel = new Label(titleText);
        typeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label badge = new Label("AI Generated");
        badge.setStyle("-fx-background-color: rgba(139,253,145,0.10); -fx-text-fill: #8bfd91; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");

        header.getChildren().addAll(typeLabel, badge);
        resultsDashboardView.getChildren().add(header);

        try {
            String jsonContent = cleanJson(findings);
            AnalysisResult result = mapper.readValue(jsonContent, AnalysisResult.class);

            if (result != null && result.defects != null && !result.defects.isEmpty()) {
                HBox summaryLayout = new HBox(20);
                summaryLayout.getChildren().addAll(
                    createSummaryCard("Total Defects", String.valueOf(result.defects.size()), "#dde4e5"),
                    createSummaryCard("High Severity", String.valueOf(result.defects.stream().filter(d -> "HIGH".equalsIgnoreCase(d.severity) || "CRITICAL".equalsIgnoreCase(d.severity)).count()), "#ff4b4b"),
                    createSummaryCard("Medium Severity", String.valueOf(result.defects.stream().filter(d -> "MEDIUM".equalsIgnoreCase(d.severity)).count()), "#ffd166")
                );
                resultsDashboardView.getChildren().add(summaryLayout);

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
            Label errorLabel = new Label("Could not parse structured results. Displaying raw output.");
            errorLabel.setStyle("-fx-text-fill: #ff4b4b; -fx-font-size: 12px;");
            resultsDashboardView.getChildren().add(errorLabel);
            ex.printStackTrace();
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

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
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

        Label explanation = new Label(defect.explanation != null ? defect.explanation : "No explanation provided.");
        explanation.setWrapText(true);
        explanation.setStyle("-fx-font-size: 13px; -fx-text-fill: #a8b5b5; -fx-line-spacing: 4px;");

        card.getChildren().addAll(header, explanation);

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

        if (defect.possibleFixes != null && !defect.possibleFixes.isEmpty()) {
            VBox fixesBox = new VBox(4);
            Label fixesTitle = new Label("Suggested Fixes:");
            fixesTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849494;");
            fixesBox.getChildren().add(fixesTitle);

            for (Object fixObj : defect.possibleFixes) {
                String fixText;
                if (fixObj instanceof String) {
                    fixText = (String) fixObj;
                } else if (fixObj instanceof java.util.Map) {
                    java.util.Map<?, ?> map = (java.util.Map<?, ?>) fixObj;
                    Object desc = map.get("description");
                    if (desc == null) desc = map.get("fix");
                    if (desc == null) desc = map.get("title");
                    if (desc == null) {
                        fixText = map.values().stream().map(Object::toString).collect(java.util.stream.Collectors.joining(" - "));
                    } else {
                        fixText = desc.toString();
                    }
                } else {
                    fixText = String.valueOf(fixObj);
                }
                Label fixLbl = new Label("• " + fixText);
                fixLbl.setWrapText(true);
                fixLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8bfd91;");
                fixesBox.getChildren().add(fixLbl);
            }
            card.getChildren().add(fixesBox);
        }

        return card;
    }

    private void scrollToResults() {
        Platform.runLater(() -> {
            sessionContainer.applyCss();
            sessionContainer.layout();

            double contentHeight = sessionContainer.getHeight();
            double viewportHeight = getViewportBounds().getHeight();
            double resultsY = resultsDashboardView.getLayoutY();

            double scrollableHeight = contentHeight - viewportHeight;
            if (scrollableHeight <= 0) return;

            double targetVvalue = resultsY / scrollableHeight;
            targetVvalue = Math.max(0.0, Math.min(1.0, targetVvalue));

            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(1.2),
                    new KeyValue(vvalueProperty(), targetVvalue, Interpolator.EASE_BOTH)
            ));
            timeline.play();
        });
    }
}
