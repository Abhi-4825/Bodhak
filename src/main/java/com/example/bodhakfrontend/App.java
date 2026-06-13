package com.example.bodhakfrontend;

import com.example.bodhakfrontend.ai.context.AiPromptContext;
import com.example.bodhakfrontend.ai.context.AiPromptContextBuilder;
import com.example.bodhakfrontend.ai.evidence.builder.ArchitectureEvidenceBuilder;
import com.example.bodhakfrontend.ai.evidence.model.ArchitectureAnalysisEvidence;
import com.example.bodhakfrontend.ai.model.AnalysisType;
import com.example.bodhakfrontend.ai.prompt.ArchitecturePromptBuilder;
import com.example.bodhakfrontend.ai.service.ArchitectureAnalysisService;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Analysis.AnalysisIssue;
import com.example.bodhakfrontend.core.Analysis.AnalysisReport;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.incremental.EntityViewModel;
import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.engine.AppController;
import com.example.bodhakfrontend.engine.Performance.core.PerformanceAnalysisService;
import com.example.bodhakfrontend.engine.growth.core.GrowthAnalysisService;
import com.example.bodhakfrontend.engine.incremental.ProjectWatcherService;
import com.example.bodhakfrontend.languages.java.JavaLanguagePlugin;
import com.example.bodhakfrontend.languages.python.PythonLanguagePlugin;

import com.example.bodhakfrontend.sync.api.DispatchProfile;
import com.example.bodhakfrontend.sync.bus.FixedIntervalPolicy;
import com.example.bodhakfrontend.sync.bus.UIEventBus;
import com.example.bodhakfrontend.sync.bus.UpdateDispatcher;
import com.example.bodhakfrontend.sync.handler.*;
import com.example.bodhakfrontend.sync.store.UIStore;
import  com.example.bodhakfrontend.engine.GraphSnapshot;
import com.example.bodhakfrontend.ui.Front.FileTreeNodeFactory;
import com.example.bodhakfrontend.ui.Optimization.OptimizationController;
import com.example.bodhakfrontend.ui.PlaceHolderUi;
import com.example.bodhakfrontend.ui.ProjectAnalysis.ProjectAnalysisUi;
import com.example.bodhakfrontend.ui.nav.BodhakNavBar;
import com.example.bodhakfrontend.ui.nav.NavTab;
import com.example.bodhakfrontend.ui.nav.OverviewPanel;
import com.example.bodhakfrontend.ui.nav.workspace.WorkspaceRouter;
import com.example.bodhakfrontend.ui.performance.PerformanceTestingPanel;
import com.example.bodhakfrontend.ui.overviewButton.ClassDependencyView;
import com.example.bodhakfrontend.ui.overviewButton.HealthAnalyserView;
import com.example.bodhakfrontend.ui.overviewButton.MethodView;
import com.example.bodhakfrontend.ui.overviewButton.ModernDependencyView;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import com.example.bodhakfrontend.ui.helper.UiFeatures;
import com.example.bodhakfrontend.ui.main.MainScreen.ASTViewer;
import com.example.bodhakfrontend.ui.main.MainScreen.HomeScreen;
import com.example.bodhakfrontend.ui.workspace.EditorWorkspace;
import com.example.bodhakfrontend.ui.workspace.TabStateManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class App extends Application {

    // Infrastructure
    private AppController appController;
    private ProjectWatcherService watcherService;
    private UIEventBus uiEventBus;
    private UpdateDispatcher dispatcher;
    private UIStore uiStore;

    // Navigation
    private BodhakNavBar    navBar;
    private WorkspaceRouter workspaceRouter;
    private OverviewPanel   overviewPanel;

    // UI components
    private TreeView<File> fileTreeView;
    private TabPane codeTabPane;
    private TabPane rightTabPane;
    private RightPanelTabManager rightPanelTabManager;
    private TreeView<EntityInfo> dependencyTreeView;
    private ProjectAnalysisUi projectAnalysisUi;
    private UiFeatures uiFeatures;
    private FileTreeNodeFactory fileTreeNodeFactory;
    private Scene mainScene;
    private HomeScreen homeScreen = new HomeScreen();
    private Button analyzeBtn;
    private Button optimizeBtn;
    private Button performanceBtn;
    private Label progressLabel;
    private ProgressBar progressBar;

    private File projectFolder;

    // Per-tab workspace state manager
    private TabStateManager tabStateManager;

    @Override
    public void start(Stage stage) throws Exception {
        PlaceHolderUi placeHolder = new PlaceHolderUi();

        BorderPane root = new BorderPane();
        mainScene = new Scene(root);
        applyTheme(mainScene);
        stage.setScene(mainScene);
        stage.setTitle("Project Analyser");
        stage.setMaximized(true);
        stage.show();

        progressLabel = new Label();
        progressBar = new ProgressBar(0);

        // ── Code editor pane ──────────────────────────────────────────────────
        codeTabPane = new TabPane();
        codeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        uiFeatures = new UiFeatures(codeTabPane);
        projectAnalysisUi = new ProjectAnalysisUi(uiFeatures);

        overviewPanel = new OverviewPanel();

        // ── Right panel ───────────────────────────────────────────────────────
        rightTabPane = new TabPane();
        rightPanelTabManager = new RightPanelTabManager(rightTabPane);
        dependencyTreeView = new TreeView<>();
        dependencyTreeView.setShowRoot(false);
        dependencyTreeView.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // ── Action buttons ────────────────────────────────────────────────────
        analyzeBtn = new Button("Analyze");
        analyzeBtn.setVisible(false);
        analyzeBtn.getStyleClass().add("action-btn-primary");

        optimizeBtn = new Button("Optimize");
        optimizeBtn.setVisible(false);
        optimizeBtn.getStyleClass().add("action-btn-secondary");

        performanceBtn = new Button("⚡ Performance");
        performanceBtn.setVisible(false);
        performanceBtn.getStyleClass().add("action-btn-secondary");

        HBox actionBar = new HBox(15);
        actionBar.getStyleClass().add("action-bar");
        actionBar.setPadding(new Insets(8, 12, 8, 12));
        actionBar.setAlignment(Pos.CENTER);
        HBox.setHgrow(optimizeBtn, Priority.ALWAYS);
        HBox.setHgrow(analyzeBtn, Priority.ALWAYS);
        HBox.setHgrow(performanceBtn, Priority.ALWAYS);
        optimizeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        performanceBtn.setMaxWidth(Double.MAX_VALUE);
        actionBar.getChildren().addAll(optimizeBtn, analyzeBtn, performanceBtn);

        // ── Right panel stack ─────────────────────────────────────────────────
        StackPane rightStack = new StackPane();
        VBox rightPlaceholder = placeHolder.createRightPlaceholder();
        rightStack.getChildren().addAll(rightPlaceholder, rightTabPane);
        rightPlaceholder.visibleProperty().bind(Bindings.isEmpty(rightTabPane.getTabs()));
        rightPlaceholder.managedProperty().bind(rightPlaceholder.visibleProperty());
        rightTabPane.visibleProperty().bind(Bindings.isNotEmpty(rightTabPane.getTabs()));
        rightTabPane.managedProperty().bind(rightTabPane.visibleProperty());

        BorderPane rightPanel = new BorderPane();
        // The router drives the main right-panel view; the legacy tab pane floats on top
        // The overview panel drives the main right-panel view in IDE mode
        rightPanel.setCenter(overviewPanel.getRoot());
        rightPanel.setBottom(actionBar);

        // Overlay the legacy right tab pane in a StackPane so both can coexist.
        // Legacy tabs (Analyze, Optimize, file overview) open on top of router content.
        StackPane rightOverlay = new StackPane();
        rightTabPane.setPickOnBounds(false);
        rightTabPane.visibleProperty().bind(Bindings.isNotEmpty(rightTabPane.getTabs()));
        rightTabPane.managedProperty().bind(rightTabPane.visibleProperty());
        rightOverlay.getChildren().addAll(overviewPanel.getRoot(), rightTabPane);
        rightPanel.setCenter(rightOverlay);

        // ── Workspaces ────────────────────────────────────────────────────────
        Node emptyState = placeHolder.createCenterPlaceholder();
        EditorWorkspace editorWorkspace = new EditorWorkspace(codeTabPane, emptyState);
        emptyState.visibleProperty().bind(Bindings.isEmpty(codeTabPane.getTabs()));
        emptyState.managedProperty().bind(emptyState.visibleProperty());
        codeTabPane.visibleProperty().bind(Bindings.isNotEmpty(codeTabPane.getTabs()));
        codeTabPane.managedProperty().bind(codeTabPane.visibleProperty());

        Button astBtn = editorWorkspace.getAstBtn();
        Button overviewBtn = editorWorkspace.getOverviewBtn();


        // ── File tree ─────────────────────────────────────────────────────────
        fileTreeView = new TreeView<>();
        fileTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            public void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) { setText(null); setGraphic(null); }
                else {
                    setText(file.getName().isEmpty() ? file.getAbsolutePath() : file.getName());
                    TreeItem<File> item = getTreeItem();
                    if (item != null) setGraphic(item.getGraphic());
                }
            }
        });

        // ── Layout ───────────────────────────────────────────────────────────
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(homeScreen.createSidebar(fileTreeView), editorWorkspace, rightPanel);
        splitPane.setDividerPositions(0.18, 0.58);

        // ── WorkspaceRouter — owns all workspaces, drives navigation ──────────
        // root.setCenter() is called ONCE here and never again on tab change.
        workspaceRouter = new WorkspaceRouter(splitPane, overviewPanel);
        navBar = new BodhakNavBar(workspaceRouter::show);

        root.setCenter(workspaceRouter.getRoot());
        root.setTop(homeScreen.createTopBar(
            button -> button.setOnAction(e -> openProject(stage)),
            navBar.build()
        ));
        root.setBottom(homeScreen.createBottomBar(progressBar, progressLabel));
        root.getStyleClass().add("app-root");
        fileTreeView.getStyleClass().add("sidebar");
        codeTabPane.getStyleClass().add("editor");
        rightPanel.getStyleClass().add("right-panel");

        // ── Per-tab workspace state ───────────────────────────────────────
        tabStateManager = new TabStateManager(splitPane);
        // Restore sidebar when user switches tabs
        codeTabPane.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldTab, newTab) -> tabStateManager.onTabSwitched(newTab)
        );
        // Clean up per-tab analysis state when a tab is closed
        codeTabPane.getTabs().addListener(
            (javafx.collections.ListChangeListener<Tab>) change -> {
                while (change.next()) {
                    if (change.wasRemoved()) {
                        change.getRemoved().forEach(tabStateManager::onTabClosed);
                    }
                }
            }
        );

        // ── Button visibility bindings ────────────────────────────────────────
        BooleanBinding hasTab = codeTabPane.getSelectionModel().selectedItemProperty().isNotNull();
        overviewBtn.disableProperty().bind(hasTab.not());
        astBtn.disableProperty().bind(hasTab.not());
        overviewBtn.visibleProperty().bind(hasTab);
        astBtn.visibleProperty().bind(hasTab);

        // ── Event handlers ────────────────────────────────────────────────────
        fileTreeView.setOnMouseClicked(event -> {
            var selected = fileTreeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            File file = selected.getValue();
            if (file.isFile() && event.getClickCount() == 2) uiFeatures.openFile(file);
        });

        dependencyTreeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                var item = dependencyTreeView.getSelectionModel().getSelectedItem();
                if (item == null) return;
                EntityInfo node = item.getValue();
                uiFeatures.openAndHighlight(node.getEntityName(), node.getBeginLine(),
                        node.getBeginColumn(), node.getSourceFile());
            }
        });

        analyzeBtn.setOnAction(e -> {
            if (uiStore == null) {
                rightPanelTabManager.openAnalyzeTab(() -> new Label("No project loaded."));
                return;
            }
            // Always uses the live projectInfo from the store — never a stale snapshot
            rightPanelTabManager.openAnalyzeTab(
                    () -> projectAnalysisUi.build(uiStore.getProjectInfo())
            );
        });

        optimizeBtn.setOnAction(e -> {
            if (uiStore == null || uiStore.getProjectInfo() == null) return;
            new OptimizationController(rightPanelTabManager, uiStore.getProjectInfo(), uiFeatures)
                    .startOptimization();
            optimizeBtn.setText("Refresh");
        });

        performanceBtn.setOnAction(e -> {
            var apiSurface = appController != null
                    ? appController.getEngine().getApiSurface()
                    : null;
            rightPanelTabManager.openPerformanceTab(
                () -> new PerformanceTestingPanel().build(apiSurface)
            );
        });

        astBtn.setOnAction(e -> showASTWindow(codeTabPane));


        overviewBtn.setOnAction(e -> {
            Map<String, EntityViewModel> vmMap = appController != null
                    ? appController.getViewModelBuilder().getViewModelMap()
                    : Map.of();
            MethodView methodView = new MethodView(uiFeatures, vmMap);
            ModernDependencyView classDependencyView = new ModernDependencyView(vmMap);
            HealthAnalyserView healthAnalyserView = new HealthAnalyserView(vmMap);

            Tab selectedTab = codeTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab == null) return;
            File file = (File) selectedTab.getUserData();
            List<EntityInfo> classes = uiStore != null
                    ? uiStore.getProjectInfo().getEntities().stream()
                    .filter(ei -> ei.getSourceFile().toPath().toAbsolutePath().normalize()
                            .equals(file.toPath().toAbsolutePath().normalize()))
                    .toList()
                    : List.of();


            // Build the analysis views for this file's entities
            ComboBox<EntityInfo> classSelector = new ComboBox<>();
            classSelector.getItems().addAll(classes);
            classSelector.getSelectionModel().selectFirst();

            EntityInfo first = classSelector.getValue();
            if (first == null) return;

            // When user picks a different class, update the analysis content in-place
            classSelector.setOnAction(event -> {
                EntityInfo ei = classSelector.getValue();
                if (ei == null) return;
                tabStateManager.enterAnalysisMode(selectedTab,
                        classDependencyView.show(ei.getEntityName()),
                        methodView.show(ei.getEntityName()),
                        healthAnalyserView.show(ei.getEntityName()),
                        null // selector already set; don't reset
                );
            });

            // Enter analysis mode for THIS tab only – other tabs are unaffected
            tabStateManager.enterAnalysisMode(selectedTab,
                    classDependencyView.show(first.getEntityName()),
                    methodView.show(first.getEntityName()),
                    healthAnalyserView.show(first.getEntityName()),
                    classSelector
            );
        });
    }

    // ── Project loading ───────────────────────────────────────────────────────

    private void openProject(Stage stage) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Project Folder");
        File selected = dc.showDialog(stage);
        if (selected == null) return;
        projectFolder = selected;

        codeTabPane.getTabs().clear();
        rightTabPane.getTabs().clear();
        rightPanelTabManager.clear();
        if (tabStateManager != null) tabStateManager.clearAll();
        fileTreeNodeFactory = new FileTreeNodeFactory();
        TreeItem<File> root = fileTreeNodeFactory.createNode(projectFolder);
        fileTreeView.setRoot(root);
        fileTreeView.setShowRoot(true);
        analyzeBtn.setVisible(false);
        optimizeBtn.setVisible(false);
        startBackgroundProjectLoad(projectFolder);
    }

    private void startBackgroundProjectLoad(File folder) {
        // Tear down any existing dispatcher/watcher for the old project
        stopCurrentProject();

        Task<AppController> loadTask = new Task<>() {
            @Override
            protected AppController call() {
                updateMessage("Initialising plugins…");
                updateProgress(-1, 0); // Trigger indeterminate animation
                AppController ctx = new AppController();

                try { ctx.getRegistry().register(new JavaLanguagePlugin(List.of(folder.toPath()))); }
                catch (Exception e) { System.out.println("[Boot] No Java support: " + e.getMessage()); }

                try { ctx.getRegistry().register(new PythonLanguagePlugin()); }
                catch (Exception e) { System.out.println("[Boot] No Python support: " + e.getMessage()); }

                updateMessage("Scanning project…");
                ctx.getEngine().analyze(folder.toPath());
                updateMessage("Done.");
                return ctx;
            }
        };

        // Show progress via labels (not bound directly so we control visibility)
        progressLabel.textProperty().bind(loadTask.messageProperty());
        progressBar.progressProperty().bind(loadTask.progressProperty());
        progressBar.setVisible(true);
        progressLabel.setVisible(true);

        loadTask.setOnSucceeded(e -> {
            progressLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            progressLabel.setVisible(false);
            progressLabel.setText("");
            initAfterLoad(folder, loadTask.getValue());

        });

        loadTask.setOnFailed(e -> {
            progressLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            progressLabel.setText("Load failed — check logs.");
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask, "Project-Loader-Thread").start();
    }

    private void initAfterLoad(File folder, AppController ctx) {
        this.appController = ctx;
        AnalysisEngine engine = ctx.getEngine();

        // ── 1. Create a fresh per-project UIStore ─────────────────────────────
        uiStore = new UIStore();

        // Bind the global progress indicators to the current project's store
        progressBar.progressProperty().bind(uiStore.progressFractionProperty());
        progressLabel.textProperty().bind(uiStore.progressLabelProperty());

        // Seed the store with the initial analysis result (on FX thread — called
        // from loadTask.setOnSucceeded which runs on FX thread)
        uiStore.setProjectInfo(engine.getProjectInfo());
        uiStore.setGraphSnapshot(engine.getGraphSnapshot());
        for (EntityInfo ei : engine.getProjectInfo().getEntities()) {
            uiStore.addEntities(List.of(new EntityViewModel(ei)));
        }

       // Testing

//        System.out.println("Architectural analysis testing");
//        AnalysisContext analysisContext=new AnalysisContext(engine.getProjectInfo(),engine.getDependencyGraph(),engine.getProjectInfo().getEntities());
//        ArchitectureEvidenceBuilder architectureEvidenceBuilder=new ArchitectureEvidenceBuilder();
//        ArchitectureAnalysisEvidence evidence=architectureEvidenceBuilder.build(analysisContext);
//
//       String prompt=new ArchitecturePromptBuilder().build(evidence);
//        System.out.println(prompt);
//
//        ArchitectureAnalysisService service=new ArchitectureAnalysisService();
//        try {
//            String result=service.analyze(analysisContext);
//            System.out.println(result);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        // test code end


        // ── 2. Create UIEventBus ──────────────────────────────────────────────
        uiEventBus = new UIEventBus();

        // ── 3. Build and start UpdateDispatcher with BALANCED profile ─────────
        List<UiUpdateHandler> handlers = List.of(
                new FileTreeHandler(fileTreeView, fileTreeNodeFactory, uiStore),
                new EditorHandler(codeTabPane, rightPanelTabManager),
                new ProjectSummaryHandler(uiStore, rightPanelTabManager, projectAnalysisUi),
                new EntityListHandler(uiStore),
                new LogAndProgressHandler(uiStore),
                new UiUpdateHandler() {
                    @Override
                    public boolean canHandle(com.example.bodhakfrontend.sync.api.UiUpdateEvent event) {
                        return event instanceof com.example.bodhakfrontend.sync.events.ProjectSummaryChangedEvent;
                    }

                    @Override
                    public void apply(com.example.bodhakfrontend.sync.api.UiUpdateEvent event) {
                        // Incremental project changes — refresh the overview panel only.
                        // Full workspace refresh (incl. Architecture) happens in initAfterLoad
                        // when a new project is loaded.
                        overviewPanel.clearCache();
                        overviewPanel.update(appController != null ? appController.getEngine() : null);
                    }
                }
        );
        dispatcher = new UpdateDispatcher(
                uiEventBus,
                new FixedIntervalPolicy(DispatchProfile.BALANCED),
                handlers
        );
        dispatcher.start();

        // ── 4. Show action buttons ────────────────────────────────────────────
        Platform.runLater(() -> {
            analyzeBtn.setVisible(true);
            optimizeBtn.setVisible(true);
            performanceBtn.setVisible(true);

            // Propagate the new engine to every workspace — each updates in-place.
            // OverviewWorkspace wraps the splitPane and calls overviewPanel.update().
            // ArchitectureWorkspace clears stale graph state and rebuilds once.
            workspaceRouter.refreshAll(engine);
            navBar.select(NavTab.OVERVIEW);
        });

        // ── 5. Start live file watcher (uses the new UIEventBus) ─────────────
        try {
            watcherService = new ProjectWatcherService(engine, uiEventBus);
            watcherService.watch(folder.toPath());
        } catch (Exception ex) {
            System.err.println("[Watcher] Could not start file watcher: " + ex.getMessage());
        }

    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    private void stopCurrentProject() {
        if (progressBar != null)   { progressBar.progressProperty().unbind(); }
        if (progressLabel != null) { progressLabel.textProperty().unbind(); progressLabel.setText(""); }
        if (dispatcher != null)    { dispatcher.stop();    dispatcher = null; }
        if (watcherService != null) { watcherService.stop(); watcherService = null; }
        if (uiStore != null)        { uiStore.reset();       uiStore = null; }
        uiEventBus = null;
    }

    @Override
    public void stop() throws Exception {
        stopCurrentProject();
        System.out.println("Shutting down application…");
        super.stop();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showASTWindow(TabPane codeTabPane) {
        Tab selectedTab = codeTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        File file = (File) selectedTab.getUserData();
        var pluginOpt = appController.getEngine().getPluginRegistry().forFile(file.toPath());
        if (pluginOpt.isEmpty()) {
            rightPanelTabManager.openOverviewTab(file, new Label("AST Error: File type not supported."));
            return;
        }
        var astProvider = pluginOpt.get().getAstProvider();
        if (astProvider == null) {
            rightPanelTabManager.openOverviewTab(file, new Label("AST Error: Plugin does not provide an AST."));
            return;
        }
        var genericRoot = astProvider.parse(file.toPath());
        if (genericRoot == null) {
            rightPanelTabManager.openOverviewTab(file, new Label("AST Error: Failed to parse tree. Check syntax."));
            return;
        }
        new ASTViewer().show(file, genericRoot);
    }

    private void applyTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
