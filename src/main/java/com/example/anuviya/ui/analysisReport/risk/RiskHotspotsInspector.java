package com.example.anuviya.ui.analysisReport.risk;

import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import com.example.anuviya.ui.analysisReport.state.RiskHotspotsState;
import com.example.anuviya.ui.analysisReport.state.RiskHotspotsState.InspectorEntry;
import com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.text.NumberFormat;
import java.util.Comparator;

public class RiskHotspotsInspector extends AnalysisInspectorContent {

    private RiskHotspotsState state;
    private final ObservableList<InspectorEntry> masterData = FXCollections.observableArrayList();
    private final FilteredList<InspectorEntry> filteredData = new FilteredList<>(masterData);
    private final SortedList<InspectorEntry> sortedData = new SortedList<>(filteredData);

    // Sidebar Filter Elements
    private final CheckBox cbAllHotspots = new CheckBox("All Hotspots");
    private final CheckBox cbOversized = new CheckBox("Oversized Entities");
    private final CheckBox cbHighlyInstable = new CheckBox("Highly Instable");
    private final CheckBox cbHighFanIn = new CheckBox("High Fan-In");
    private final CheckBox cbCircularDep = new CheckBox("Circular Dependencies");
    private final CheckBox cbHighFanOut = new CheckBox("High Fan-Out");

    private final CheckBox cbCritical = new CheckBox("Critical");
    private final CheckBox cbHigh = new CheckBox("High");
    private final CheckBox cbMedium = new CheckBox("Medium");
    private final CheckBox cbLow = new CheckBox("Low");

    private final Slider sliderInstability = new Slider(0.0, 1.0, 1.0);
    private final Slider sliderLoc = new Slider(0, 10000, 10000);

    // Top Overview Metrics Labels
    private final Label lblTotalHotspots = new Label("0");
    private final Label lblTotalHotspotsSub = new Label("0% of total entities");
    private final Label lblCriticalRisk = new Label("0");
    private final Label lblCriticalRiskSub = new Label("0% of hotspots");
    private final Label lblAvgInstability = new Label("0.00");
    private final Label lblLargestEntity = new Label("0 LOC");
    private final Label lblLargestEntityName = new Label("N/A");
    private final Label lblHighestFanIn = new Label("0");
    private final Label lblHighestFanInName = new Label("N/A");

    // Search and Sort
    private final TextField txtSearch = new TextField();
    private final ComboBox<String> cmbSort = new ComboBox<>();
    private final Button btnSortToggle = new Button("↓");
    private boolean sortAscending = false;

    // Pagination State
    private static final int PAGE_SIZE = 8;
    private int currentPage = 0;

    // Card List container
    private final VBox cardsContainer = new VBox(10);
    private VBox selectedRowNode = null;
    private InspectorEntry selectedEntry = null;

    // Footer Pagination Panel
    private final Label lblShowing = new Label("Showing 0 to 0 of 0 hotspots");
    private final HBox paginationBox = new HBox(6);

    // Bottom Selected Detail columns
    private final VBox colDetail1 = new VBox(12);
    private final VBox colDetail2 = new VBox(12);
    private final VBox colDetail3 = new VBox(12);
    private final VBox colDetail4 = new VBox(12);
    private final HBox detailsPanel = new HBox(24);

    public RiskHotspotsInspector() {
        setSpacing(16);
        setPadding(new Insets(16));
        
        // Explicitly load the stylesheet to ensure all nested controls render correctly
        try {
            String css = getClass().getResource("/styles/AnalysisReport.css").toExternalForm();
            getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Could not load AnalysisReport.css in RiskHotspotsInspector: " + e.getMessage());
        }

        initialiseLayout();
    }

    @Override
    public String getTitle() {
        return "Risk Hotspots";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("warning");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #ff9956;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getRiskHotspotsState();
        masterData.setAll(this.state.getAllHotspots());
        
        // Initial setup
        calculateTopMetrics();
        setupFilters();
        updateFilterPredicate();
        
        if (!sortedData.isEmpty()) {
            selectEntry(sortedData.get(0), null);
        }
    }

    private void initialiseLayout() {
        // 1. Build Top Metrics Panels Row
        HBox metricsRow = new HBox(16);
        metricsRow.setAlignment(Pos.CENTER_LEFT);
        metricsRow.getChildren().addAll(
            createMetricPanel("🎯", "#00daf3", "Total Hotspots", lblTotalHotspots, lblTotalHotspotsSub),
            createMetricPanel("🛡", "#ff4b4b", "Critical Risk", lblCriticalRisk, lblCriticalRiskSub),
            createMetricPanel("⚡", "#9b59b6", "Avg. Instability", lblAvgInstability, new Label("vs previous analysis")),
            createMetricPanel("📝", "#e67e22", "Largest Entity", lblLargestEntity, lblLargestEntityName),
            createMetricPanel("📥", "#2ecc71", "Highest Fan-In", lblHighestFanIn, lblHighestFanInName)
        );

        // 2. Build Split Body Layout
        HBox bodyRow = new HBox(20);
        VBox.setVgrow(bodyRow, Priority.ALWAYS);

        // A. Left Filters Sidebar
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(210);
        sidebar.setMinWidth(210);
        sidebar.setStyle(
            "-fx-background-color: rgba(18, 24, 31, 0.4); " +
            "-fx-border-color: rgba(26, 38, 48, 0.5); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-padding: 12;"
        );

        HBox sidebarHeader = new HBox();
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblFiltersTitle = new Label("FILTERS");
        lblFiltersTitle.setStyle("-fx-text-fill: #e6f1f3; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'Epilogue';");
        Pane filterSpacer = new Pane();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        Hyperlink hlReset = new Hyperlink("Reset All");
        hlReset.setStyle("-fx-text-fill: #00daf3; -fx-font-size: 11px; -fx-padding: 0;");
        hlReset.setOnAction(e -> resetAllFilters());
        sidebarHeader.getChildren().addAll(lblFiltersTitle, filterSpacer, hlReset);

        VBox catSection = createSidebarSection("Risk Category", cbAllHotspots, cbOversized, cbHighlyInstable, cbHighFanIn, cbCircularDep, cbHighFanOut);
        VBox levelSection = createSidebarSection("Risk Level", cbCritical, cbHigh, cbMedium, cbLow);

        styleLevelCheckbox(cbCritical, "#ff4b4b");
        styleLevelCheckbox(cbHigh, "#e67e22");
        styleLevelCheckbox(cbMedium, "#f1c40f");
        styleLevelCheckbox(cbLow, "#2ecc71");

        // Instability Range Slider
        VBox instSection = new VBox(4);
        Label lblInst = new Label("Instability Range");
        lblInst.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-weight: bold;");
        sliderInstability.setStyle("-fx-control-inner-background: #11171d;");
        Label lblInstVal = new Label("0.00 - 1.00");
        lblInstVal.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
        sliderInstability.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblInstVal.setText(String.format("0.00 - %.2f", newVal.doubleValue()));
            currentPage = 0;
            updateFilterPredicate();
        });
        instSection.getChildren().addAll(lblInst, sliderInstability, lblInstVal);

        // LOC Slider
        VBox locSection = new VBox(4);
        Label lblLoc = new Label("LOC (Lines of Code)");
        lblLoc.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-weight: bold;");
        sliderLoc.setStyle("-fx-control-inner-background: #11171d;");
        Label lblLocVal = new Label("0 - 10000+");
        lblLocVal.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
        sliderLoc.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblLocVal.setText(String.format("0 - %d+", newVal.intValue()));
            currentPage = 0;
            updateFilterPredicate();
        });
        locSection.getChildren().addAll(lblLoc, sliderLoc, lblLocVal);

        sidebar.getChildren().addAll(sidebarHeader, catSection, levelSection, instSection, locSection);

        // B. Right Main Panel
        VBox mainPanel = new VBox(10);
        HBox.setHgrow(mainPanel, Priority.ALWAYS);

        // Search & Sort bar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        txtSearch.setPromptText("Search entity or package...");
        txtSearch.setStyle("-fx-background-color: #11171d; -fx-text-fill: #e6f1f3; -fx-border-color: rgba(26,38,48,0.8); -fx-border-radius: 6; -fx-padding: 6 12; -fx-font-size: 12px;");
        txtSearch.setPrefWidth(260);
        txtSearch.textProperty().addListener((obs, old, newVal) -> {
            currentPage = 0;
            updateFilterPredicate();
        });

        Pane toolSpacer = new Pane();
        HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        Label lblSort = new Label("Sort by");
        lblSort.setStyle("-fx-text-fill: #72848a; -fx-font-size: 12px;");

        cmbSort.getItems().addAll("Risk Score", "LOC", "Instability", "Fan-Out", "Fan-In");
        cmbSort.setValue("Risk Score");
        cmbSort.setStyle("-fx-background-color: #11171d; -fx-text-fill: #e6f1f3; -fx-border-color: rgba(26,38,48,0.8); -fx-border-radius: 6;");
        cmbSort.valueProperty().addListener((obs, old, newVal) -> {
            currentPage = 0;
            applySorting();
        });

        btnSortToggle.setStyle("-fx-background-color: #11171d; -fx-text-fill: #bac9cc; -fx-border-color: rgba(26,38,48,0.8); -fx-border-radius: 6; -fx-cursor: hand;");
        btnSortToggle.setOnAction(e -> {
            sortAscending = !sortAscending;
            btnSortToggle.setText(sortAscending ? "↑" : "↓");
            currentPage = 0;
            applySorting();
        });

        toolbar.getChildren().addAll(txtSearch, toolSpacer, lblSort, cmbSort, btnSortToggle);

        // Scrollable List container for card rows (with limited height to prevent oversized layout)
        ScrollPane listScroll = new ScrollPane(cardsContainer);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        listScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        listScroll.setPrefHeight(380);
        listScroll.setMaxHeight(380);
        listScroll.setMinHeight(380);
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        sortedData.addListener((ListChangeListener<InspectorEntry>) c -> rebuildCards());

        // Footer Pagination
        HBox tableFooter = new HBox();
        tableFooter.setAlignment(Pos.CENTER_LEFT);
        lblShowing.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px;");
        
        Pane footerSpacer = new Pane();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        paginationBox.setAlignment(Pos.CENTER_RIGHT);

        tableFooter.getChildren().addAll(lblShowing, footerSpacer, paginationBox);

        mainPanel.getChildren().addAll(toolbar, listScroll, tableFooter);
        bodyRow.getChildren().addAll(sidebar, mainPanel);

        // 3. Bottom Selected Detail Panel
        detailsPanel.setStyle(
            "-fx-background-color: rgba(18, 24, 31, 0.6); " +
            "-fx-border-color: rgba(26, 38, 48, 0.8); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 14;"
        );
        detailsPanel.setPrefHeight(200);
        detailsPanel.setMinHeight(200);

        setupDetailColumns();

        getChildren().addAll(metricsRow, bodyRow, detailsPanel);
    }

    private Node createMetricPanel(String iconStr, String colorHex, String title, Label valLabel, Label subLabel) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setStyle(
            "-fx-background-color: rgba(18, 24, 31, 0.4); " +
            "-fx-border-color: rgba(26, 38, 48, 0.5); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 14;"
        );

        Label icon = new Label(iconStr);
        icon.setStyle("-fx-font-size: 18px; -fx-text-fill: " + colorHex + ";");
        StackPane iconCircle = new StackPane(icon);
        iconCircle.setPrefSize(34, 34);
        iconCircle.setStyle(
            "-fx-background-color: " + colorHex + "0D; " +
            "-fx-border-color: " + colorHex + "40; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 17; " +
            "-fx-background-radius: 17;"
        );

        VBox textCol = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-family: 'Inter';");
        valLabel.setStyle("-fx-text-fill: #e6f1f3; -fx-font-weight: bold; -fx-font-size: 18px; -fx-font-family: 'Epilogue';");
        subLabel.setStyle("-fx-text-fill: #55676b; -fx-font-size: 9px; -fx-font-family: 'Inter';");

        textCol.getChildren().addAll(titleLbl, valLabel, subLabel);
        box.getChildren().addAll(iconCircle, textCol);
        return box;
    }

    private VBox createSidebarSection(String title, CheckBox... checkboxes) {
        VBox section = new VBox(6);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-weight: bold;");
        section.getChildren().add(titleLbl);

        for (CheckBox cb : checkboxes) {
            cb.setStyle("-fx-text-fill: #bac9cc; -fx-font-size: 11px; -fx-cursor: hand;");
            cb.selectedProperty().addListener((obs, old, newVal) -> {
                currentPage = 0;
                updateFilterPredicate();
            });
            section.getChildren().add(cb);
        }
        return section;
    }

    private void styleLevelCheckbox(CheckBox cb, String dotColor) {
        Circle dot = new Circle(3, Color.web(dotColor));
        HBox graphicBox = new HBox(6, dot);
        graphicBox.setAlignment(Pos.CENTER_LEFT);
        cb.setGraphic(graphicBox);
    }

    private void rebuildCards() {
        cardsContainer.getChildren().clear();
        selectedRowNode = null;

        int totalCount = sortedData.size();
        int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;

        if (currentPage >= totalPages) {
            currentPage = 0;
        }

        if (totalCount == 0) {
            cardsContainer.getChildren().add(createEmptyState());
            lblShowing.setText("Showing 0 of 0 hotspots");
            paginationBox.getChildren().clear();
            clearDetailPanel();
            return;
        }

        int startIdx = currentPage * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, totalCount);

        lblShowing.setText(String.format("Showing %d to %d of %d hotspots", startIdx + 1, endIdx, totalCount));

        // Render card entities of current page
        for (int i = startIdx; i < endIdx; i++) {
            InspectorEntry entry = sortedData.get(i);
            
            // Build the card layout block exactly like the mockup
            HBox cardRow = new HBox(20);
            cardRow.setAlignment(Pos.CENTER_LEFT);

            // Left Section: Entity & Package
            VBox colLeft = new VBox(4);
            colLeft.setPrefWidth(240);
            colLeft.setMinWidth(240);
            HBox.setHgrow(colLeft, Priority.ALWAYS);
            
            Label lblEntHeader = new Label("ENTITY");
            lblEntHeader.setStyle("-fx-text-fill: #55676b; -fx-font-size: 8px; -fx-font-weight: bold; -fx-font-family: 'Inter';");
            
            HBox entNameBox = new HBox(6);
            entNameBox.setAlignment(Pos.CENTER_LEFT);
            Label codeIcon = new Label("C");
            codeIcon.setStyle(
                "-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-font-size: 8px; " +
                "-fx-background-color: rgba(142, 68, 173, 0.1); -fx-border-color: rgba(142, 68, 173, 0.3); " +
                "-fx-border-radius: 4; -fx-background-radius: 4; -fx-alignment: center; " +
                "-fx-pref-width: 14; -fx-pref-height: 14;"
            );
            Label nameLbl = new Label(entry.name());
            nameLbl.setStyle("-fx-text-fill: #00daf3; -fx-font-weight: bold; -fx-font-size: 13px; -fx-font-family: 'Epilogue';");
            Label linkIcon = new Label("↗");
            linkIcon.setStyle("-fx-text-fill: #55676b; -fx-font-size: 9px;");
            entNameBox.getChildren().addAll(codeIcon, nameLbl, linkIcon);

            Label lblPkgHeader = new Label("PACKAGE (Namespace)");
            lblPkgHeader.setStyle("-fx-text-fill: #55676b; -fx-font-size: 8px; -fx-font-weight: bold; -fx-font-family: 'Inter'; -fx-padding: 4 0 0 0;");
            
            Label pkgVal = new Label(entry.pkg());
            pkgVal.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-family: 'Inter';");

            colLeft.getChildren().addAll(lblEntHeader, entNameBox, lblPkgHeader, pkgVal);

            // Middle Section: Active Flags
            VBox colMiddle = new VBox(6);
            colMiddle.setPrefWidth(180);
            colMiddle.setMinWidth(180);
            colMiddle.setMaxWidth(180);
            
            Label lblFlagsHeader = new Label("ACTIVE FLAGS");
            lblFlagsHeader.setStyle("-fx-text-fill: #55676b; -fx-font-size: 8px; -fx-font-weight: bold; -fx-font-family: 'Inter';");

            FlowPane flagsPane = new FlowPane(4, 4);
            String[] tokens = entry.flags().split(", ");
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                Label badge = new Label(token.replace("_ENTITY", "").replace("HIGH_", ""));
                String colorHex = "#3498db";
                if (token.contains("INSTABILITY")) colorHex = "#ff4b4b";
                else if (token.contains("COUPLING")) colorHex = "#9b59b6";
                else if (token.contains("DEPENDENCY")) colorHex = "#e67e22";
                
                badge.setStyle(
                    "-fx-text-fill: " + colorHex + "; -fx-background-color: " + colorHex + "15; " +
                    "-fx-border-color: " + colorHex + "33; -fx-border-width: 1; -fx-border-radius: 4; " +
                    "-fx-background-radius: 4; -fx-padding: 2 6; -fx-font-size: 8px; -fx-font-weight: bold;"
                );
                flagsPane.getChildren().add(badge);
            }

            colMiddle.getChildren().addAll(lblFlagsHeader, flagsPane);

            // Right Section: Risk & Metrics Grid
            VBox colRight = new VBox(4);
            colRight.setPrefWidth(160);
            colRight.setMinWidth(160);
            colRight.setMaxWidth(160);

            Label lblRiskHeader = new Label("RISK");
            lblRiskHeader.setStyle("-fx-text-fill: #55676b; -fx-font-size: 8px; -fx-font-weight: bold; -fx-font-family: 'Inter';");
            
            Label riskVal = new Label(entry.riskLevel().toUpperCase());
            String riskColor = "CRITICAL".equalsIgnoreCase(entry.riskLevel()) || "HIGH".equalsIgnoreCase(entry.riskLevel()) ? "#ff4b4b" : "#e67e22";
            riskVal.setStyle("-fx-text-fill: " + riskColor + "; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold; -fx-font-size: 13px;");

//            Separator horizDivider = new Separator(javafx.geometry.Orientation.HORIZONTAL);
//            horizDivider.setStyle("-fx-background-color: rgba(26, 38, 48, 0.3); -fx-pref-height: 1; -fx-padding: 2 0;");

            // Metrics HBox Row
            HBox metricsRow = new HBox(12);
            metricsRow.setAlignment(Pos.CENTER_LEFT);
            metricsRow.getChildren().addAll(
                createMiniMetric("LOC", String.valueOf(entry.loc())), createMetricDivider(),
                createMiniMetric("METHODS", String.valueOf(entry.methodCount())), createMetricDivider(),
                createMiniMetric("FIELDS", String.valueOf(entry.fieldCount())), createMetricDivider(),
                createMiniMetric("FAN-IN", String.valueOf(entry.fanIn())), createMetricDivider(),
                createMiniMetric("FAN-OUT", String.valueOf(entry.fanOut())), createMetricDivider(),
                createMiniMetric("INSTABILITY", String.format("%.2f", entry.instability()))
            );

            colRight.getChildren().addAll(lblRiskHeader, riskVal);

            // Add visual dividers
            Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
            sep1.setStyle("-fx-background-color: rgba(26, 38, 48, 0.4); -fx-pref-width: 1;");
            Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
            sep2.setStyle("-fx-background-color: rgba(26, 38, 48, 0.4); -fx-pref-width: 1;");

            cardRow.getChildren().addAll(colLeft, sep1, colMiddle, sep2, colRight);
            
            VBox cardBox = new VBox(8);
            cardBox.setPadding(new Insets(14, 18, 14, 18));
            cardBox.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.45); " +
                "-fx-border-color: rgba(26, 38, 48, 0.6); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );
            cardBox.getChildren().addAll(cardRow, metricsRow);

            // Selection styles
            cardBox.setOnMouseEntered(e -> {
                if (selectedRowNode != cardBox) {
                    cardBox.setStyle(
                        "-fx-background-color: rgba(0, 218, 243, 0.03); " +
                        "-fx-border-color: rgba(0, 218, 243, 0.15); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
                    );
                }
            });
            cardBox.setOnMouseExited(e -> {
                if (selectedRowNode != cardBox) {
                    cardBox.setStyle(
                        "-fx-background-color: rgba(18, 24, 31, 0.45); " +
                        "-fx-border-color: rgba(26, 38, 48, 0.6); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
                    );
                }
            });
            cardBox.setOnMouseClicked(e -> selectEntry(entry, cardBox));

            // Restore selection reference
            if (selectedEntry != null && selectedEntry.name().equals(entry.name())) {
                selectEntry(entry, cardBox);
            } else if (selectedEntry == null && i == startIdx) {
                selectEntry(entry, cardBox);
            }

            cardsContainer.getChildren().add(cardBox);
        }

        // Build Pagination Buttons
        buildPaginationControls(totalPages);
    }

    private Node createMiniMetric(String name, String value) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.CENTER);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #55676b; -fx-font-size: 8px; -fx-font-weight: bold; -fx-font-family: 'Inter';");
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold;");
        cell.getChildren().addAll(nameLbl, valLbl);
        HBox.setHgrow(cell, Priority.ALWAYS);
        return cell;
    }

    private Node createMetricDivider() {
        Separator s = new Separator(javafx.geometry.Orientation.VERTICAL);
        s.setStyle("-fx-background-color: rgba(26, 38, 48, 0.25); -fx-pref-width: 1; -fx-pref-height: 14; -fx-padding: 0 4;");
        return s;
    }

    private void selectEntry(InspectorEntry entry, VBox cardRow) {
        if (selectedRowNode != null) {
            selectedRowNode.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.45); " +
                "-fx-border-color: rgba(26, 38, 48, 0.6); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );
        }

        selectedEntry = entry;
        selectedRowNode = cardRow;

        if (cardRow != null) {
            cardRow.setStyle(
                "-fx-background-color: rgba(0, 218, 243, 0.08); " +
                "-fx-border-color: rgba(0, 218, 243, 0.35); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );
        }

        updateDetailPanel(entry);
    }

    private void buildPaginationControls(int totalPages) {
        paginationBox.getChildren().clear();

        if (totalPages <= 1) return;

        // Previous Button
        Button btnPrev = new Button("<");
        btnPrev.setDisable(currentPage == 0);
        btnPrev.setStyle("-fx-background-color: #12181f; -fx-text-fill: #bac9cc; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4; -fx-cursor: hand; -fx-font-size: 10px;");
        btnPrev.setMinWidth(24);
        btnPrev.setOnAction(e -> {
            currentPage--;
            rebuildCards();
        });
        paginationBox.getChildren().add(btnPrev);

        // Standard Pagination Scheme with real ellipsis for hidden pages
        if (totalPages <= 7) {
            for (int p = 0; p < totalPages; p++) {
                addPageButton(p);
            }
        } else {
            // Always show first page
            addPageButton(0);

            if (currentPage > 2) {
                Label ellipsis1 = new Label("...");
                ellipsis1.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-padding: 0 4; -fx-alignment: bottom-center;");
                paginationBox.getChildren().add(ellipsis1);
            }

            int middleStart = Math.max(1, currentPage - 1);
            int middleEnd = Math.min(totalPages - 2, currentPage + 1);

            // Adjust boundaries
            if (currentPage <= 2) {
                middleEnd = 3;
            } else if (currentPage >= totalPages - 3) {
                middleStart = totalPages - 4;
            }

            for (int p = middleStart; p <= middleEnd; p++) {
                addPageButton(p);
            }

            if (currentPage < totalPages - 3) {
                Label ellipsis2 = new Label("...");
                ellipsis2.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-padding: 0 4; -fx-alignment: bottom-center;");
                paginationBox.getChildren().add(ellipsis2);
            }

            // Always show last page
            addPageButton(totalPages - 1);
        }

        // Next Button
        Button btnNext = new Button(">");
        btnNext.setDisable(currentPage == totalPages - 1);
        btnNext.setStyle("-fx-background-color: #12181f; -fx-text-fill: #bac9cc; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4; -fx-cursor: hand; -fx-font-size: 10px;");
        btnNext.setMinWidth(24);
        btnNext.setOnAction(e -> {
            currentPage++;
            rebuildCards();
        });
        paginationBox.getChildren().add(btnNext);
    }

    private void addPageButton(int p) {
        final int pageIdx = p;
        Button btnPage = new Button(String.valueOf(p + 1));
        btnPage.setMinWidth(Region.USE_PREF_SIZE);
        btnPage.setPrefWidth(26);
        if (p == currentPage) {
            btnPage.setStyle(
                "-fx-background-color: rgba(0, 218, 243, 0.15); " +
                "-fx-text-fill: #00daf3; " +
                "-fx-border-color: #00daf3; " +
                "-fx-border-radius: 4; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 10px;"
            );
        } else {
            btnPage.setStyle("-fx-background-color: #12181f; -fx-text-fill: #bac9cc; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4; -fx-cursor: hand; -fx-font-size: 10px;");
        }
        btnPage.setOnAction(e -> {
            currentPage = pageIdx;
            rebuildCards();
        });
        paginationBox.getChildren().add(btnPage);
    }

    private Node createEmptyState() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        box.setStyle(
            "-fx-background-color: rgba(18, 24, 31, 0.2); " +
            "-fx-border-color: rgba(26, 38, 48, 0.3); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        Label lbl = new Label("No hotspots matching current active filters.");
        lbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-style: italic;");
        box.getChildren().add(lbl);
        return box;
    }

    private void setupDetailColumns() {
        detailsPanel.getChildren().clear();

        colDetail1.setPrefWidth(320); HBox.setHgrow(colDetail1, Priority.ALWAYS);
        colDetail2.setPrefWidth(180);
        colDetail3.setPrefWidth(320); HBox.setHgrow(colDetail3, Priority.ALWAYS);

        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep1.setStyle("-fx-background-color: rgba(26, 38, 48, 0.4); -fx-pref-width: 1;");
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep2.setStyle("-fx-background-color: rgba(26, 38, 48, 0.4); -fx-pref-width: 1;");

        detailsPanel.getChildren().addAll(colDetail1, sep1, colDetail2, sep2, colDetail3);
    }

    private void updateDetailPanel(InspectorEntry entry) {
        int score = calculateRiskScore(entry);

        // Column 1: Header + Badges + Warnings + Button
        colDetail1.getChildren().clear();
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label entityIcon = new Label("C");
        entityIcon.setStyle(
            "-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-font-size: 10px; " +
            "-fx-background-color: rgba(142, 68, 173, 0.15); -fx-border-color: rgba(142, 68, 173, 0.4); " +
            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-alignment: center; " +
            "-fx-pref-width: 18; -fx-pref-height: 18;"
        );
        Label nameLbl = new Label(entry.name());
        nameLbl.setStyle("-fx-text-fill: #e6f1f3; -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Epilogue';");
        header.getChildren().addAll(entityIcon, nameLbl);

        Label pkgLbl = new Label(entry.pkg());
        pkgLbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px;");

        HBox badges = new HBox(6);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label riskBadge = new Label(entry.riskLevel().toUpperCase());
        String riskColor = "CRITICAL".equalsIgnoreCase(entry.riskLevel()) || "HIGH".equalsIgnoreCase(entry.riskLevel()) ? "#ff4b4b" : "#e67e22";
        riskBadge.setStyle(
            "-fx-text-fill: " + riskColor + "; -fx-background-color: " + riskColor + "1A; " +
            "-fx-border-color: " + riskColor + "33; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 2 6; -fx-font-size: 8px; -fx-font-weight: bold;"
        );
        Label scoreBadge = new Label("Risk Score: " + score + "/100");
        scoreBadge.setStyle(
            "-fx-text-fill: #bac9cc; -fx-background-color: rgba(255,255,255,0.03); " +
            "-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 2 6; -fx-font-size: 8px; -fx-font-weight: bold;"
        );
        badges.getChildren().addAll(riskBadge, scoreBadge);

        VBox warnings = new VBox(6);
        Label whyTitle = new Label("Why is this a hotspot?");
        whyTitle.setStyle("-fx-text-fill: #72848a; -fx-font-size: 10px; -fx-font-weight: bold;");
        warnings.getChildren().add(whyTitle);

        if (entry.flags().contains("OVERSIZED")) {
            addWarningItem(warnings, "⚠ Oversized entity with " + entry.loc() + " lines of code, " + entry.methodCount()+" methods, "+entry.fieldCount()+ " fields");
        }
        if (entry.flags().contains("INSTABILITY")) {
            addWarningItem(warnings, "⚠ Very high instability (" + String.format("%.2f", entry.instability()) + ") indicates strong outward dependencies");
        }
        if (entry.flags().contains("COUPLING") || entry.flags().contains("FAN_OUT") || entry.flags().contains("FAN_IN")) {
            addWarningItem(warnings, "⚠ High coupling: " + entry.fanOut() + " outgoing dependencies" + entry.fanIn()+"Incoming dependencies");
        }
        if (entry.flags().contains("CIRCULAR")) {
            addWarningItem(warnings, "⚠ Circular reference loops detected on component");
        }



        colDetail1.getChildren().addAll(header, pkgLbl, badges, warnings);

        // Column 2: Key Metrics Table
        colDetail2.getChildren().clear();
        Label metricsHeader = new Label("Key Metrics");
        metricsHeader.setStyle("-fx-text-fill: #72848a; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);
        addGridRow(grid, "LOC", String.valueOf(entry.loc()), 0);
        addGridRow(grid, "Methods", String.valueOf(entry.methodCount()), 1);
        addGridRow(grid, "Fields", String.valueOf(entry.fieldCount()), 2);
        addGridRow(grid, "Fan-Out", String.valueOf(entry.fanOut()), 3);
        addGridRow(grid, "Fan-In", String.valueOf(entry.fanIn()), 4);
        
        // Instability with visual bar
        Label lblI = new Label("Instability");
        lblI.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px;");
        HBox iBox = new HBox(6);
        iBox.setAlignment(Pos.CENTER_RIGHT);
        Label valI = new Label(String.format("%.2f", entry.instability()));
        valI.setStyle("-fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold;");
        Region bar = new Region();
        bar.setPrefSize(30 * entry.instability(), 4);
        bar.setStyle("-fx-background-color: #ff4b4b; -fx-background-radius: 2;");
        iBox.getChildren().addAll(bar, valI);

        grid.add(lblI, 0, 5);
        grid.add(iBox, 1, 5);

        colDetail2.getChildren().addAll(metricsHeader, grid);

        // Column 3: Active Flags
        colDetail3.getChildren().clear();
        Label flagsHeader = new Label("Active Flags");
        flagsHeader.setStyle("-fx-text-fill: #72848a; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        VBox flagsBox = new VBox(8);
        String[] tokens = entry.flags().split(", ");
        for (String token : tokens) {
            if (token.trim().isEmpty()) continue;
            String displayName = token.replace("_", " ").toUpperCase();
            
            String color = "blue";
            if (token.contains("INSTABILITY") || token.contains("CRITICAL")) color = "red";
            else if (token.contains("COUPLING") || token.contains("FAN_IN") || token.contains("FAN_OUT")) color = "purple";
            else if (token.contains("DEPENDENCY")) color = "orange";

            flagsBox.getChildren().add(createFlagCheckbox(displayName, color));
        }

        colDetail3.getChildren().addAll(flagsHeader, flagsBox);
    }

    private void addWarningItem(VBox parent, String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #ffaa56; -fx-font-size: 10px; -fx-wrap-text: true;");
        parent.getChildren().add(lbl);
    }

    private void addGridRow(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private Node createFlagCheckbox(String flagName, String color) {
        String colorHex = color.equals("blue") ? "#3498db" : (color.equals("red") ? "#ff4b4b" : (color.equals("orange") ? "#e67e22" : "#9b59b6"));
        Label lbl = new Label("☑ " + flagName);
        lbl.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        return lbl;
    }

    private void calculateTopMetrics() {
        int totalCount = masterData.size();
        long criticalCount = masterData.stream().filter(e -> "HIGH".equalsIgnoreCase(e.riskLevel())).count();
        double avgInst = masterData.stream().mapToDouble(InspectorEntry::instability).average().orElse(0.0);

        InspectorEntry largest = masterData.stream().max(Comparator.comparingInt(InspectorEntry::loc)).orElse(null);
        InspectorEntry highestIn = masterData.stream().max(Comparator.comparingInt(InspectorEntry::fanIn)).orElse(null);

        lblTotalHotspots.setText(String.valueOf(totalCount));
        lblTotalHotspotsSub.setText("18.7% of total entities");
        lblCriticalRisk.setText(String.valueOf(criticalCount));
        
        double criticalPct = totalCount > 0 ? (double) criticalCount / totalCount * 100 : 0.0;
        lblCriticalRiskSub.setText(String.format("%.1f%% of hotspots", criticalPct));
        lblAvgInstability.setText(String.format("%.2f", avgInst));

        if (largest != null) {
            lblLargestEntity.setText(NumberFormat.getInstance().format(largest.loc()) + " LOC");
            lblLargestEntityName.setText(largest.name());
        }
        if (highestIn != null) {
            lblHighestFanIn.setText(String.valueOf(highestIn.fanIn()));
            lblHighestFanInName.setText(highestIn.name());
        }
    }

    private void setupFilters() {
        cbAllHotspots.setOnAction(e -> {
            if (cbAllHotspots.isSelected()) {
                cbOversized.setSelected(false);
                cbHighlyInstable.setSelected(false);
                cbHighFanIn.setSelected(false);
                cbCircularDep.setSelected(false);
                cbHighFanOut.setSelected(false);
            }
            updateFilterPredicate();
        });

        sliderInstability.setValue(1.0);
        sliderLoc.setValue(10000);
    }

    private void resetAllFilters() {
        cbAllHotspots.setSelected(true);
        cbOversized.setSelected(false);
        cbHighlyInstable.setSelected(false);
        cbHighFanIn.setSelected(false);
        cbCircularDep.setSelected(false);
        cbHighFanOut.setSelected(false);

        cbCritical.setSelected(false);
        cbHigh.setSelected(false);
        cbMedium.setSelected(false);
        cbLow.setSelected(false);

        sliderInstability.setValue(1.0);
        sliderLoc.setValue(10000);
        txtSearch.clear();

        updateFilterPredicate();
    }

    private void updateFilterPredicate() {
        filteredData.setPredicate(entry -> {
            // Search filter
            String search = txtSearch.getText().toLowerCase().trim();
            if (!search.isEmpty()) {
                boolean matchSearch = entry.name().toLowerCase().contains(search) || 
                                     entry.pkg().toLowerCase().contains(search);
                if (!matchSearch) return false;
            }

            // Category filters
            boolean categoryMatch = true;
            if (!cbAllHotspots.isSelected() && (cbOversized.isSelected() || cbHighlyInstable.isSelected() || 
                cbHighFanIn.isSelected() || cbCircularDep.isSelected() || cbHighFanOut.isSelected())) {
                categoryMatch = false;
                if (cbOversized.isSelected() && entry.flags().contains("OVERSIZED")) categoryMatch = true;
                if (cbHighlyInstable.isSelected() && entry.flags().contains("INSTABILITY")) categoryMatch = true;
                if (cbHighFanIn.isSelected() && entry.flags().contains("FAN_IN")) categoryMatch = true;
                if (cbCircularDep.isSelected() && entry.flags().contains("CIRCULAR")) categoryMatch = true;
                if (cbHighFanOut.isSelected() && entry.flags().contains("FAN_OUT")) categoryMatch = true;
            }
            if (!categoryMatch) return false;

            // Risk Level filters
            if (cbCritical.isSelected() || cbHigh.isSelected() || cbMedium.isSelected() || cbLow.isSelected()) {
                boolean levelMatch = false;
                if (cbCritical.isSelected() && "CRITICAL".equalsIgnoreCase(entry.riskLevel())) levelMatch = true;
                if (cbHigh.isSelected() && "HIGH".equalsIgnoreCase(entry.riskLevel())) levelMatch = true;
                if (cbMedium.isSelected() && "MEDIUM".equalsIgnoreCase(entry.riskLevel())) levelMatch = true;
                if (cbLow.isSelected() && "LOW".equalsIgnoreCase(entry.riskLevel())) levelMatch = true;
                if (!levelMatch) return false;
            }

            // Sliders filters
            if (entry.instability() > sliderInstability.getValue()) return false;
            if (entry.loc() > sliderLoc.getValue()) return false;

            return true;
        });

        rebuildCards();
    }

    private void applySorting() {
        String prop = cmbSort.getValue();
        Comparator<InspectorEntry> comp;
        
        switch (prop) {
            case "LOC":
                comp = Comparator.comparingInt(InspectorEntry::loc);
                break;
            case "Instability":
                comp = Comparator.comparingDouble(InspectorEntry::instability);
                break;
            case "Fan-Out":
                comp = Comparator.comparingInt(InspectorEntry::fanOut);
                break;
            case "Fan-In":
                comp = Comparator.comparingInt(InspectorEntry::fanIn);
                break;
            case "Risk Score":
            default:
                comp = Comparator.comparingInt(this::calculateRiskScore);
                break;
        }

        if (!sortAscending) {
            comp = comp.reversed();
        }

        sortedData.setComparator(comp);
    }

    private int calculateRiskScore(InspectorEntry entry) {
        int flagsCount = entry.flags().isEmpty() ? 0 : entry.flags().split(",").length;
        double raw = (flagsCount * 25.0) + (entry.loc() / 150.0) + (entry.instability() * 20.0);
        return Math.min(100, (int) Math.round(raw));
    }

    private void clearDetailPanel() {
        colDetail1.getChildren().clear();
        colDetail2.getChildren().clear();
        colDetail3.getChildren().clear();
        colDetail4.getChildren().clear();
    }
}
