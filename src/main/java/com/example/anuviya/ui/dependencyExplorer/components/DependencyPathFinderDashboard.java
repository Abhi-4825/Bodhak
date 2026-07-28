package com.example.anuviya.ui.dependencyExplorer.components;

import com.example.anuviya.context.SemanticGraphIndex;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.anuviya.ui.dependencyExplorer.components.PathQueryEngine.PathQueryResult;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;

import java.util.*;

public class DependencyPathFinderDashboard extends ScrollPane {

    private final DependencyExplorerState state;
    private SemanticGraphIndex graphIndex = null;
    private final PathQueryEngine queryEngine = new PathQueryEngine();

    private final ObjectProperty<EntityInfo> fromEntity = new SimpleObjectProperty<>(null);
    private final ObjectProperty<EntityInfo> toEntity = new SimpleObjectProperty<>(null);

    // Form inputs
    private ComboBox<String> pathTypeCombo;
    private Spinner<Integer> maxDepthSpinner;
    private CheckBox includeExternalCheckBox;

    // Metrics bindings
    private final StringProperty totalPathsMetric = new SimpleStringProperty("0");
    private final StringProperty shortestPathMetric = new SimpleStringProperty("-");
    private final StringProperty longestPathMetric = new SimpleStringProperty("-");
    private final StringProperty avgPathMetric = new SimpleStringProperty("-");
    private final StringProperty connectedMetric = new SimpleStringProperty("No");
    private final StringProperty searchTimeMetric = new SimpleStringProperty("0 ms");

    // Consolidated Results state
    private static class ConsolidatedResult {
        PathQueryResult shortest;
        PathQueryResult alternatives;
        PathQueryResult chain;
        long totalSearchTimeMs;
    }

    private ConsolidatedResult consolidatedResult = null;
    private EntityPath selectedPath = null;
    private final StringProperty activeTab = new SimpleStringProperty("SHORTEST PATH");

    // UI containers
    private final VBox mainLayout = new VBox(16);
    private final HBox pathTabBar = new HBox(8);
    private final HBox visualizerFlow = new HBox(12);
    private final VBox breakdownRows = new VBox(4);
    private final PieChart donutChart = new PieChart();
    private final Label donutTotalLabel = new Label("0\nTotal");
    private final VBox legendBox = new VBox(6);

    // Loading overlay
    private final StackPane visualizerCard = new StackPane();
    private final StackPane loadingOverlay = new StackPane();
    private final Label loadingStatus = new Label("Ready");

    // Route Stats Grid
    private final VBox statsGrid = new VBox(8);

    private GridPane metricsGrid;
    private final StackPane selectorRowContainer = new StackPane();
    private final HBox selectorRowHBox = new HBox(16);
    private final VBox selectorRowVBox = new VBox(16);
    private VBox routeCard;
    private VBox optionsCard;
    private VBox statisticsCard;

    private final StackPane bottomRowContainer = new StackPane();
    private final HBox bottomRowHBox = new HBox(16);
    private final VBox bottomRowVBox = new VBox(16);
    private VBox tableCard;
    private VBox chartCard;
    private VBox actionsCard;

    public DependencyPathFinderDashboard(DependencyExplorerState state) {
        this.state = state;
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

        // Listen to context changes to retrieve SemanticGraphIndex
        state.analysisContextProperty().addListener((obs, old, context) -> {
            if (context != null) {
                graphIndex = context.getSemanticGraphIndex();
            } else {
                graphIndex = null;
            }
        });
        if (state.analysisContextProperty().get() != null) {
            graphIndex = state.analysisContextProperty().get().getSemanticGraphIndex();
        }

        // 1. Header Row
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        VBox titleBox = new VBox(4);
        Label title = new Label("Path Finder");
        title.setStyle("-fx-font-family: 'Epilogue', sans-serif; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dce3ec;");
        Label subtitle = new Label("Find the dependency path between any two entities in your codebase");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #849396;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search entity, namespace... (Ctrl + K)");
        searchField.setStyle("-fx-background-color: #151c23; -fx-text-fill: #dce3ec; -fx-prompt-text-fill: #849396; -fx-border-color: rgba(132, 147, 150, 0.15); -fx-border-radius: 6; -fx-background-radius: 6; -fx-pref-width: 320; -fx-padding: 6 12;");
        
        ContextMenu searchPopup = new ContextMenu();
        searchPopup.setStyle("-fx-background-color: #151c23; -fx-border-color: rgba(132, 147, 150, 0.15); -fx-border-radius: 6;");

        searchField.textProperty().addListener((obs, old, text) -> {
            if (text == null || text.isBlank()) {
                searchPopup.hide();
                return;
            }
            if (state.analysisContextProperty().get() == null) return;
            searchPopup.getItems().clear();
            String query = text.toLowerCase();
            List<EntityInfo> matches = state.analysisContextProperty().get().getEntities().stream()
                    .filter(e -> e.getEntityName().toLowerCase().contains(query)
                            || (e.getNamespaceName() != null && e.getNamespaceName().toLowerCase().contains(query)))
                    .limit(6)
                    .toList();

            for (EntityInfo match : matches) {
                MenuItem item = new MenuItem(match.getSimpleName() + " (" + match.getNamespaceName() + ")");
                item.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px;");
                item.setOnAction(ev -> {
                    if (fromEntity.get() == null) {
                        fromEntity.set(match);
                    } else {
                        toEntity.set(match);
                    }
                    searchField.clear();
                    searchPopup.hide();
                });
                searchPopup.getItems().add(item);
            }

            if (!searchPopup.getItems().isEmpty()) {
                if (!searchPopup.isShowing()) {
                    searchPopup.show(searchField, Side.BOTTOM, 0, 0);
                }
            } else {
                searchPopup.hide();
            }
        });

        header.getChildren().addAll(titleBox, spacer, searchField);

        // 2. Metrics Ribbon
        metricsGrid = new GridPane();
        metricsGrid.setHgap(12);
        metricsGrid.setVgap(12);

        // Columns Constraint will be set dynamically inside adaptLayout

        metricsGrid.add(createMetricTile("TOTAL PATHS FOUND", totalPathsMetric, "Between selected entities"), 0, 0);
        metricsGrid.add(createMetricTile("SHORTEST PATH LENGTH", shortestPathMetric, "Minimum hops"), 1, 0);
        metricsGrid.add(createMetricTile("DEEPEST CHAIN LENGTH", longestPathMetric, "SCC DAG path depth"), 2, 0);
        metricsGrid.add(createMetricTile("AVERAGE PATH LENGTH", avgPathMetric, "Across all paths"), 3, 0);
        metricsGrid.add(createMetricTile("CONNECTED", connectedMetric, "Entities are connected"), 4, 0);
        metricsGrid.add(createMetricTile("SEARCH TIME", searchTimeMetric, "Query execution time"), 5, 0);

        // 3. Route Selector Box & Options Row
        routeCard = new VBox(10);
        routeCard.getStyleClass().add("dd-card");
        HBox.setHgrow(routeCard, Priority.ALWAYS);

        Label routeTitle = new Label("FIND PATH BETWEEN");
        routeTitle.getStyleClass().add("dd-card-title");

        HBox selectContainer = new HBox(12);
        selectContainer.setAlignment(Pos.CENTER_LEFT);

        StackPane fromBox = new StackPane();
        HBox.setHgrow(fromBox, Priority.ALWAYS);
        setupSearchSelector(fromBox, fromEntity, "FROM (Select source entity...)");

        Button swapBtn = new Button("⇄");
        swapBtn.setStyle("-fx-background-color: #1a2027; -fx-text-fill: #00daf3; -fx-border-color: rgba(0, 218, 243, 0.2); -fx-border-radius: 6; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 8 12;");
        swapBtn.setOnAction(e -> {
            EntityInfo temp = fromEntity.get();
            fromEntity.set(toEntity.get());
            toEntity.set(temp);
        });

        StackPane toBox = new StackPane();
        HBox.setHgrow(toBox, Priority.ALWAYS);
        setupSearchSelector(toBox, toEntity, "TO (Select target entity...)");

        selectContainer.getChildren().addAll(fromBox, swapBtn, toBox);
        routeCard.getChildren().addAll(routeTitle, selectContainer);

        // Path Options Card
        optionsCard = new VBox(10);
        optionsCard.getStyleClass().add("dd-card");
        optionsCard.setMinWidth(250);

        Label optionsTitle = new Label("PATH OPTIONS");
        optionsTitle.getStyleClass().add("dd-card-title");

        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(8);
        optionsGrid.setVgap(8);

        Label lblType = new Label("Path Type");
        lblType.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
        pathTypeCombo = new ComboBox<>();
        pathTypeCombo.getStyleClass().add("dd-combobox");
        pathTypeCombo.getItems().addAll("Shortest Path Only", "Alternative Paths", "Deepest SCC Chain");
        pathTypeCombo.setValue("Shortest Path Only");
        pathTypeCombo.setMaxWidth(Double.MAX_VALUE);

        Label lblLen = new Label("Max Path Length");
        lblLen.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
        maxDepthSpinner = new Spinner<>(1, 50, 15);
        maxDepthSpinner.getStyleClass().add("dd-spinner");
        maxDepthSpinner.setMaxWidth(Double.MAX_VALUE);

        Label lblExt = new Label("Include External");
        lblExt.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
        includeExternalCheckBox = new CheckBox();
        includeExternalCheckBox.setSelected(true);
        includeExternalCheckBox.setStyle("-fx-text-fill: #dce3ec;");

        optionsGrid.add(lblType, 0, 0);
        optionsGrid.add(pathTypeCombo, 1, 0);
        optionsGrid.add(lblLen, 0, 1);
        optionsGrid.add(maxDepthSpinner, 1, 1);
        optionsGrid.add(lblExt, 0, 2);
        optionsGrid.add(includeExternalCheckBox, 1, 2);

        Button findPathsBtn = new Button("🔍 Find Paths");
        findPathsBtn.getStyleClass().add("dd-button-cyan");
        findPathsBtn.setMaxWidth(Double.MAX_VALUE);
        findPathsBtn.setOnAction(e -> calculatePaths());

        optionsCard.getChildren().addAll(optionsTitle, optionsGrid, findPathsBtn);

        // Path Statistics Card
        statisticsCard = new VBox(10);
        statisticsCard.getStyleClass().add("dd-card");
        statisticsCard.setMinWidth(260);

        Label statsTitle = new Label("PATH STATISTICS");
        statsTitle.getStyleClass().add("dd-card-title");

        statsGrid.setStyle("-fx-background-color: transparent;");
        updateStatsGrid(0, 0, 0, 0, 0, 0, 0);

        statisticsCard.getChildren().addAll(statsTitle, statsGrid);

        // 4. Path Visualizer Card
        visualizerCard.getStyleClass().add("dd-card");
        
        VBox visualizerLayout = new VBox(14);
        
        HBox tabHeader = new HBox(12);
        tabHeader.setAlignment(Pos.CENTER_LEFT);
        
        pathTabBar.setAlignment(Pos.CENTER_LEFT);
        
        Region tabSpacer = new Region();
        HBox.setHgrow(tabSpacer, Priority.ALWAYS);
        
        Button exportBtn = createActionBtn("Export Path", "download");
        Button copyBtn = createActionBtn("Copy Path", "content_copy");
        
        tabHeader.getChildren().addAll(pathTabBar, tabSpacer, exportBtn, copyBtn);

        // Flow Pipeline container
        ScrollPane visualizerScroll = new ScrollPane(visualizerFlow);
        visualizerScroll.setFitToHeight(true);
        visualizerScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
        visualizerScroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        visualizerScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        
        visualizerFlow.setAlignment(Pos.CENTER_LEFT);
        visualizerFlow.setPadding(new Insets(10, 0, 10, 0));

        // Pipeline Metrics Bar
        HBox visualizerMetricsBar = new HBox(24);
        visualizerMetricsBar.setStyle("-fx-background-color: #1a2027; -fx-padding: 8 16; -fx-background-radius: 6;");
        visualizerMetricsBar.setAlignment(Pos.CENTER_LEFT);

        Label pathLenLbl = new Label("Path Length: - hops");
        pathLenLbl.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px;");
        Label totalRefsLbl = new Label("Total References: -");
        totalRefsLbl.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px;");
        Label typesLbl = new Label("Types Involved: -");
        typesLbl.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px;");
        
        Region metSpacer = new Region();
        HBox.setHgrow(metSpacer, Priority.ALWAYS);
        
        Label impactBadge = new Label("Path Impact: -");
        impactBadge.setStyle("-fx-font-size: 9px; -fx-text-fill: #849396; -fx-background-color: rgba(132, 147, 150, 0.1); -fx-padding: 2 6; -fx-background-radius: 4;");

        visualizerMetricsBar.getChildren().addAll(pathLenLbl, totalRefsLbl, typesLbl, metSpacer, impactBadge);

        visualizerLayout.getChildren().addAll(tabHeader, visualizerScroll, visualizerMetricsBar);

        // Loading spinner overlay
        loadingOverlay.setStyle("-fx-background-color: rgba(21, 28, 35, 0.85); -fx-background-radius: 12;");
        loadingOverlay.setVisible(false);
        VBox loadingBox = new VBox(12);
        loadingBox.setAlignment(Pos.CENTER);
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #00daf3;");
        spinner.setMaxSize(40, 40);
        
        loadingStatus.setStyle("-fx-text-fill: #00daf3; -fx-font-size: 12px; -fx-font-family: 'JetBrains Mono';");
        loadingBox.getChildren().addAll(spinner, loadingStatus);
        loadingOverlay.getChildren().add(loadingBox);

        visualizerCard.getChildren().addAll(visualizerLayout, loadingOverlay);

        // 5. Bottom Row: Table, Donut Chart, Quick Actions
        tableCard = new VBox(10);
        tableCard.getStyleClass().add("dd-card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        Label tableTitle = new Label("DETAILED PATH BREAKDOWN");
        tableTitle.getStyleClass().add("dd-card-title");

        ScrollPane tableScroll = new ScrollPane(breakdownRows);
        tableScroll.setFitToWidth(true);
        tableScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(tableScroll, Priority.ALWAYS);
        
        breakdownRows.setStyle("-fx-background-color: transparent;");
        clearBreakdownTable();

        tableCard.getChildren().addAll(tableTitle, tableScroll);

        // Donut Chart Card (Center-Right)
        chartCard = new VBox(10);
        chartCard.getStyleClass().add("dd-card");
        chartCard.setPrefWidth(300);
        chartCard.setMinWidth(300);

        Label chartTitle = new Label("REFERENCE TYPE DISTRIBUTION");
        chartTitle.getStyleClass().add("dd-card-title");

        StackPane chartStack = new StackPane();
        chartStack.setPrefSize(200, 200);

        donutChart.setLegendVisible(false);
        donutChart.setLabelsVisible(false);
        donutChart.setStartAngle(90);

        // Central cutout
        StackPane centerHole = new StackPane();
        centerHole.setStyle("-fx-background-color: #151c23; -fx-background-radius: 100;");
        centerHole.setMaxSize(90, 90);
        
        donutTotalLabel.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 13px; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center;");
        centerHole.getChildren().add(donutTotalLabel);
        chartStack.getChildren().addAll(donutChart, centerHole);

        legendBox.setPadding(new Insets(10, 0, 0, 0));
        legendBox.setAlignment(Pos.CENTER);

        chartCard.getChildren().addAll(chartTitle, chartStack, legendBox);

        // Quick Actions Card (Far-Right)
        actionsCard = new VBox(12);
        actionsCard.getStyleClass().add("dd-card");
        actionsCard.setMinWidth(260);

        Label actionsTitle = new Label("QUICK ACTIONS");
        actionsTitle.getStyleClass().add("dd-card-title");

        VBox actionList = new VBox(8);
        actionList.getChildren().addAll(
                createQuickActionRow("Show in Dependency Graph", "account_tree"),
                createQuickActionRow("Show in Matrix View", "grid_on"),
                createQuickActionRow("Analyze Path Impact", "local_fire_department"),
                createQuickActionRow("Find Reverse Path", "swap_horiz"),
                createQuickActionRow("Compare with Another Path", "difference")
        );

        actionsCard.getChildren().addAll(actionsTitle, actionList);

        selectorRowHBox.setAlignment(Pos.TOP_LEFT);
        selectorRowVBox.setAlignment(Pos.TOP_LEFT);
        bottomRowHBox.setAlignment(Pos.TOP_LEFT);
        bottomRowVBox.setAlignment(Pos.TOP_LEFT);

        mainLayout.getChildren().addAll(header, metricsGrid, selectorRowContainer, visualizerCard, bottomRowContainer);

        // Adapt Layout initially
        adaptLayout(1400);

        widthProperty().addListener((obs, oldVal, newVal) -> {
            adaptLayout(newVal.doubleValue());
        });

        // Setup activeTab change listener
        activeTab.addListener((obs, old, tab) -> renderSelectedTab());

        // Initialize tab bar
        updatePathTabBar();
    }

    private HBox createMetricTile(String labelStr, StringProperty valProp, String subtextStr) {
        HBox tile = new HBox(8);
        tile.getStyleClass().add("dd-stat-tile");
        tile.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(2);
        Label lbl = new Label(labelStr);
        lbl.getStyleClass().add("dd-stat-label");

        Label val = new Label();
        val.textProperty().bind(valProp);
        val.getStyleClass().add("dd-stat-value");

        Label sub = new Label(subtextStr);
        sub.setStyle("-fx-font-size: 9px; -fx-text-fill: #849396;");

        text.getChildren().addAll(lbl, val, sub);
        tile.getChildren().add(text);
        return tile;
    }

    private void setupSearchSelector(StackPane box, ObjectProperty<EntityInfo> property, String placeholderText) {
        box.setStyle("-fx-background-color: #1a2027; -fx-border-color: rgba(132, 147, 150, 0.15); -fx-border-radius: 6; -fx-background-radius: 6; -fx-min-height: 50;");
        
        TextField search = new TextField();
        search.setPromptText(placeholderText);
        search.setStyle("-fx-background-color: transparent; -fx-text-fill: #dce3ec; -fx-prompt-text-fill: #849396; -fx-padding: 10; -fx-border-color: transparent;");
        
        ContextMenu pop = new ContextMenu();
        pop.setStyle("-fx-background-color: #151c23; -fx-border-color: rgba(132, 147, 150, 0.15); -fx-border-radius: 6;");

        search.textProperty().addListener((obs, old, text) -> {
            if (text == null || text.isBlank()) {
                pop.hide();
                return;
            }
            if (state.analysisContextProperty().get() == null) return;
            pop.getItems().clear();
            String query = text.toLowerCase();
            List<EntityInfo> matches = state.analysisContextProperty().get().getEntities().stream()
                    .filter(e -> e.getEntityName().toLowerCase().contains(query)
                            || (e.getNamespaceName() != null && e.getNamespaceName().toLowerCase().contains(query)))
                    .limit(6)
                    .toList();

            for (EntityInfo match : matches) {
                MenuItem mi = new MenuItem(match.getSimpleName() + " (" + match.getNamespaceName() + ")");
                mi.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px;");
                mi.setOnAction(ev -> {
                    property.set(match);
                    pop.hide();
                });
                pop.getItems().add(mi);
            }

            if (!pop.getItems().isEmpty()) {
                if (!pop.isShowing()) {
                    pop.show(search, Side.BOTTOM, 0, 0);
                }
            } else {
                pop.hide();
            }
        });

        property.addListener((obs, oldVal, newVal) -> {
            box.getChildren().clear();
            if (newVal == null) {
                search.clear();
                box.getChildren().add(search);
            } else {
                HBox chip = new HBox(8);
                chip.setPadding(new Insets(8, 12, 8, 12));
                chip.setAlignment(Pos.CENTER_LEFT);

                Label icon = new Label("deployed_code");
                icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-text-fill: #00daf3; -fx-font-size: 16px;");

                VBox desc = new VBox(1);
                Label name = new Label(newVal.getSimpleName());
                name.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 12px; -fx-font-weight: bold;");
                Label pkg = new Label(newVal.getNamespaceName());
                pkg.setStyle("-fx-text-fill: #849396; -fx-font-size: 9px;");
                desc.getChildren().addAll(name, pkg);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Button clear = new Button("×");
                clear.setStyle("-fx-background-color: transparent; -fx-text-fill: #849396; -fx-font-size: 16px; -fx-padding: 0 4; -fx-cursor: hand;");
                clear.setOnAction(e -> property.set(null));

                chip.getChildren().addAll(icon, desc, sp, clear);
                box.getChildren().add(chip);
            }
        });

        box.getChildren().add(search);
    }

    private Button createActionBtn(String text, String iconName) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #1a2027; -fx-text-fill: #bac9cc; -fx-border-color: rgba(132, 147, 150, 0.15); -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px;");
        
        Label icon = new Label(iconName);
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 13px;");
        btn.setGraphic(icon);
        return btn;
    }

    private HBox createQuickActionRow(String labelText, String iconSymbol) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 8, 6, 8));
        row.setStyle("-fx-background-color: #1a2027; -fx-background-radius: 6; -fx-cursor: hand;");

        Label icon = new Label(iconSymbol);
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-text-fill: #849396; -fx-font-size: 15px;");

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px; -fx-font-weight: bold;");

        row.getChildren().addAll(icon, label);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(0, 218, 243, 0.08); -fx-background-radius: 6; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #1a2027; -fx-background-radius: 6; -fx-cursor: hand;"));

        if (labelText.equals("Find Reverse Path")) {
            row.setOnMouseClicked(e -> {
                EntityInfo temp = fromEntity.get();
                fromEntity.set(toEntity.get());
                toEntity.set(temp);
                calculatePaths();
            });
        }

        return row;
    }

    private void updateStatsGrid(int traversedEnts, int traversedRefs, int circulars, int externals, int shortestLen, int longestLen, double avgLen) {
        statsGrid.getChildren().clear();
        statsGrid.getChildren().addAll(
                createStatRow("Total Entities Traversed", String.valueOf(traversedEnts)),
                createStatRow("Total References Traversed", String.valueOf(traversedRefs)),
                createStatRow("Shortest Path Length", shortestLen > 0 ? shortestLen + " hops" : "-"),
                createStatRow("Deepest Chain Length", longestLen > 0 ? longestLen + " hops" : "-"),
                createStatRow("Average Path Length", avgLen > 0 ? String.format("%.2f", avgLen) : "-"),
                createStatRow("Circular Dependencies in Path", String.valueOf(circulars)),
                createStatRow("External Dependencies", String.valueOf(externals))
        );
    }

    private HBox createStatRow(String key, String val) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label keyLbl = new Label(key);
        keyLbl.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
        
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        
        Label valLbl = new Label(val);
        valLbl.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
        
        row.getChildren().addAll(keyLbl, sp, valLbl);
        return row;
    }

    /**
     * Executes the Pathfinding computations on a background thread.
     */
    private void calculatePaths() {
        if (graphIndex == null) return;
        if (fromEntity.get() == null || toEntity.get() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select both FROM and TO entities first.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String fromQName = fromEntity.get().getEntityName();
        String toQName = toEntity.get().getEntityName();
        int maxDepth = maxDepthSpinner.getValue();

        // 1. Show Loading Overlay
        loadingStatus.textProperty().unbind();
        loadingStatus.setText("Traversing dependency graph...");
        loadingOverlay.setVisible(true);

        // 2. Instantiate Background Task
        Task<ConsolidatedResult> task = new Task<>() {
            @Override
            protected ConsolidatedResult call() throws Exception {
                long totalStart = System.currentTimeMillis();
                ConsolidatedResult res = new ConsolidatedResult();

                // Shortest Path (Bidirectional BFS)
                updateMessage("Calculating shortest path (Bidirectional BFS)...");
                res.shortest = queryEngine.queryShortestPath(graphIndex, state.analysisContextProperty().get(), fromQName, toQName);

                // Alternative Paths (Bounded BFS)
                updateMessage("Calculating alternative paths (lazy bounded BFS)...");
                res.alternatives = queryEngine.queryAlternativePaths(graphIndex, state.analysisContextProperty().get(), fromQName, toQName, maxDepth, 5);

                // Deepest SCC Chain (SCC Condensation Topological Sort)
                updateMessage("Analysing strongly connected components DAG...");
                res.chain = queryEngine.queryDependencyChain(graphIndex, state.analysisContextProperty().get(), fromQName);

                res.totalSearchTimeMs = System.currentTimeMillis() - totalStart;
                updateMessage("Done");
                return res;
            }
        };

        // Bind message property to UI status
        loadingStatus.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                consolidatedResult = task.getValue();
                
                // Unbind & Hide Loading State
                loadingStatus.textProperty().unbind();
                loadingOverlay.setVisible(false);

                // Populate stats on UI
                populateDashboardResults();
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                loadingStatus.textProperty().unbind();
                loadingOverlay.setVisible(false);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Path query failed: " + task.getException().getMessage(), ButtonType.OK);
                alert.showAndWait();
            });
        });

        // Run Task on separate daemon thread
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void populateDashboardResults() {
        if (consolidatedResult == null) return;

        int totalPaths = consolidatedResult.shortest.getPaths().size() + consolidatedResult.alternatives.getPaths().size();
        totalPathsMetric.set(String.valueOf(totalPaths));
        searchTimeMetric.set(consolidatedResult.totalSearchTimeMs + " ms");
        connectedMetric.set(consolidatedResult.shortest.isConnected() ? "Yes" : "No");

        int shortestHops = consolidatedResult.shortest.getPaths().isEmpty() ? 0 : consolidatedResult.shortest.getPaths().get(0).getHops();
        int longestHops = consolidatedResult.chain.getPaths().isEmpty() ? 0 : consolidatedResult.chain.getPaths().get(0).getHops();
        
        shortestPathMetric.set(shortestHops > 0 ? String.valueOf(shortestHops) : "-");
        longestPathMetric.set(longestHops > 0 ? String.valueOf(longestHops) : "-");

        double avgLen = 0;
        int totalHops = 0;
        int totalPathCount = consolidatedResult.alternatives.getPaths().size();
        for (EntityPath p : consolidatedResult.alternatives.getPaths()) {
            totalHops += p.getHops();
        }
        if (totalPathCount > 0) {
            avgLen = (double) totalHops / totalPathCount;
            avgPathMetric.set(String.format("%.2f", avgLen));
        } else {
            avgPathMetric.set("-");
        }

        // Update statistics card
        updateStatsGrid(
                consolidatedResult.shortest.getTotalEntitiesTraversed() + consolidatedResult.alternatives.getTotalEntitiesTraversed(),
                consolidatedResult.shortest.getTotalReferencesTraversed() + consolidatedResult.alternatives.getTotalReferencesTraversed(),
                consolidatedResult.shortest.getCircularCount(),
                consolidatedResult.shortest.getExternalCount() + consolidatedResult.alternatives.getExternalCount(),
                shortestHops,
                longestHops,
                avgLen
        );

        // Update Tab buttons
        updatePathTabBar();

        // Render tab contents
        activeTab.set("SHORTEST PATH");
        renderSelectedTab();
    }

    private void updatePathTabBar() {
        pathTabBar.getChildren().clear();

        int nShort = consolidatedResult == null ? 0 : consolidatedResult.shortest.getPaths().size();
        int nAlt = consolidatedResult == null ? 0 : consolidatedResult.alternatives.getPaths().size();
        int nChain = consolidatedResult == null ? 0 : consolidatedResult.chain.getPaths().size();

        pathTabBar.getChildren().addAll(
                createTabButton("SHORTEST PATH", nShort),
                createTabButton("ALTERNATIVE PATHS", nAlt),
                createTabButton("DEPENDENCY CHAIN", nChain)
        );
    }

    private Button createTabButton(String tabName, int count) {
        String labelText = tabName + " (" + count + ")";
        if (tabName.equals("SHORTEST PATH") || tabName.equals("DEPENDENCY CHAIN")) {
            labelText = tabName;
        }

        Button btn = new Button(labelText);
        btn.getStyleClass().add("dd-tab-button");

        boolean isActive = activeTab.get().equals(tabName);
        if (isActive) {
            btn.getStyleClass().add("dd-tab-button-active");
        }

        btn.setOnAction(e -> activeTab.set(tabName));
        return btn;
    }

    private void renderSelectedTab() {
        // Sync active tab classes
        updatePathTabBar();

        visualizerFlow.getChildren().clear();
        clearBreakdownTable();

        if (consolidatedResult == null) return;

        List<EntityPath> list = switch (activeTab.get()) {
            case "SHORTEST PATH" -> consolidatedResult.shortest.getPaths();
            case "ALTERNATIVE PATHS" -> consolidatedResult.alternatives.getPaths();
            case "DEPENDENCY CHAIN" -> consolidatedResult.chain.getPaths();
            default -> Collections.emptyList();
        };

        if (list.isEmpty()) {
            Label empty = new Label("No paths found for this category");
            empty.setStyle("-fx-text-fill: #849396; -fx-font-size: 12px; -fx-font-family: 'JetBrains Mono';");
            visualizerFlow.getChildren().add(empty);
            return;
        }

        EntityPath rawPath = list.get(0); // get first path node route

        // Lazily resolve connection details for the selected path
        if (state.analysisContextProperty().get() != null) {
            List<PathConnection> resolvedConnections = PathQueryEngine.resolvePathConnections(rawPath, state.analysisContextProperty().get());
            selectedPath = new EntityPath(rawPath.getEntities(), resolvedConnections);
        } else {
            selectedPath = rawPath;
        }

        renderPathFlowchart(selectedPath);
        renderBreakdownTable(selectedPath);
        buildDonutDistributionChart();
    }

    private void renderPathFlowchart(EntityPath path) {
        visualizerFlow.getChildren().clear();

        List<String> nodes = path.getEntities();
        int totalNodes = nodes.size();
        
        for (int i = 0; i < totalNodes; i++) {
            if (totalNodes > 100 && i == 50) {
                Label truncateLabel = new Label("... (+ " + (totalNodes - 100) + " more nodes) ...");
                truncateLabel.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 0 10;");
                visualizerFlow.getChildren().add(truncateLabel);
                
                Label arrow = new Label("→");
                arrow.getStyleClass().add("dd-path-arrow");
                visualizerFlow.getChildren().add(arrow);
                
                i = totalNodes - 51; // Next iteration will be totalNodes - 50
                continue;
            }

            String nodeName = nodes.get(i);
            int lastDot = nodeName.lastIndexOf('.');
            String simple = lastDot == -1 ? nodeName : nodeName.substring(lastDot + 1);
            String pkg = lastDot == -1 ? "" : nodeName.substring(0, lastDot);

            VBox nodeCard = new VBox(2);
            nodeCard.getStyleClass().add("dd-path-node-card");
            
            HBox top = new HBox(6);
            top.setAlignment(Pos.CENTER_LEFT);
            Label lblName = new Label(simple);
            lblName.setStyle("-fx-text-fill: #dce3ec; -fx-font-weight: bold; -fx-font-size: 11px;");
            
            Label kindChip = new Label("CLASS");
            kindChip.setStyle("-fx-font-size: 7px; -fx-text-fill: #00daf3; -fx-background-color: rgba(0, 218, 243, 0.1); -fx-padding: 1 3; -fx-background-radius: 3;");
            top.getChildren().addAll(lblName, kindChip);

            Label lblPkg = new Label(pkg);
            lblPkg.setStyle("-fx-text-fill: #849396; -fx-font-size: 8px;");

            nodeCard.getChildren().addAll(top, lblPkg);
            visualizerFlow.getChildren().add(nodeCard);

            if (i < totalNodes - 1) {
                Label arrow = new Label("→");
                arrow.getStyleClass().add("dd-path-arrow");
                visualizerFlow.getChildren().add(arrow);
            }
        }

        // Update pipeline metrics bar
        HBox metricsBar = (HBox) ((VBox) visualizerCard.getChildren().get(0)).getChildren().get(2);
        ((Label) metricsBar.getChildren().get(0)).setText("Path Length: " + path.getHops() + " hops");

        int totalRefs = path.getConnections().stream().mapToInt(PathConnection::count).sum();
        ((Label) metricsBar.getChildren().get(1)).setText("Total References: " + totalRefs);

        Set<String> uniqueTypes = new HashSet<>();
        path.getConnections().forEach(c -> uniqueTypes.add(c.referenceType()));
        ((Label) metricsBar.getChildren().get(2)).setText("Types Involved: " + uniqueTypes.size());

        Label badge = (Label) metricsBar.getChildren().get(4);
        if (path.getHops() <= 4) {
            badge.setText("Path Impact: Low");
            badge.getStyleClass().setAll("dd-badge-green");
        } else {
            badge.setText("Path Impact: High");
            badge.getStyleClass().setAll("dd-badge-red");
        }
    }

    private void renderBreakdownTable(EntityPath path) {
        breakdownRows.getChildren().clear();

        // Header Row
        HBox header = new HBox(8);
        header.setStyle("-fx-background-color: #1a2027; -fx-padding: 6;");
        header.getChildren().addAll(
                createHeaderCell("#", 30),
                createHeaderCell("FROM", 200),
                createHeaderCell("TO", 200),
                createHeaderCell("REFERENCE TYPE", 140),
                createHeaderCell("REFERENCES", 80),
                createHeaderCell("DETAILS", 300)
        );
        breakdownRows.getChildren().add(header);

        // Data Rows
        List<PathConnection> conns = path.getConnections();
        int totalConns = conns.size();
        for (int i = 0; i < totalConns; i++) {
            if (totalConns > 100 && i == 50) {
                HBox truncRow = new HBox();
                truncRow.setStyle("-fx-padding: 10; -fx-alignment: center;");
                Label truncLabel = new Label("... (+ " + (totalConns - 100) + " more connections hidden to conserve memory) ...");
                truncLabel.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px; -fx-font-style: italic;");
                truncRow.getChildren().add(truncLabel);
                breakdownRows.getChildren().add(truncRow);
                i = totalConns - 51;
                continue;
            }

            PathConnection c = conns.get(i);
            HBox row = new HBox(8);
            row.getStyleClass().add("dd-table-row");

            Label num = new Label(String.valueOf(i + 1));
            num.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px;");
            num.setPrefWidth(30);

            // FROM column
            int lastDotF = c.from().lastIndexOf('.');
            String simpleF = lastDotF == -1 ? c.from() : c.from().substring(lastDotF + 1);
            VBox fromBox = new VBox(1);
            fromBox.setPrefWidth(200);
            Label nameF = new Label(simpleF);
            nameF.setStyle("-fx-text-fill: #dce3ec; -fx-font-weight: bold; -fx-font-size: 11px;");
            Label pkgF = new Label(lastDotF == -1 ? "" : c.from().substring(0, lastDotF));
            pkgF.setStyle("-fx-text-fill: #849396; -fx-font-size: 8px;");
            fromBox.getChildren().addAll(nameF, pkgF);

            // TO column
            int lastDotT = c.to().lastIndexOf('.');
            String simpleT = lastDotT == -1 ? c.to() : c.to().substring(lastDotT + 1);
            VBox toBox = new VBox(1);
            toBox.setPrefWidth(200);
            Label nameT = new Label(simpleT);
            nameT.setStyle("-fx-text-fill: #dce3ec; -fx-font-weight: bold; -fx-font-size: 11px;");
            Label pkgT = new Label(lastDotT == -1 ? "" : c.to().substring(0, lastDotT));
            pkgT.setStyle("-fx-text-fill: #849396; -fx-font-size: 8px;");
            toBox.getChildren().addAll(nameT, pkgT);

            // REFERENCE TYPE column (with indicator dot)
            HBox refBox = new HBox(6);
            refBox.setAlignment(Pos.CENTER_LEFT);
            refBox.setPrefWidth(140);
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + resolveColor(c.referenceType()) + "; -fx-font-size: 10px;");
            Label type = new Label(c.referenceType());
            type.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 10px;");
            refBox.getChildren().addAll(dot, type);

            // REFERENCES count column
            Label count = new Label(String.valueOf(c.count()));
            count.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
            count.setPrefWidth(80);

            // DETAILS column
            Label details = new Label(c.details());
            details.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px;");
            details.setPrefWidth(300);

            row.getChildren().addAll(num, fromBox, toBox, refBox, count, details);
            breakdownRows.getChildren().add(row);
        }
    }

    private Label createHeaderCell(String text, double width) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #849396; -fx-font-size: 9px; -fx-font-weight: bold;");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private void clearBreakdownTable() {
        breakdownRows.getChildren().clear();
        Label empty = new Label("Run a pathfinding search to view detailed breakdowns.");
        empty.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px; -fx-padding: 20; -fx-alignment: center;");
        breakdownRows.getChildren().add(empty);
    }

    private String resolveColor(String type) {
        return switch (type.toUpperCase()) {
            case "CALL" -> "#00daf3";
            case "MEMBER" -> "#00e676";
            case "TYPE" -> "#ffa726";
            case "ANNOTATION" -> "#ec407a";
            case "FRAMEWORK" -> "#4bf6ff";
            default -> "#849396";
        };
    }

    private void buildDonutDistributionChart() {
        if (selectedPath == null) return;

        Map<String, Integer> counts = new HashMap<>();
        int total = 0;
        for (PathConnection c : selectedPath.getConnections()) {
            counts.put(c.referenceType(), counts.getOrDefault(c.referenceType(), 0) + c.count());
            total += c.count();
        }

        donutTotalLabel.setText(total + "\nTotal");

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        legendBox.getChildren().clear();

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String kind = entry.getKey();
            int count = entry.getValue();
            double pct = total > 0 ? (count * 100.0) / total : 0.0;

            PieChart.Data data = new PieChart.Data(kind, count);
            pieData.add(data);

            // Add Legend Row
            HBox legendRow = new HBox(8);
            legendRow.setAlignment(Pos.CENTER_LEFT);
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + resolveColor(kind) + "; -fx-font-size: 11px;");
            Label lbl = new Label(kind + ": " + count + " (" + String.format("%.1f", pct) + "%)");
            lbl.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px;");
            legendRow.getChildren().addAll(dot, lbl);
            legendBox.getChildren().add(legendRow);
        }

        donutChart.setData(pieData);

        // Apply colors to pie slices dynamically
        for (PieChart.Data data : pieData) {
            String color = resolveColor(data.getName());
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }
    }

    public void setPathQuery(EntityInfo from, EntityInfo to) {
        fromEntity.set(from);
        toEntity.set(to);
        if (from != null && to != null) {
            calculatePaths();
        }
    }

    private void adaptLayout(double width) {
        // --- 1. Metrics Grid Responsiveness ---
        metricsGrid.getChildren().clear();
        metricsGrid.getColumnConstraints().clear();
        metricsGrid.getRowConstraints().clear();

        Node card1 = createMetricTile("TOTAL PATHS FOUND", totalPathsMetric, "Between selected entities");
        Node card2 = createMetricTile("SHORTEST PATH LENGTH", shortestPathMetric, "Minimum hops");
        Node card3 = createMetricTile("DEEPEST CHAIN LENGTH", longestPathMetric, "SCC DAG path depth");
        Node card4 = createMetricTile("AVERAGE PATH LENGTH", avgPathMetric, "Across all paths");
        Node card5 = createMetricTile("CONNECTED", connectedMetric, "Entities are connected");
        Node card6 = createMetricTile("SEARCH TIME", searchTimeMetric, "Query execution time");

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

        // --- 2. Selector Row Responsiveness ---
        if (width > 1150) {
            // Wide Mode: side-by-side HBox
            if (selectorRowHBox.getChildren().isEmpty()) {
                selectorRowVBox.getChildren().clear();
                
                routeCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(routeCard, Priority.ALWAYS);
                optionsCard.setMinWidth(250); HBox.setHgrow(optionsCard, Priority.NEVER);
                statisticsCard.setMinWidth(260); HBox.setHgrow(statisticsCard, Priority.NEVER);

                selectorRowHBox.getChildren().addAll(routeCard, optionsCard, statisticsCard);
            }
            if (selectorRowContainer.getChildren().isEmpty() || selectorRowContainer.getChildren().get(0) != selectorRowHBox) {
                selectorRowContainer.getChildren().clear();
                selectorRowContainer.getChildren().add(selectorRowHBox);
            }
        } else if (width > 850) {
            // Medium Mode: routeCard side-by-side with VBox(optionsCard + statisticsCard)
            selectorRowHBox.getChildren().clear();
            selectorRowVBox.getChildren().clear();

            VBox optionsStatsVBox = new VBox(16);
            HBox.setHgrow(optionsStatsVBox, Priority.NEVER);
            optionsStatsVBox.setPrefWidth(280);
            
            routeCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(routeCard, Priority.ALWAYS);
            
            optionsCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(optionsCard, Priority.ALWAYS);
            statisticsCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(statisticsCard, Priority.ALWAYS);
            optionsStatsVBox.getChildren().addAll(optionsCard, statisticsCard);

            selectorRowHBox.getChildren().addAll(routeCard, optionsStatsVBox);
            selectorRowContainer.getChildren().clear();
            selectorRowContainer.getChildren().add(selectorRowHBox);
        } else {
            // Narrow Mode: routeCard, optionsCard, statisticsCard stacked vertically
            if (selectorRowVBox.getChildren().isEmpty()) {
                selectorRowHBox.getChildren().clear();

                routeCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(routeCard, Priority.ALWAYS);
                optionsCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(optionsCard, Priority.ALWAYS);
                statisticsCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(statisticsCard, Priority.ALWAYS);

                selectorRowVBox.getChildren().addAll(routeCard, optionsCard, statisticsCard);
            }
            if (selectorRowContainer.getChildren().isEmpty() || selectorRowContainer.getChildren().get(0) != selectorRowVBox) {
                selectorRowContainer.getChildren().clear();
                selectorRowContainer.getChildren().add(selectorRowVBox);
            }
        }

        // --- 3. Bottom Row Responsiveness ---
        if (width > 1150) {
            // Wide Mode: side-by-side HBox
            bottomRowContainer.setMinHeight(300);
            bottomRowContainer.setPrefHeight(300);
            if (bottomRowHBox.getChildren().isEmpty()) {
                bottomRowVBox.getChildren().clear();
                
                tableCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(tableCard, Priority.ALWAYS);
                chartCard.setMinWidth(300); HBox.setHgrow(chartCard, Priority.NEVER);
                actionsCard.setMinWidth(260); HBox.setHgrow(actionsCard, Priority.NEVER);

                bottomRowHBox.getChildren().addAll(tableCard, chartCard, actionsCard);
            }
            if (bottomRowContainer.getChildren().isEmpty() || bottomRowContainer.getChildren().get(0) != bottomRowHBox) {
                bottomRowContainer.getChildren().clear();
                bottomRowContainer.getChildren().add(bottomRowHBox);
            }
        } else if (width > 850) {
            // Medium Mode: tableCard side-by-side with VBox(chartCard + actionsCard)
            bottomRowContainer.setMinHeight(Region.USE_COMPUTED_SIZE);
            bottomRowContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
            bottomRowHBox.getChildren().clear();
            bottomRowVBox.getChildren().clear();

            VBox chartActionsVBox = new VBox(16);
            HBox.setHgrow(chartActionsVBox, Priority.NEVER);
            chartActionsVBox.setPrefWidth(280);
            
            tableCard.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(tableCard, Priority.ALWAYS);
            
            chartCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(chartCard, Priority.ALWAYS);
            actionsCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(actionsCard, Priority.ALWAYS);
            chartActionsVBox.getChildren().addAll(chartCard, actionsCard);

            bottomRowHBox.getChildren().addAll(tableCard, chartActionsVBox);
            bottomRowContainer.getChildren().clear();
            bottomRowContainer.getChildren().add(bottomRowHBox);
        } else {
            // Narrow Mode: tableCard, chartCard, actionsCard stacked vertically
            bottomRowContainer.setMinHeight(Region.USE_COMPUTED_SIZE);
            bottomRowContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
            if (bottomRowVBox.getChildren().isEmpty()) {
                bottomRowHBox.getChildren().clear();

                tableCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(tableCard, Priority.ALWAYS);
                chartCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(chartCard, Priority.ALWAYS);
                actionsCard.setMaxWidth(Double.MAX_VALUE); VBox.setVgrow(actionsCard, Priority.ALWAYS);

                bottomRowVBox.getChildren().addAll(tableCard, chartCard, actionsCard);
            }
            if (bottomRowContainer.getChildren().isEmpty() || bottomRowContainer.getChildren().get(0) != bottomRowVBox) {
                bottomRowContainer.getChildren().clear();
                bottomRowContainer.getChildren().add(bottomRowVBox);
            }
        }
    }
}
