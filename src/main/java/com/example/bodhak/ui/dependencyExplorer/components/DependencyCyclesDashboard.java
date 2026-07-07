package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.SemanticGraphIndex;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.bodhak.ui.dependencyExplorer.state.CyclesState;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Polyline;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencyCyclesDashboard extends ScrollPane {

    private final DependencyExplorerState state;
    private final Consumer<String> tabSwitcher;
    private final BiConsumer<EntityInfo, EntityInfo> pathFinderTrigger;

    // Top Metrics
    private final StringProperty totalCyclesMetric = new SimpleStringProperty("0");
    private final StringProperty affectedEntitiesMetric = new SimpleStringProperty("0");
    private final StringProperty maxCycleLengthMetric = new SimpleStringProperty("0");
    private final StringProperty strongComponentsMetric = new SimpleStringProperty("0");
    private final StringProperty cycleDensityMetric = new SimpleStringProperty("0.00%");
    private final StringProperty cycleHealthMetric = new SimpleStringProperty("Healthy");

    // Right Metrics Panel Bindings
    private final StringProperty cycleLengthMetric = new SimpleStringProperty("-");
    private final StringProperty internalDepsMetric = new SimpleStringProperty("-");
    private final StringProperty externalDepsMetric = new SimpleStringProperty("-");
    private final StringProperty instabilityMetric = new SimpleStringProperty("-");
    private final StringProperty impactScoreMetric = new SimpleStringProperty("-");

    // UI Containers
    private final VBox mainLayout = new VBox(16);
    private final VBox cycleListViewBox = new VBox(8);
    private final Label cycleListTitle = new Label("CYCLE LIST (0)");
    private final TextField searchField = new TextField();

    private GridPane metricsGrid;
    private final StackPane contentLayoutContainer = new StackPane();
    private final HBox contentHBox = new HBox(16);
    private final VBox contentVBox = new VBox(16);
    
    private final VBox detailContainer = new VBox(16);
    private final HBox detailTopRow = new HBox(16);
    private final HBox detailBottomRow = new HBox(16);

    private VBox leftCol;
    private VBox centerCol;
    private VBox rightCol;

    // Center Detail panel
    private final Label detailTitleLabel = new Label("SELECT A CYCLE TO VIEW DETAILS");
    private final Label detailBadgeEntities = new Label("0 Entities");
    private final Label detailBadgeImpact = new Label("Low Impact");
    private final VBox flowchartCardsBox = new VBox(8);
    private final Pane flowchartArrowPane = new Pane();
    private Node registeredFirstCard = null;
    private Node registeredLastCard = null;
    private final javafx.beans.value.ChangeListener<Object> redrawListener = (obs, oldVal, newVal) -> drawRedReturnLine();
    private final VBox suggestedBreakpointsBox = new VBox(8);
    
    // Root Cause Analysis fields
    private final Label primaryCauseLabel = new Label("—");
    private final Label secondaryCauseLabel = new Label("—");
    private final Label introducedByLabel = new Label("—");
    private final Label cyclePatternLabel = new Label("—");

    // Cycle Impact fields
    private final Label entitiesAffectedLabel = new Label("0");
    private final Label namespacesAffectedLabel = new Label("0");
    private final Label compUnitsAffectedLabel = new Label("0");
    private final Label archRiskLabel = new Label("MEDIUM");
    private final ProgressBar refactorCostBar = new ProgressBar(0.0);
    private final Label refactorCostLabel = new Label("0%");
    private final Label warningLabel = new Label("Break any link in this cycle to eliminate circular dependency.");

    private VBox rootCauseCard;
    private VBox impactCard;

    public DependencyCyclesDashboard(DependencyExplorerState state, Consumer<String> tabSwitcher, BiConsumer<EntityInfo, EntityInfo> pathFinderTrigger) {
        this.state = state;
        this.tabSwitcher = tabSwitcher;
        this.pathFinderTrigger = pathFinderTrigger;
        initialise();
    }

    private void initialise() {
        setFitToWidth(true);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        getStyleClass().add("dd-scroll-pane");
        setStyle("-fx-background: #0d141a; -fx-background-color: #0d141a;");

        mainLayout.setPadding(new Insets(16));
        mainLayout.setStyle("-fx-background-color: #0d141a;");
        setContent(mainLayout);

        // Bind metrics properties directly to CyclesState properties
        totalCyclesMetric.bind(state.getCyclesState().totalCyclesMetricProperty());
        affectedEntitiesMetric.bind(state.getCyclesState().affectedEntitiesMetricProperty());
        maxCycleLengthMetric.bind(state.getCyclesState().maxCycleLengthMetricProperty());
        strongComponentsMetric.bind(state.getCyclesState().strongComponentsMetricProperty());
        cycleDensityMetric.bind(state.getCyclesState().cycleDensityMetricProperty());
        cycleHealthMetric.bind(state.getCyclesState().cycleHealthMetricProperty());

        // Bind metrics panel detail fields
        cycleLengthMetric.bind(state.getCyclesState().cycleLengthMetricProperty());
        internalDepsMetric.bind(state.getCyclesState().internalDepsMetricProperty());
        externalDepsMetric.bind(state.getCyclesState().externalDepsMetricProperty());
        instabilityMetric.bind(state.getCyclesState().instabilityMetricProperty());
        impactScoreMetric.bind(state.getCyclesState().impactScoreMetricProperty());

        // Bind root cause details directly to CyclesState properties
        primaryCauseLabel.textProperty().bind(state.getCyclesState().primaryCauseProperty());
        secondaryCauseLabel.textProperty().bind(state.getCyclesState().secondaryCauseProperty());
        introducedByLabel.textProperty().bind(state.getCyclesState().introducedByProperty());
        cyclePatternLabel.textProperty().bind(state.getCyclesState().cyclePatternProperty());

        // Bind impact details directly to CyclesState properties
        entitiesAffectedLabel.textProperty().bind(state.getCyclesState().entitiesAffectedProperty());
        namespacesAffectedLabel.textProperty().bind(state.getCyclesState().namespacesAffectedProperty());
        compUnitsAffectedLabel.textProperty().bind(state.getCyclesState().compUnitsAffectedProperty());
        archRiskLabel.textProperty().bind(state.getCyclesState().archRiskProperty());
        refactorCostBar.progressProperty().bind(state.getCyclesState().refactorCostProgressProperty());
        refactorCostLabel.textProperty().bind(state.getCyclesState().refactorCostLabelProperty());

        // Bind Arch Risk Color to risk changes
        state.getCyclesState().archRiskProperty().addListener((obs, oldVal, risk) -> {
            String riskColor = "#ffa726";
            if ("CRITICAL".equals(risk)) {
                riskColor = "#ff4b4b";
            } else if ("HIGH".equals(risk)) {
                riskColor = "#ec407a";
            } else if ("LOW".equals(risk)) {
                riskColor = "#00e676";
            }
            archRiskLabel.setStyle("-fx-text-fill: " + riskColor + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        });

        // Listen for cycle list changes
        state.getCyclesState().getCycles().addListener((javafx.collections.ListChangeListener<EntityPath>) c -> {
            int total = state.getCyclesState().getCycles().size();
            cycleListTitle.setText("CYCLE LIST (" + total + ")");
            populateCycleList(searchField.getText());
        });

        // Listen for selected cycle changes to rebuild/redraw the UI
        state.getCyclesState().selectedCycleProperty().addListener((obs, oldVal, newVal) -> {
            populateDetailView();
        });

        // Initial setup
        int total = state.getCyclesState().getCycles().size();
        cycleListTitle.setText("CYCLE LIST (" + total + ")");
        populateCycleList(searchField.getText());
        populateDetailView();

        buildDashboard();
    }

    private void buildDashboard() {
        metricsGrid = new GridPane();
        metricsGrid.setHgap(12);
        metricsGrid.setVgap(12);

        metricsGrid.add(createMetricCard("TOTAL CYCLES", totalCyclesMetric, "Circular dependency groups"), 0, 0);
        metricsGrid.add(createMetricCard("AFFECTED ENTITIES", affectedEntitiesMetric, "Entities involved in cycles"), 1, 0);
        metricsGrid.add(createMetricCard("MAX CYCLE LENGTH", maxCycleLengthMetric, "Longest cycle contains N entities"), 2, 0);
        metricsGrid.add(createMetricCard("STRONG COMPONENTS", strongComponentsMetric, "Strongly connected components"), 3, 0);
        metricsGrid.add(createMetricCard("CYCLE DENSITY", cycleDensityMetric, "Of total dependencies"), 4, 0);
        metricsGrid.add(createMetricCard("CYCLE HEALTH", cycleHealthMetric, "Review and refactor cycles"), 5, 0);

        leftCol = buildLeftColumn();
        leftCol.setPrefWidth(280);
        leftCol.setMinWidth(280);
        HBox.setHgrow(leftCol, Priority.NEVER);

        centerCol = buildCenterColumn();
        HBox.setHgrow(centerCol, Priority.ALWAYS);

        rightCol = buildRightColumn();
        rightCol.setPrefWidth(300);
        rightCol.setMinWidth(300);
        HBox.setHgrow(rightCol, Priority.NEVER);

        rootCauseCard = buildRootCauseCard();
        impactCard = buildImpactCard();

        // Content configurations HBox/VBox
        contentHBox.setAlignment(Pos.TOP_LEFT);
        contentVBox.setAlignment(Pos.TOP_LEFT);
        
        contentHBox.setMaxWidth(Double.MAX_VALUE);
        contentVBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(contentHBox, Priority.ALWAYS);
        VBox.setVgrow(contentVBox, Priority.ALWAYS);
        
        contentLayoutContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(contentLayoutContainer, Priority.ALWAYS);

        mainLayout.getChildren().addAll(metricsGrid, contentLayoutContainer);

        // Adapt Layout initially
        adaptLayout(1400);

        widthProperty().addListener((obs, oldVal, newVal) -> {
            adaptLayout(newVal.doubleValue());
        });
    }

    private void adaptLayout(double width) {
        // --- 1. Metrics Grid Responsiveness ---
        metricsGrid.getChildren().clear();
        metricsGrid.getColumnConstraints().clear();
        metricsGrid.getRowConstraints().clear();

        Node card1 = createMetricCard("TOTAL CYCLES", totalCyclesMetric, "Circular dependency groups");
        Node card2 = createMetricCard("AFFECTED ENTITIES", affectedEntitiesMetric, "Entities involved in cycles");
        Node card3 = createMetricCard("MAX CYCLE LENGTH", maxCycleLengthMetric, "Longest cycle contains N entities");
        Node card4 = createMetricCard("STRONG COMPONENTS", strongComponentsMetric, "Strongly connected components");
        Node card5 = createMetricCard("CYCLE DENSITY", cycleDensityMetric, "Of total dependencies");
        Node card6 = createMetricCard("CYCLE HEALTH", cycleHealthMetric, "Review and refactor cycles");

        if (width > 1200) {
            // Wide Mode: 6 columns, 1 row
            for (int i = 0; i < 6; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100.0 / 6.0);
                metricsGrid.getColumnConstraints().add(cc);
            }
            metricsGrid.add(card1, 0, 0);
            metricsGrid.add(card2, 1, 0);
            metricsGrid.add(card3, 2, 0);
            metricsGrid.add(card4, 3, 0);
            metricsGrid.add(card5, 4, 0);
            metricsGrid.add(card6, 5, 0);
        } else if (width > 850) {
            // Medium Mode: 3 columns, 2 rows
            for (int i = 0; i < 3; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100.0 / 3.0);
                metricsGrid.getColumnConstraints().add(cc);
            }
            metricsGrid.add(card1, 0, 0);
            metricsGrid.add(card2, 1, 0);
            metricsGrid.add(card3, 2, 0);
            metricsGrid.add(card4, 0, 1);
            metricsGrid.add(card5, 1, 1);
            metricsGrid.add(card6, 2, 1);
        } else {
            // Narrow Mode: 2 columns, 3 rows
            for (int i = 0; i < 2; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100.0 / 2.0);
                metricsGrid.getColumnConstraints().add(cc);
            }
            metricsGrid.add(card1, 0, 0);
            metricsGrid.add(card2, 1, 0);
            metricsGrid.add(card3, 0, 1);
            metricsGrid.add(card4, 1, 1);
            metricsGrid.add(card5, 0, 2);
            metricsGrid.add(card6, 1, 2);
        }

        // --- 2. Main Columns Responsiveness ---
        detailContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(detailContainer, Priority.ALWAYS);
        HBox.setHgrow(detailContainer, Priority.ALWAYS);

        detailTopRow.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(detailTopRow, Priority.ALWAYS);

        detailBottomRow.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(detailBottomRow, Priority.ALWAYS);

        if (width > 1150) {
            // Wide Mode: side-by-side
            contentHBox.getChildren().clear();
            contentVBox.getChildren().clear();
            detailContainer.getChildren().clear();
            detailTopRow.getChildren().clear();
            detailBottomRow.getChildren().clear();
            
            leftCol.setPrefWidth(280); leftCol.setMinWidth(280); HBox.setHgrow(leftCol, Priority.NEVER);
            
            centerCol.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(centerCol, Priority.ALWAYS);
            rightCol.setPrefWidth(300); rightCol.setMinWidth(300); HBox.setHgrow(rightCol, Priority.NEVER);
            detailTopRow.getChildren().addAll(centerCol, rightCol);

            rootCauseCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(rootCauseCard, Priority.ALWAYS);
            impactCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(impactCard, Priority.ALWAYS);
            detailBottomRow.getChildren().addAll(rootCauseCard, impactCard);

            detailContainer.getChildren().addAll(detailTopRow, detailBottomRow);
            contentHBox.getChildren().addAll(leftCol, detailContainer);

            if (contentLayoutContainer.getChildren().isEmpty() || contentLayoutContainer.getChildren().get(0) != contentHBox) {
                contentLayoutContainer.getChildren().clear();
                contentLayoutContainer.getChildren().add(contentHBox);
            }
        } else if (width > 850) {
            // Medium Mode: leftCol side-by-side with VBox(centerCol + rightCol + bottomRow)
            contentHBox.getChildren().clear();
            contentVBox.getChildren().clear();
            detailContainer.getChildren().clear();
            detailTopRow.getChildren().clear();
            detailBottomRow.getChildren().clear();

            leftCol.setPrefWidth(260); leftCol.setMinWidth(260); HBox.setHgrow(leftCol, Priority.NEVER);
            
            VBox centerRightVBox = new VBox(16);
            centerRightVBox.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(centerRightVBox, Priority.ALWAYS);
            centerCol.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(centerCol, Priority.ALWAYS);
            rightCol.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(rightCol, Priority.ALWAYS);
            centerRightVBox.getChildren().addAll(centerCol, rightCol);

            rootCauseCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(rootCauseCard, Priority.ALWAYS);
            impactCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(impactCard, Priority.ALWAYS);
            detailBottomRow.getChildren().addAll(rootCauseCard, impactCard);

            detailContainer.getChildren().addAll(centerRightVBox, detailBottomRow);
            contentHBox.getChildren().addAll(leftCol, detailContainer);

            if (contentLayoutContainer.getChildren().isEmpty() || contentLayoutContainer.getChildren().get(0) != contentHBox) {
                contentLayoutContainer.getChildren().clear();
                contentLayoutContainer.getChildren().add(contentHBox);
            }
        } else {
            // Narrow Mode: leftCol, centerCol, rightCol, rootCauseCard, impactCard stacked vertically
            contentHBox.getChildren().clear();
            contentVBox.getChildren().clear();
            detailContainer.getChildren().clear();
            detailTopRow.getChildren().clear();
            detailBottomRow.getChildren().clear();

            leftCol.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(leftCol, Priority.ALWAYS);
            centerCol.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(centerCol, Priority.ALWAYS);
            rightCol.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(rightCol, Priority.ALWAYS);
            rootCauseCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(rootCauseCard, Priority.ALWAYS);
            impactCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(impactCard, Priority.ALWAYS);

            contentVBox.getChildren().addAll(leftCol, centerCol, rightCol, rootCauseCard, impactCard);

            if (contentLayoutContainer.getChildren().isEmpty() || contentLayoutContainer.getChildren().get(0) != contentVBox) {
                contentLayoutContainer.getChildren().clear();
                contentLayoutContainer.getChildren().add(contentVBox);
            }
        }
    }

    private VBox createMetricCard(String title, StringProperty valueProp, String subtitle) {
        VBox card = new VBox(4);
        card.getStyleClass().add("dd-stat-tile");
        card.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 12;");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("dd-stat-label");
        lblTitle.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; -fx-text-fill: #849396;");

        Label lblValue = new Label();
        lblValue.textProperty().bind(valueProp);
        lblValue.getStyleClass().add("dd-stat-value");
        lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Apply health color styling dynamically
        valueProp.addListener((obs, old, val) -> {
            if (title.equals("CYCLE HEALTH")) {
                if (val.equals("Healthy")) {
                    lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
                } else if (val.equals("Needs Attention")) {
                    lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
                } else {
                    lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                }
            } else {
                lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00daf3;");
            }
        });

        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-text-fill: #607274; -fx-font-size: 8px;");

        card.getChildren().addAll(lblTitle, lblValue, lblSub);
        return card;
    }

    private VBox buildLeftColumn() {
        VBox col = new VBox(12);
        col.getStyleClass().add("dd-card");
        col.setPadding(new Insets(16));

        cycleListTitle.getStyleClass().add("dd-card-title");
        cycleListTitle.setStyle("-fx-text-fill: #bac9cc; -fx-font-weight: bold;");

        searchField.setPromptText("Search cycles...");
        searchField.setStyle("-fx-background-color: #0b1014; -fx-text-fill: #dce3ec; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 10;");
        searchField.textProperty().addListener((obs, old, val) -> populateCycleList(val));

        ScrollPane listScroll = new ScrollPane(cycleListViewBox);
        listScroll.setFitToWidth(true);
        listScroll.setFitToHeight(false);
        listScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        listScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        listScroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        Button btnExport = new Button("Export Cycles Report");
        btnExport.setStyle("-fx-background-color: rgba(0, 218, 243, 0.08); -fx-text-fill: #00daf3; -fx-border-color: rgba(0, 218, 243, 0.2); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 16; -fx-alignment: center; -fx-font-size: 11px;");
        btnExport.setMaxWidth(Double.MAX_VALUE);

        col.getChildren().addAll(cycleListTitle, searchField, listScroll, btnExport);
        return col;
    }

    private void populateCycleList(String query) {
        cycleListViewBox.getChildren().clear();
        String lowerQuery = query == null ? "" : query.toLowerCase().trim();

        var cyclesList = state.getCyclesState().getCycles();
        for (int i = 0; i < cyclesList.size(); i++) {
            EntityPath path = cyclesList.get(i);
            String cycleName = "Cycle #" + (i + 1);
            int entitiesCount = path.getEntities().size() - 1; // last repeats first

            // Simple preview string
            StringBuilder preview = new StringBuilder();
            List<String> list = path.getEntities();
            int previewCount = Math.min(4, list.size());
            for (int k = 0; k < previewCount; k++) {
                String full = list.get(k);
                String simpleName = full.substring(full.lastIndexOf('.') + 1);
                preview.append(simpleName);
                if (k < previewCount - 1) {
                    preview.append(" → ");
                }
            }
            if (list.size() > 4) {
                preview.append(" → ... → ");
                String lastFull = list.get(list.size() - 1);
                preview.append(lastFull.substring(lastFull.lastIndexOf('.') + 1));
            }

            if (!lowerQuery.isEmpty() && !cycleName.toLowerCase().contains(lowerQuery) && !preview.toString().toLowerCase().contains(lowerQuery)) {
                continue;
            }

            VBox card = new VBox(6);
            card.setStyle("-fx-background-color: #12181f; -fx-border-color: rgba(132, 147, 150, 0.08); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
            
            HBox topRow = new HBox(8);
            topRow.setAlignment(Pos.CENTER_LEFT);
            Label lblName = new Label(cycleName);
            lblName.setStyle("-fx-text-fill: #dce3ec; -fx-font-weight: bold; -fx-font-size: 12px;");
            Label lblCount = new Label(entitiesCount + " entities");
            lblCount.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px;");
            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label();
            if (entitiesCount >= 20) {
                badge.setText("High");
                badge.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: rgba(231, 76, 60, 0.1); -fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            } else if (entitiesCount >= 6) {
                badge.setText("Medium");
                badge.setStyle("-fx-text-fill: #e67e22; -fx-background-color: rgba(230, 126, 34, 0.1); -fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            } else {
                badge.setText("Low");
                badge.setStyle("-fx-text-fill: #2ecc71; -fx-background-color: rgba(46, 204, 113, 0.1); -fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            }

            topRow.getChildren().addAll(lblName, lblCount, spacer, badge);

            Label lblPreview = new Label(preview.toString());
            lblPreview.setStyle("-fx-text-fill: #607274; -fx-font-size: 9px; -fx-font-family: 'JetBrains Mono';");
            lblPreview.setWrapText(true);

            card.getChildren().addAll(topRow, lblPreview);

            if (state.getCyclesState().selectedCycleProperty().get() == path) {
                card.setStyle("-fx-background-color: rgba(0, 218, 243, 0.05); -fx-border-color: #00daf3; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
            }

            final EntityPath targetPath = path;
            card.setOnMouseClicked(e -> {
                state.getCyclesState().selectedCycleProperty().set(targetPath);
                state.getCyclesState().updateSelectedCycleMetrics(state.analysisContextProperty().get(), targetPath);
            });

            cycleListViewBox.getChildren().add(card);
        }
    }

    private VBox buildCenterColumn() {
        VBox col = new VBox(12);
        col.getStyleClass().add("dd-card");
        col.setPadding(new Insets(16));

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        detailTitleLabel.getStyleClass().add("dd-card-title");
        detailTitleLabel.setStyle("-fx-text-fill: #bac9cc; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        detailBadgeEntities.setStyle("-fx-text-fill: #00daf3; -fx-background-color: rgba(0, 218, 243, 0.1); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");
        detailBadgeImpact.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: rgba(231, 76, 60, 0.1); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");

        Button btnFit = new Button("Fit to View");
        btnFit.setStyle("-fx-background-color: #12181f; -fx-text-fill: #849396; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px;");
        Button btnReset = new Button("Reset View");
        btnReset.setStyle("-fx-background-color: #12181f; -fx-text-fill: #849396; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 10px;");

        header.getChildren().addAll(detailTitleLabel, detailBadgeEntities, detailBadgeImpact, spacer, btnFit, btnReset);

        // Coupling description banner
        HBox descBanner = new HBox(8);
        descBanner.setAlignment(Pos.CENTER_LEFT);
        descBanner.setStyle("-fx-background-color: rgba(0, 218, 243, 0.03); -fx-border-color: rgba(0, 218, 243, 0.1); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10 12;");
        Label descInfoIcon = new Label("ⓘ");
        descInfoIcon.setStyle("-fx-text-fill: #00daf3; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label descLabel = new Label("This cycle creates tight coupling in your architecture. Breaking any reference will eliminate it.");
        descLabel.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
        descBanner.getChildren().addAll(descInfoIcon, descLabel);

        // Legend row
        HBox legendRow = new HBox(16);
        legendRow.setAlignment(Pos.CENTER);
        legendRow.setPadding(new Insets(8, 0, 8, 0));
        
        Label legDepends = new Label("➡ Depends On");
        legDepends.setStyle("-fx-text-fill: #00daf3; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label legUsed = new Label("⬅ Used By");
        legUsed.setStyle("-fx-text-fill: #8e44ad; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label legReturn = new Label("➡ Returns to start");
        legReturn.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px; -fx-font-weight: bold;");
        legendRow.getChildren().addAll(legDepends, legUsed, legReturn);

        // Flowchart Panel (Cards + Arrow overlay next to each other)
        HBox flowchartHBox = new HBox(8);
        flowchartHBox.setAlignment(Pos.TOP_CENTER);
        flowchartHBox.setPadding(new Insets(16, 0, 16, 0));
        
        flowchartCardsBox.setAlignment(Pos.TOP_CENTER);
        flowchartCardsBox.setPrefWidth(320);

        flowchartArrowPane.setPrefWidth(40);
        flowchartArrowPane.setMinWidth(40);

        flowchartHBox.getChildren().addAll(flowchartCardsBox, flowchartArrowPane);

        // Dynamic Polyline drawing overlay listener
        flowchartCardsBox.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            drawRedReturnLine();
        });

        ScrollPane centerScroll = new ScrollPane(flowchartHBox);
        centerScroll.setFitToWidth(true);
        centerScroll.setFitToHeight(false);
        centerScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        centerScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        centerScroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(centerScroll, Priority.ALWAYS);

        // Bottom Warning Banner
        HBox bottomBanner = new HBox(12);
        bottomBanner.setAlignment(Pos.CENTER_LEFT);
        bottomBanner.setStyle("-fx-background-color: rgba(231, 76, 60, 0.05); -fx-border-color: rgba(231, 76, 60, 0.1); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 16;");
        
        Label warningIcon = new Label("⚠");
        warningIcon.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");
        warningLabel.setStyle("-fx-text-fill: #bac9cc; -fx-font-size: 11px;");
        HBox.setHgrow(warningLabel, Priority.ALWAYS);

//        btnViewBreakSuggestions.setStyle("-fx-background-color: rgba(231, 76, 60, 0.1); -fx-text-fill: #e74c3c; -fx-border-color: rgba(231, 76, 60, 0.2); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12; -fx-font-size: 10px; -fx-cursor: hand;");

        bottomBanner.getChildren().addAll(warningIcon, warningLabel);

        col.getChildren().addAll(header, descBanner, legendRow, centerScroll, bottomBanner);
        return col;
    }

    private void populateDetailView() {
        AnalysisContext context = state.analysisContextProperty().get();
        SemanticGraphIndex graphIndex = context != null ? context.getSemanticGraphIndex() : null;
        flowchartCardsBox.getChildren().clear();
        suggestedBreakpointsBox.getChildren().clear();

        EntityPath selectedCycle = state.getCyclesState().selectedCycleProperty().get();
        if (selectedCycle == null) {
            detailTitleLabel.setText("NO CYCLES DETECTED");
            detailBadgeEntities.setVisible(false);
            detailBadgeImpact.setVisible(false);
            return;
        }

        var cyclesList = state.getCyclesState().getCycles();
        int index = cyclesList.indexOf(selectedCycle) + 1;
        detailTitleLabel.setText("CYCLE #" + index + " - DETAIL VIEW");
        
        int nEntities = selectedCycle.getEntities().size() - 1;
        detailBadgeEntities.setText(nEntities + " Entities");
        detailBadgeEntities.setVisible(true);

        if (nEntities >= 20) {
            detailBadgeImpact.setText("High Impact");
            detailBadgeImpact.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: rgba(231, 76, 60, 0.1); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");
        } else if (nEntities >= 6) {
            detailBadgeImpact.setText("Medium Impact");
            detailBadgeImpact.setStyle("-fx-text-fill: #e67e22; -fx-background-color: rgba(230, 126, 34, 0.1); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");
        } else {
            detailBadgeImpact.setText("Low Impact");
            detailBadgeImpact.setStyle("-fx-text-fill: #2ecc71; -fx-background-color: rgba(46, 204, 113, 0.1); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 4;");
        }
        detailBadgeImpact.setVisible(true);

        // Render flowchart nodes (exclude repeated start node at the end)
        List<String> nodes = selectedCycle.getEntities();
        int totalNodes = nodes.isEmpty() ? 0 : nodes.size() - 1;
        for (int i = 0; i < totalNodes; i++) {
            if (totalNodes > 100 && i == 50) {
                Label truncateLabel = new Label("... (+ " + (totalNodes - 100) + " more nodes) ...");
                truncateLabel.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 8 0;");
                flowchartCardsBox.getChildren().add(truncateLabel);

                Label arrow = new Label("↓");
                arrow.setStyle("-fx-text-fill: #00daf3; -fx-font-size: 14px; -fx-padding: 2 0;");
                flowchartCardsBox.getChildren().add(arrow);
                
                i = totalNodes - 50; // skip intermediate nodes
                continue;
            }

            String node = nodes.get(i);
            String simple = node.substring(node.lastIndexOf('.') + 1);
            String pkg = node.substring(0, Math.max(0, node.lastIndexOf('.')));

            VBox nodeCard = new VBox(4);
            nodeCard.getStyleClass().add("dd-path-node-card");
            nodeCard.setPadding(new Insets(10, 12, 10, 12));
            nodeCard.setStyle("-fx-background-color: #12181f; -fx-border-color: rgba(132, 147, 150, 0.08); -fx-border-radius: 8; -fx-background-radius: 8;");
            
            HBox top = new HBox(8);
            top.setAlignment(Pos.CENTER_LEFT);
            Label lblNum = new Label((i + 1) + ".");
            lblNum.setStyle("-fx-text-fill: #849396; -fx-font-weight: bold; -fx-font-size: 11px;");
            Label lblName = new Label(simple);
            lblName.setStyle("-fx-text-fill: #dce3ec; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label kindChip = new Label("class");
            kindChip.setStyle("-fx-text-fill: #849396; -fx-background-color: rgba(132, 147, 150, 0.05); -fx-font-size: 8px; -fx-padding: 2 6; -fx-background-radius: 4;");
            
            // Map node info to JavaFX component references if this is the start or end card
            if (i == 0) {
                registeredFirstCard = nodeCard;
            }
            if (i == totalNodes - 1) {
                registeredLastCard = nodeCard;
            }

            top.getChildren().addAll(lblNum, lblName, spacer, kindChip);

            Label lblPkg = new Label(pkg);
            lblPkg.setStyle("-fx-text-fill: #849396; -fx-font-size: 8px;");

            nodeCard.getChildren().addAll(top, lblPkg);
            flowchartCardsBox.getChildren().add(nodeCard);

            if (i < totalNodes - 1) {
                Label arrow = new Label("↓");
                arrow.setStyle("-fx-text-fill: #00daf3; -fx-font-size: 14px; -fx-padding: 2 0;");
                flowchartCardsBox.getChildren().add(arrow);
            }
        }

        // Generate Suggested Breakpoints
        List<String[]> edgeList = new ArrayList<>();
        for (int i = 0; i < nodes.size() - 1; i++) {
            edgeList.add(new String[]{nodes.get(i), nodes.get(i+1)});
        }
        int limit = Math.min(4, edgeList.size());
        for (int i = 0; i < limit; i++) {
            String[] edge = edgeList.get(i);
            String fromSimple = edge[0].substring(edge[0].lastIndexOf('.') + 1);
            String toSimple = edge[1].substring(edge[1].lastIndexOf('.') + 1);
            String refType = i == 0 ? "Dependency Injection" : (i == 1 ? "Interface Extraction" : (i == 2 ? "Event / Observer Pattern" : "Refactor Utility Class"));
            String severity = i == 0 ? "High Impact" : (i == 1 ? "Medium Impact" : "Low Impact");
            suggestedBreakpointsBox.getChildren().add(createBreakpointItem(fromSimple + " → " + toSimple, refType, severity));
        }

        Platform.runLater(this::drawRedReturnLine);
    }

    private HBox createBreakpointItem(String edgeText, String pattern, String severity) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #12181f; -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12;");

        CheckBox cb = new CheckBox();
        cb.setStyle("-fx-opacity: 0.8;");

        VBox texts = new VBox(2);
        Label lblEdge = new Label(edgeText);
        lblEdge.setStyle("-fx-text-fill: #dce3ec; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
        Label lblPattern = new Label(pattern);
        lblPattern.setStyle("-fx-text-fill: #849396; -fx-font-size: 9px;");
        texts.getChildren().addAll(lblEdge, lblPattern);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblSev = new Label(severity);
        if (severity.contains("High")) {
            lblSev.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: rgba(231, 76, 60, 0.1); -fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 3;");
        } else if (severity.contains("Medium")) {
            lblSev.setStyle("-fx-text-fill: #e67e22; -fx-background-color: rgba(230, 126, 34, 0.1); -fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 3;");
        } else {
            lblSev.setStyle("-fx-text-fill: #2ecc71; -fx-background-color: rgba(46, 204, 113, 0.1); -fx-font-size: 8px; -fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 3;");
        }

        box.getChildren().addAll(cb, texts, spacer, lblSev);
        return box;
    }

    private VBox buildRightColumn() {
        VBox col = new VBox(16);
        col.setAlignment(Pos.TOP_LEFT);

        // 1. Cycle Metrics card
        VBox metricsCard = new VBox(12);
        metricsCard.getStyleClass().add("dd-card");
        Label lblMetTitle = new Label("CYCLE METRICS");
        lblMetTitle.getStyleClass().add("dd-card-title");
        lblMetTitle.setStyle("-fx-text-fill: #bac9cc; -fx-font-weight: bold;");

        VBox metricsRows = new VBox(8);
        metricsRows.getChildren().addAll(
            createDetailMetricRow("Cycle Length", cycleLengthMetric),
            createDetailMetricRow("Internal Dependencies", internalDepsMetric),
            createDetailMetricRow("External Dependencies", externalDepsMetric),
            createDetailMetricRow("Instability", instabilityMetric),
            createDetailMetricRow("Impact Score", impactScoreMetric)
        );

        metricsCard.getChildren().addAll(lblMetTitle, metricsRows);

        // 2. Suggested Breakpoints card
        VBox breakCard = new VBox(12);
        breakCard.getStyleClass().add("dd-card");
        Label lblBreakTitle = new Label("SUGGESTED BREAK POINTS");
        lblBreakTitle.getStyleClass().add("dd-card-title");
        lblBreakTitle.setStyle("-fx-text-fill: #bac9cc; -fx-font-weight: bold;");

        ScrollPane breakScroll = new ScrollPane(suggestedBreakpointsBox);
        breakScroll.setFitToWidth(true);
        breakScroll.setFitToHeight(false);
        breakScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        breakScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        breakScroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        Button btnViewAll = new Button("View All Suggestions");
        btnViewAll.setStyle("-fx-background-color: #12181f; -fx-text-fill: #00daf3; -fx-border-color: rgba(0, 218, 243, 0.1); -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8; -fx-font-size: 11px;");
        btnViewAll.setMaxWidth(Double.MAX_VALUE);

        breakCard.getChildren().addAll(lblBreakTitle, breakScroll, btnViewAll);

        col.getChildren().addAll(metricsCard, breakCard);
        return col;
    }

    private VBox buildRootCauseCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("dd-card");
        Label lblRootCauseTitle = new Label("ROOT CAUSE ANALYSIS");
        lblRootCauseTitle.getStyleClass().add("dd-card-title");
        lblRootCauseTitle.setStyle("-fx-text-fill: #bac9cc; -fx-font-weight: bold;");
        
        VBox rcContent = new VBox(8);
        rcContent.getChildren().addAll(
            createDetailTextRow("Primary Cause", primaryCauseLabel),
            createSeparatorLine(),
            createDetailTextRow("Secondary Cause", secondaryCauseLabel),
            createSeparatorLine(),
            createDetailTextRow("Introduced By", introducedByLabel),
            createSeparatorLine(),
            createDetailTextRow("Cycle Pattern", cyclePatternLabel)
        );
        card.getChildren().addAll(lblRootCauseTitle, rcContent);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox buildImpactCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("dd-card");
        Label lblImpactTitle = new Label("CYCLE IMPACT");
        lblImpactTitle.getStyleClass().add("dd-card-title");
        lblImpactTitle.setStyle("-fx-text-fill: #bac9cc; -fx-font-weight: bold;");
        
        VBox impactRows = new VBox(8);
        impactRows.getChildren().addAll(
            createDetailTextRow("Entities affected", entitiesAffectedLabel),
            createDetailTextRow("Namespaces", namespacesAffectedLabel),
            createDetailTextRow("Compilation Units", compUnitsAffectedLabel),
            createDetailTextRow("Architecture Risk", archRiskLabel),
            createCostProgressBarRow()
        );
        card.getChildren().addAll(lblImpactTitle, impactRows);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }




    private VBox createDetailTextRow(String label, Label val) {
        VBox row = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        val.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
        val.setWrapText(true);
        val.setMinWidth(0);
        
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private Region createSeparatorLine() {
        Region hr = new Region();
        hr.setStyle("-fx-background-color: rgba(132, 147, 150, 0.1); -fx-min-height: 1; -fx-max-height: 1;");
        return hr;
    }

    private VBox createCostProgressBarRow() {
        VBox row = new VBox(6);
        
        HBox labelRow = new HBox(8);
        Label lbl = new Label("Estimated Refactoring Cost");
        lbl.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px; -fx-font-weight: bold;");
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        refactorCostLabel.setStyle("-fx-text-fill: #00daf3; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
        labelRow.getChildren().addAll(lbl, spacer, refactorCostLabel);
        
        refactorCostBar.setMaxWidth(Double.MAX_VALUE);
        refactorCostBar.setStyle("-fx-accent: #00daf3; -fx-control-inner-background: #12181f; -fx-background-color: transparent; -fx-pref-height: 8;");
        
        row.getChildren().addAll(labelRow, refactorCostBar);
        return row;
    }

    private HBox createDetailMetricRow(String label, StringProperty valueProp) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label val = new Label();
        val.textProperty().bind(valueProp);
        val.setStyle("-fx-text-fill: #dce3ec; -fx-font-weight: bold; -fx-font-size: 11px;");
        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private Button createActionButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bac9cc; -fx-padding: 6 0; -fx-alignment: center-left; -fx-font-size: 11px; -fx-cursor: hand;");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00daf3; -fx-padding: 6 0; -fx-alignment: center-left; -fx-font-size: 11px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bac9cc; -fx-padding: 6 0; -fx-alignment: center-left; -fx-font-size: 11px; -fx-cursor: hand;"));
        return btn;
    }

    private void drawRedReturnLine() {
        flowchartArrowPane.getChildren().clear();
        if (flowchartCardsBox.getChildren().isEmpty()) return;

        Node firstCard = null;
        Node lastCard = null;
        for (Node child : flowchartCardsBox.getChildren()) {
            if (child instanceof VBox && child.getStyleClass().contains("dd-path-node-card")) {
                if (firstCard == null) firstCard = child;
                lastCard = child;
            }
        }

        if (firstCard == null || lastCard == null || firstCard == lastCard) return;

        double startY = firstCard.getLayoutY() + firstCard.getBoundsInParent().getHeight() / 2;
        double endY = lastCard.getLayoutY() + lastCard.getBoundsInParent().getHeight() / 2;

        // If startY and endY are both 0 (meaning layout pass hasn't completed yet), defer
        if (startY == 0 && endY == 0) {
            Platform.runLater(this::drawRedReturnLine);
            return;
        }

        // Setup property listeners for dynamic coordinate updates
        if (registeredFirstCard != firstCard || registeredLastCard != lastCard) {
            if (registeredFirstCard != null) {
                registeredFirstCard.layoutYProperty().removeListener(redrawListener);
                registeredFirstCard.boundsInParentProperty().removeListener(redrawListener);
            }
            if (registeredLastCard != null) {
                registeredLastCard.layoutYProperty().removeListener(redrawListener);
                registeredLastCard.boundsInParentProperty().removeListener(redrawListener);
            }

            registeredFirstCard = firstCard;
            registeredLastCard = lastCard;

            if (firstCard != null) {
                firstCard.layoutYProperty().addListener(redrawListener);
                firstCard.boundsInParentProperty().addListener(redrawListener);
            }
            if (lastCard != null) {
                lastCard.layoutYProperty().addListener(redrawListener);
                lastCard.boundsInParentProperty().addListener(redrawListener);
            }
        }

        Polyline redLine = new Polyline();
        redLine.getPoints().addAll(
            0.0, endY,
            30.0, endY,
            30.0, startY,
            0.0, startY
        );
        redLine.setStyle("-fx-stroke: #e74c3c; -fx-stroke-width: 2; -fx-fill: transparent;");

        Polyline arrowhead = new Polyline();
        arrowhead.getPoints().addAll(
            8.0, startY - 5.0,
            0.0, startY,
            8.0, startY + 5.0
        );
        arrowhead.setStyle("-fx-stroke: #e74c3c; -fx-stroke-width: 2; -fx-fill: transparent;");

        flowchartArrowPane.getChildren().addAll(redLine, arrowhead);
    }
}
