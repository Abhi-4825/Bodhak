package com.example.anuviya.ui.workspace.home;

import com.example.anuviya.platform.state.PlatformState;
import com.example.anuviya.workspace.WorkspaceManager;
import com.example.anuviya.workspace.model.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public final class WorkspaceHomeScreen extends BorderPane {

    private final StackPane parentStack;
    private final Consumer<File> onProjectSelected;
    
    private final VBox leftContentArea = new VBox(25);
    private final VBox rightSidebar = new VBox(20);
    private final VBox sidebarTabs = new VBox(10);
    private final TextField searchField = new TextField();
    private final HBox splitLayout = new HBox(30);

    private String currentTab = "Recent";
    private String searchQuery = "";

    public WorkspaceHomeScreen(StackPane parentStack, Consumer<File> onProjectSelected) {
        this.parentStack = parentStack;
        this.onProjectSelected = onProjectSelected;

        // Load stylesheet
        String stylePath = getClass().getResource("/styles/workspace-home.css").toExternalForm();
        this.getStylesheets().add(stylePath);
        
        getStyleClass().add("workspace-home-root");

        // Build Layout structure
        setTop(buildTopHeader());
        
        splitLayout.setPadding(new Insets(10, 0, 0, 0));
        
        ScrollPane leftScroll = new ScrollPane(leftContentArea);
        leftScroll.setFitToWidth(true);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroll.getStyleClass().add("scroll-pane");
        HBox.setHgrow(leftScroll, Priority.ALWAYS);
        
        leftContentArea.setMinWidth(450);
        rightSidebar.setMinWidth(260);
        rightSidebar.setMaxWidth(260);

        splitLayout.getChildren().addAll(leftScroll, rightSidebar);
        setCenter(splitLayout);

        // Bind data change listener to PlatformState
        PlatformState.currentProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Platform.runLater(this::render);
            }
        });

        // Trigger initial render
        render();
    }

    private HBox buildTopHeader() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("workspace-home-header");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("ANUVIYA");
        title.getStyleClass().add("workspace-home-title");
        Label subtitle = new Label("Understand Code, Build better");
        subtitle.getStyleClass().add("workspace-home-subtitle");
        
        VBox titleBox = new VBox(2);
        titleBox.getChildren().addAll(title, subtitle);

        javafx.scene.image.ImageView logoIcon = com.example.anuviya.ui.helper.IconHelper.createLogoImageView(28);
        HBox titleContainer = new HBox(10);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        if (logoIcon != null) {
            titleContainer.getChildren().add(logoIcon);
        }
        titleContainer.getChildren().add(titleBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField.setPromptText("Search projects...");
        searchField.getStyleClass().add("workspace-search-bar");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchQuery = newVal.trim().toLowerCase();
            render();
        });

        Button openFolderBtn = new Button("Open Folder");
        openFolderBtn.getStyleClass().add("project-card-btn-continue");
        openFolderBtn.setOnAction(e -> triggerOpenFolder());

        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.getChildren().addAll(searchField, openFolderBtn);

        topBar.getChildren().addAll(titleContainer, spacer, actionsBox);
        return topBar;
    }

    private void render() {
        leftContentArea.getChildren().clear();
        rightSidebar.getChildren().clear();
        sidebarTabs.getChildren().clear();

        PlatformState state = PlatformState.getCurrent();
        if (state == null || state.getWorkspaceState() == null) {
            renderEmptyState();
            return;
        }

        WorkspaceState wsState = state.getWorkspaceState();
        List<WorkspaceProject> allProjects = wsState.recentProjects();

        if (allProjects.isEmpty()) {
            renderEmptyState();
            return;
        }

        // Left sidebar tabs navigation if multiple projects exist
        if (allProjects.size() > 1) {
            buildSidebarTabs(wsState);
        } else {
            splitLayout.getChildren().remove(sidebarTabs);
        }

        // 1. Continue Working (Hero card for most recent)
        WorkspaceProject mostRecent = allProjects.get(0);
        if (searchQuery.isEmpty()) {
            leftContentArea.getChildren().add(buildHeroCard(mostRecent, wsState.intelligence().get(mostRecent.id())));
        }

        // 2. Recent Projects List (filtered)
        VBox projectsList = new VBox(15);
        Label listTitle = new Label(currentTab + " Projects");
        listTitle.getStyleClass().add("action-card-title");
        projectsList.getChildren().add(listTitle);

        List<WorkspaceProject> filtered = filterProjects(allProjects);
        // Exclude the hero card from list if query is empty to avoid redundancy
        if (searchQuery.isEmpty() && !filtered.isEmpty() && filtered.get(0).id().equals(mostRecent.id())) {
            filtered.remove(0);
        }

        if (filtered.isEmpty()) {
            Label noProj = new Label("No matching projects found.");
            noProj.getStyleClass().add("project-card-details");
            projectsList.getChildren().add(noProj);
        } else {
            for (WorkspaceProject p : filtered) {
                projectsList.getChildren().add(buildProjectCard(p, wsState.intelligence().get(p.id())));
            }
        }
        leftContentArea.getChildren().add(projectsList);

        // 3. Analysis timeline at the bottom
        leftContentArea.getChildren().add(buildTimelineWidget(mostRecent.id()));

        // Right side panels
        rightSidebar.getChildren().add(buildPlatformStatusPanel(wsState.platformStatus()));
        rightSidebar.getChildren().add(buildQuickActionsPanel(allProjects));
    }

    private void renderEmptyState() {
        splitLayout.getChildren().remove(sidebarTabs);
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));
        
        Label brand = new Label("ANUVIYA");
        brand.getStyleClass().add("workspace-home-title");
        brand.setStyle("-fx-font-size: 32px;");

        Label tagline = new Label("Understand Code, Build better");
        tagline.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00daf3;");

        Label desc = new Label("Analyze software architecture, scalability bottlenecks, and compiler facts in real-time.");
        desc.getStyleClass().add("project-card-details");
        desc.setStyle("-fx-alignment: center;");
        desc.setWrapText(true);

        Button openBtn = new Button("Open Project");
        openBtn.getStyleClass().add("project-card-btn-continue");
        openBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 24 10 24;");
        openBtn.setOnAction(e -> triggerOpenFolder());

        javafx.scene.image.ImageView emptyIcon = com.example.anuviya.ui.helper.IconHelper.createLogoImageView(64);
        if (emptyIcon != null) {
            emptyBox.getChildren().add(emptyIcon);
        }
        emptyBox.getChildren().addAll(brand, tagline, desc, openBtn);
        leftContentArea.getChildren().add(emptyBox);

        // Minimalist right panel empty status
        PlatformState state = PlatformState.getCurrent();
        if (state != null && state.getWorkspaceState() != null) {
            rightSidebar.getChildren().add(buildPlatformStatusPanel(state.getWorkspaceState().platformStatus()));
        }
    }

    private List<WorkspaceProject> filterProjects(List<WorkspaceProject> projects) {
        List<WorkspaceProject> result = new ArrayList<>();
        for (WorkspaceProject p : projects) {
            if (!searchQuery.isEmpty()) {
                if (!p.name().toLowerCase().contains(searchQuery) && 
                    !p.location().toLowerCase().contains(searchQuery)) {
                    continue;
                }
            }
            // Filter by active sidebar tab
            if (currentTab.equals("Pinned") && !p.pinned()) continue;
            if (currentTab.equals("Archived") && p.status() != ProjectStatus.ARCHIVED) continue;
            if (!currentTab.equals("Archived") && p.status() == ProjectStatus.ARCHIVED) continue;

            result.add(p);
        }
        return result;
    }

    private void buildSidebarTabs(WorkspaceState wsState) {
        sidebarTabs.getStyleClass().add("workspace-home-sidebar");
        
        String[] tabs = {"Recent", "Pinned", "Archived"};
        for (String tab : tabs) {
            Button btn = new Button(tab);
            btn.getStyleClass().add("sidebar-tab-btn");
            if (tab.equals(currentTab)) {
                btn.getStyleClass().add("sidebar-tab-btn-active");
            }
            btn.setOnAction(e -> {
                currentTab = tab;
                render();
            });
            sidebarTabs.getChildren().add(btn);
        }
        // Insert at index 0 of splitLayout or left side if sidebar not added yet
        if (!splitLayout.getChildren().contains(sidebarTabs)) {
            splitLayout.getChildren().add(0, sidebarTabs);
        }
    }

    private Pane buildHeroCard(WorkspaceProject p, ProjectIntelligence intel) {
        VBox hero = new VBox(15);
        hero.getStyleClass().add("project-card");
        hero.setStyle("-fx-background-color: rgba(13, 20, 26, 0.85); -fx-border-color: rgba(0, 218, 243, 0.2); -fx-border-width: 2;");

        Label label = new Label("CONTINUE WORKING");
        label.getStyleClass().add("workspace-home-subtitle");
        
        Label title = new Label(p.name());
        title.getStyleClass().add("project-card-title");
        title.setStyle("-fx-font-size: 20px;");

        Label meta = new Label(p.language() + " · " + p.buildTool() + " · " + p.location());
        meta.getStyleClass().add("project-card-meta");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(title);
        
        if (p.pinned()) {
            Label pinBadge = new Label("★ PINNED");
            pinBadge.getStyleClass().add("reanalysis-badge");
            header.getChildren().add(pinBadge);
        }

        // Health Indicators
        HBox healthBox = new HBox(20);
        healthBox.setAlignment(Pos.CENTER_LEFT);
        if (intel != null) {
            healthBox.getChildren().addAll(
                buildHealthDot("Architecture", intel.architecture()),
                buildHealthDot("Scalability", intel.scalability())
            );
            if (intel.deltaDescription() != null && !intel.deltaDescription().isEmpty()) {
                Label deltaLabel = new Label(intel.deltaDescription());
                deltaLabel.getStyleClass().add("project-card-delta-text");
                healthBox.getChildren().add(deltaLabel);
            }
        } else {
            Label noAnalysis = new Label("No analysis recorded yet.");
            noAnalysis.getStyleClass().add("project-card-details");
            healthBox.getChildren().add(noAnalysis);
        }

        // Action Buttons
        Button continueBtn = new Button("Continue Project");
        continueBtn.getStyleClass().add("project-card-btn-continue");
        continueBtn.setOnAction(e -> onProjectSelected.accept(new File(p.location())));

        Button reanalyzeBtn = new Button("Reanalyze");
        reanalyzeBtn.getStyleClass().add("project-card-btn-reanalyze");
        reanalyzeBtn.setOnAction(e -> {
            // Reanalyze logic triggers load + direct analyze flag
            onProjectSelected.accept(new File(p.location()));
        });

        HBox btnBox = new HBox(12);
        btnBox.getChildren().addAll(continueBtn, reanalyzeBtn);

        hero.getChildren().addAll(label, header, meta, healthBox, btnBox);
        return hero;
    }

    private Pane buildProjectCard(WorkspaceProject p, ProjectIntelligence intel) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(p.name());
        title.getStyleClass().add("project-card-title");

        Label meta = new Label(p.language() + " · " + p.buildTool());
        meta.getStyleClass().add("project-card-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Star / Pin controls
        Button pinBtn = new Button(p.pinned() ? "★" : "☆");
        pinBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00daf3; -fx-font-size: 14px; -fx-cursor: hand;");
        pinBtn.setOnAction(e -> {
            WorkspaceManager.getInstance().getProjectManager().setPinned(p.id(), !p.pinned());
            WorkspaceManager.getInstance().getStore().saveProject(
                WorkspaceManager.getInstance().getStore().findProject(p.id()).get()
            );
            com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
        });

        topRow.getChildren().addAll(title, meta, spacer, pinBtn);

        HBox healthBox = new HBox(15);
        healthBox.setAlignment(Pos.CENTER_LEFT);
        if (intel != null) {
            healthBox.getChildren().addAll(
                buildHealthDot("Arch", intel.architecture()),
                buildHealthDot("Scalability", intel.scalability())
            );
        } else {
            Label details = new Label("Not analyzed");
            details.getStyleClass().add("project-card-details");
            healthBox.getChildren().add(details);
        }

        // Action controls
        Button loadBtn = new Button("Open");
        loadBtn.getStyleClass().add("project-card-btn-continue");
        loadBtn.setStyle("-fx-padding: 4 10 4 10; -fx-font-size: 11px;");
        loadBtn.setOnAction(e -> onProjectSelected.accept(new File(p.location())));

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle(
            "-fx-background-color: rgba(255, 180, 171, 0.05);" +
            "-fx-text-fill: #ffb4ab;" +
            "-fx-border-color: rgba(255, 180, 171, 0.2);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 4 10 4 10;" +
            "-fx-font-size: 11px;" +
            "-fx-cursor: hand;"
        );
        removeBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Project");
            alert.setHeaderText("Remove from Recent List?");
            alert.setContentText("This will remove '" + p.name() + "' from the Home screen history. This does not delete any files on your disk.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    WorkspaceManager.getInstance().getProjectManager().deleteProject(p.id());
                    com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
                }
            });
        });

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Label pathLabel = new Label(p.location());
        pathLabel.getStyleClass().add("project-card-meta");
        pathLabel.setStyle("-fx-font-size: 9px;");
        
        bottomRow.getChildren().addAll(pathLabel, bottomSpacer, removeBtn, loadBtn);

        card.getChildren().addAll(topRow, healthBox, bottomRow);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                onProjectSelected.accept(new File(p.location()));
            }
        });

        return card;
    }

    private Pane buildHealthDot(String name, HealthStatus status) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4);
        dot.getStyleClass().add("status-indicator-dot-green");
        
        String styleClass = "project-card-health-val-good";
        if (status == HealthStatus.WARNING) {
            dot.getStyleClass().clear();
            dot.getStyleClass().add("status-indicator-dot-yellow");
            styleClass = "project-card-health-val-warning";
        } else if (status == HealthStatus.CRITICAL) {
            dot.getStyleClass().clear();
            dot.getStyleClass().add("status-indicator-dot-red");
            styleClass = "project-card-health-val-critical";
        }

        Label nameLabel = new Label(name.toUpperCase() + ":");
        nameLabel.getStyleClass().add("project-card-health-label");

        Label statusLabel = new Label(status.name());
        statusLabel.getStyleClass().add(styleClass);

        box.getChildren().addAll(dot, nameLabel, statusLabel);
        return box;
    }

    private Pane buildTimelineWidget(UUID projectId) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));

        Label title = new Label("ANALYSIS HISTORY");
        title.getStyleClass().add("action-card-title");
        box.getChildren().add(title);

        Map<LocalDate, List<AnalysisSession>> timeline = WorkspaceManager.getInstance().getTimeline(projectId);
        if (timeline.isEmpty()) {
            Label noHistory = new Label("No recent analysis sessions recorded.");
            noHistory.getStyleClass().add("project-card-details");
            box.getChildren().add(noHistory);
            return box;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        int count = 0;
        for (Map.Entry<LocalDate, List<AnalysisSession>> entry : timeline.entrySet()) {
            if (count++ > 3) break; // Only show latest 3 days of session timeline

            Label dateLabel = new Label(entry.getKey().format(formatter).toUpperCase());
            dateLabel.getStyleClass().add("timeline-title-date");
            box.getChildren().add(dateLabel);

            for (AnalysisSession session : entry.getValue()) {
                HBox sessionRow = new HBox(10);
                sessionRow.getStyleClass().add("timeline-item-box");
                
                Label sessionText = new Label("Analysis Run (" + session.reports().size() + " reports)");
                sessionText.getStyleClass().add("timeline-item-text");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                java.time.ZonedDateTime zdt = session.timestamp().atZone(ZoneId.systemDefault());
                Label timeLabel = new Label(zdt.format(DateTimeFormatter.ofPattern("hh:mm a")));
                timeLabel.getStyleClass().add("timeline-item-time");

                sessionRow.getChildren().addAll(sessionText, spacer, timeLabel);
                box.getChildren().add(sessionRow);
            }
        }

        return box;
    }

    private Pane buildPlatformStatusPanel(PlatformStatus status) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("action-card");

        Label title = new Label("PLATFORM STATUS");
        title.getStyleClass().add("action-card-title");
        panel.getChildren().add(title);

        boolean isReady = status != null && status.providerStatus() == com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.READY;

        HBox aiStatus = new HBox(8);
        aiStatus.setAlignment(Pos.CENTER_LEFT);
        Circle aiDot = new Circle(4);
        if (isReady) {
            aiDot.getStyleClass().add("status-indicator-dot-green");
        } else {
            aiDot.getStyleClass().add("status-indicator-dot-red");
        }
        String pName = (status != null && status.preferredProviderName() != null && !status.preferredProviderName().isBlank())
            ? status.preferredProviderName()
            : "None";
        String pStateStr = isReady ? " (Ready)" : " (Offline)";
        Label aiLabel = new Label("AI Provider: " + pName + pStateStr);
        aiLabel.getStyleClass().add("project-card-details");
        aiStatus.getChildren().addAll(aiDot, aiLabel);

        HBox modelStatus = new HBox(8);
        modelStatus.setAlignment(Pos.CENTER_LEFT);
        Circle modelDot = new Circle(4);
        boolean hasModel = status != null && status.activeModelName() != null && !status.activeModelName().equals("None");
        if (isReady && hasModel) {
            modelDot.getStyleClass().add("status-indicator-dot-green");
        } else {
            modelDot.getStyleClass().add("status-indicator-dot-yellow");
        }
        String mName = (status != null && status.activeModelName() != null) ? status.activeModelName() : "None";
        Label modelLabel = new Label("Active Model: " + mName);
        modelLabel.getStyleClass().add("project-card-details");
        modelStatus.getChildren().addAll(modelDot, modelLabel);

        HBox compStatus = new HBox(8);
        compStatus.setAlignment(Pos.CENTER_LEFT);
        Circle compDot = new Circle(4);
        compDot.getStyleClass().add("status-indicator-dot-green");
        Label compLabel = new Label("Compiler: READY");
        compLabel.getStyleClass().add("project-card-details");
        compStatus.getChildren().addAll(compDot, compLabel);

        panel.getChildren().addAll(aiStatus, modelStatus, compStatus);
        return panel;
    }

    private Pane buildQuickActionsPanel(List<WorkspaceProject> allProjects) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("action-card");

        Label title = new Label("QUICK ACTIONS");
        title.getStyleClass().add("action-card-title");
        panel.getChildren().add(title);

        Button openBtn = new Button("Open Project Folder");
        openBtn.getStyleClass().add("action-card-btn");
        openBtn.setOnAction(e -> triggerOpenFolder());

        Button reopenBtn = new Button("Reopen Last Project");
        reopenBtn.getStyleClass().add("action-card-btn");
        if (!allProjects.isEmpty()) {
            reopenBtn.setOnAction(e -> onProjectSelected.accept(new File(allProjects.get(0).location())));
        } else {
            reopenBtn.setDisable(true);
        }

        panel.getChildren().addAll(openBtn, reopenBtn);
        return panel;
    }

    private void triggerOpenFolder() {
        Stage stage = (Stage) getScene().getWindow();
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Project Folder");
        File selected = dc.showDialog(stage);
        if (selected != null) {
            onProjectSelected.accept(selected);
        }
    }
}
