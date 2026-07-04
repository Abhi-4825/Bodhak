package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.bodhak.model.entity.EntityInfo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DependencyExplorerDashboard extends ScrollPane {

    private final DependencyExplorerState state;
    private final VBox mainLayout = new VBox(16);
    private TextField searchField;
    private ContextMenu searchPopup;

    public DependencyExplorerDashboard(DependencyExplorerState state) {
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

        // 1. Header Bar
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        VBox titleBox = new VBox(4);
        Label title = new Label("Dependency Explorer");
        title.setStyle("-fx-font-family: 'Epilogue', sans-serif; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dce3ec;");
        Label subtitle = new Label("Explore compiler-derived semantic dependencies");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #849396;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Restored Global Search Bar
        searchField = new TextField();
        searchField.setPromptText("Search entity, namespace, package... (Ctrl + K)");
        searchField.setStyle("-fx-background-color: #151c23; -fx-text-fill: #dce3ec; -fx-prompt-text-fill: #849396; -fx-border-color: rgba(132, 147, 150, 0.15); -fx-border-radius: 6; -fx-background-radius: 6; -fx-pref-width: 350; -fx-padding: 6 12;");

        searchPopup = new ContextMenu();
        searchPopup.setStyle("-fx-background-color: #151c23; -fx-border-color: rgba(132, 147, 150, 0.15); -fx-border-radius: 6;");

        // Autocomplete filtering logic
        searchField.textProperty().addListener((obs, old, text) -> {
            if (text == null || text.isBlank()) {
                searchPopup.hide();
                return;
            }
            if (state.analysisContextProperty().get() == null) return;

            searchPopup.getItems().clear();
            String query = text.toLowerCase();

            // Match Simple Name, Qualified Name, Namespace, or Compilation Unit
            List<EntityInfo> matches = state.analysisContextProperty().get().getEntities().stream()
                    .filter(e -> e.getEntityName().toLowerCase().contains(query)
                            || (e.getNamespaceName() != null && e.getNamespaceName().toLowerCase().contains(query)))
                    .limit(8)
                    .toList();

            for (EntityInfo match : matches) {
                MenuItem item = new MenuItem(match.getSimpleName() + "  (" + match.getNamespaceName() + ")");
                item.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
                item.setOnAction(ev -> {
                    state.selectedEntityProperty().set(match);
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

        // Scene listener to register global Ctrl + K shortcut
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ke -> {
                    if (ke.isControlDown() && ke.getCode() == javafx.scene.input.KeyCode.K) {
                        searchField.requestFocus();
                        ke.consume();
                    }
                });
            }
        });

        header.getChildren().addAll(titleBox, spacer, searchField);

        // 2. Stats Ribbon (Executive Summary)
        ExecutiveSummaryStrip summaryStrip = new ExecutiveSummaryStrip(state.getExecutiveSummaryState());

        // 3. Workspace SplitPane
        SplitPane workspaceSplit = new SplitPane();
        workspaceSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        VBox.setVgrow(workspaceSplit, Priority.ALWAYS);

        // Column 1: Entity Browser (Left)
        EntityBrowserPanel browserPanel = new EntityBrowserPanel(state);
        browserPanel.setMinWidth(280);
        browserPanel.setMaxWidth(500);
        SplitPane.setResizableWithParent(browserPanel, false);

        // Column 2: Center (Graph + Bottom Panels)
        VBox centerCol = new VBox(16);
        SplitPane.setResizableWithParent(centerCol, true);

        DependencyGraphView graphView = new DependencyGraphView(state);
        graphView.setMinHeight(450);
        VBox.setVgrow(graphView, Priority.ALWAYS);

        // Bottom row inside center column: Path, Breakdown, Health
        HBox bottomRow = new HBox(16);
        bottomRow.setMinHeight(180);
        bottomRow.setPrefHeight(180);

        PathPanel pathPanel = new PathPanel(state);
        HBox.setHgrow(pathPanel, Priority.ALWAYS);

        // We will rename SemanticBreakdownPanel to ReferenceIntelligencePanel in Task 5
        ReferenceIntelligencePanel breakdownPanel = new ReferenceIntelligencePanel(state.getBreakdownState());
        HBox.setHgrow(breakdownPanel, Priority.ALWAYS);

        HealthPanel healthPanel = new HealthPanel(state.getHealthState());
        HBox.setHgrow(healthPanel, Priority.ALWAYS);

        bottomRow.getChildren().addAll(pathPanel, breakdownPanel, healthPanel);
        centerCol.getChildren().addAll(graphView, bottomRow);

        // Column 3: Right Sidebar (Selected Entity + Donut Chart)
        VBox rightCol = new VBox(16);
        rightCol.setMinWidth(350);
        rightCol.setMaxWidth(600);
        SplitPane.setResizableWithParent(rightCol, false);

        SelectedEntityPanel detailsPanel = new SelectedEntityPanel(state);
        VBox.setVgrow(detailsPanel, Priority.ALWAYS);

        rightCol.getChildren().addAll(detailsPanel);

        // Add items to SplitPane
        workspaceSplit.getItems().addAll(browserPanel, centerCol, rightCol);
        
        // Define initial divider positions
        workspaceSplit.setDividerPositions(0.22, 0.75);

        mainLayout.getChildren().addAll(header, summaryStrip, workspaceSplit);
    }
}
