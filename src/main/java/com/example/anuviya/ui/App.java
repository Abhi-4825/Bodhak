package com.example.anuviya.ui;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.EntityViewModel;
import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.orchestration.AppController;
import com.example.anuviya.orchestration.incremental.ProjectWatcherService;
import com.example.anuviya.frontend.java.JavaLanguageFrontend;
import com.example.anuviya.frontend.python.PythonLanguageFrontend;

import com.example.anuviya.event.dispatch.DispatchProfile;
import com.example.anuviya.event.dispatch.FixedIntervalPolicy;
import com.example.anuviya.event.dispatch.UIEventBus;
import com.example.anuviya.event.dispatch.UpdateDispatcher;
import com.example.anuviya.event.handler.*;
import com.example.anuviya.event.store.UIStore;
import com.example.anuviya.ui.Front.FileTreeNodeFactory;
import com.example.anuviya.ui.Optimization.OptimizationController;
import com.example.anuviya.ui.ProjectAnalysis.ProjectAnalysisUi;
import com.example.anuviya.ui.ProjectAnalysis.state.ProjectAnalysisState;
import com.example.anuviya.ui.nav.BodhakNavBar;
import com.example.anuviya.ui.nav.NavTab;
import com.example.anuviya.ui.nav.OverviewPanel;
import com.example.anuviya.ui.nav.workspace.WorkspaceRouter;
import com.example.anuviya.ui.overviewButton.HealthAnalyserView;
import com.example.anuviya.ui.overviewButton.MethodView;
import com.example.anuviya.ui.overviewButton.ModernDependencyView;
import com.example.anuviya.ui.rightPanel.RightPanelTabManager;
import com.example.anuviya.ui.helper.UiFeatures;
import com.example.anuviya.ui.main.MainScreen.ASTViewer;
import com.example.anuviya.ui.main.MainScreen.HomeScreen;
import com.example.anuviya.ui.workspace.EditorWorkspace;
import com.example.anuviya.ui.workspace.TabStateManager;

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
import java.util.List;
import java.util.Map;

public class App extends Application {

    // Infrastructure
    private AppController appController;
    private ProjectWatcherService watcherService;
    private UIEventBus uiEventBus;
    private UpdateDispatcher dispatcher;
    private UIStore uiStore;
    private ProjectAnalysisState projectAnalysisState;

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
    private Button optimizeBtn;
    private Label progressLabel;
    private ProgressBar progressBar;

    private File projectFolder;
    private BorderPane root;
    private StackPane windowStack;
    private SplitPane splitPane;
    private Node sidebar;
    private boolean sidebarExpanded = false;

    // Per-tab workspace state manager
    private TabStateManager tabStateManager;

    @Override
    public void start(Stage stage) throws Exception {
        PlaceHolderUi placeHolder = new PlaceHolderUi();

        root = new BorderPane();
        windowStack = new StackPane(root);
        
        // Add StartupOverlay to cover root content during boot
        com.example.anuviya.ui.startup.StartupOverlay startupOverlay = new com.example.anuviya.ui.startup.StartupOverlay(windowStack, () -> showWorkspaceHome(stage));
        windowStack.getChildren().add(startupOverlay);

        mainScene = new Scene(windowStack);
        applyTheme(mainScene);
        stage.setScene(mainScene);
        stage.setTitle("Anuviya – Project Analyser");
        com.example.anuviya.ui.helper.IconHelper.applyWindowIcon(stage);
        stage.setMaximized(true);
        stage.show();

        // Launch the bootstrap sequence
        startupOverlay.startSequence();

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
        optimizeBtn = new Button("Quick Optimization");
        optimizeBtn.setVisible(false);
        optimizeBtn.getStyleClass().add("action-btn-primary");

        HBox actionBar = new HBox(15);
        actionBar.getStyleClass().add("action-bar");
        actionBar.setPadding(new Insets(8, 12, 8, 12));
        actionBar.setAlignment(Pos.CENTER);
        HBox.setHgrow(optimizeBtn, Priority.ALWAYS);
        optimizeBtn.setMaxWidth(Double.MAX_VALUE);
        actionBar.getChildren().add(optimizeBtn);

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
        sidebar = homeScreen.createSidebar(fileTreeView);
        if (sidebar instanceof Region) {
            ((Region) sidebar).setMinWidth(180);
        }
        editorWorkspace.setMinWidth(350);
        rightPanel.setMinWidth(280);

        splitPane = new SplitPane();
        splitPane.getItems().addAll(editorWorkspace, rightPanel);
        splitPane.setDividerPositions(0.42);

        splitPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (newWidth != null && newWidth.doubleValue() < 1024.0) {
                if (sidebarExpanded) {
                    Platform.runLater(this::toggleSidebar);
                }
            }
        });

        // ── WorkspaceRouter — owns all workspaces, drives navigation ──────────
        // root.setCenter() is called ONCE here and never again on tab change.
        workspaceRouter = new WorkspaceRouter(splitPane, overviewPanel, this::toggleSidebar);
        projectAnalysisUi.setOnNavigate(workspaceRouter::show);
        navBar = new BodhakNavBar(workspaceRouter::show);

        root.setCenter(placeHolder.createCenterPlaceholder());
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

        optimizeBtn.setOnAction(e -> {
            new OptimizationController(rightPanelTabManager, appController.getAnalysisContextManager().getCurrentContext(), uiFeatures)
                    .startOptimization();
            optimizeBtn.setText("Refresh Quick Optimization");
        });

        astBtn.setOnAction(e -> showASTWindow(codeTabPane));


        overviewBtn.setOnAction(e -> {
            Map<String, EntityViewModel> vmMap = appController != null
                    ? appController.getViewModelBuilder().getViewModelMap()
                    : Map.of();
            MethodView methodView = new MethodView(uiFeatures, vmMap);
            ModernDependencyView classDependencyView = new ModernDependencyView(vmMap);
            HealthAnalyserView healthAnalyserView = new HealthAnalyserView(vmMap, () -> 
                appController != null ? appController.getAnalysisContextManager().getCurrentContext() : null
            );

            Tab selectedTab = codeTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab == null) return;
            File file = (File) selectedTab.getUserData();
            List<EntityInfo> classes = uiStore != null
                    ? uiStore.getEntities().stream()
                    .map(EntityViewModel::getEntity)
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
        loadProject(selected, stage);
    }

    private void loadProject(File folder, Stage stage) {
        // Restore top & bottom bars with home + recent project navigation support
        root.setTop(homeScreen.createTopBar(
            button -> button.setOnAction(e -> openProject(stage)),
            navBar.build(),
            () -> showWorkspaceHome(stage),
            f -> loadProject(f, stage)
        ));
        root.setBottom(homeScreen.createBottomBar(progressBar, progressLabel));
        
        // Show Editor Workspace
        root.setCenter(workspaceRouter.getRoot());
        
        // Open project in workspace subsystem
        projectFolder = folder;
        var proj = com.example.anuviya.workspace.WorkspaceManager.getInstance().openProject(folder);
        com.example.anuviya.workspace.WorkspaceManager.getInstance().setCurrentProject(proj);
        
        // Rebuild platform state to reflect the opened project in recent list
        com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
        
        // Save last opened project preference
        var prefs = com.example.anuviya.workspace.WorkspaceManager.getInstance().loadPreferences();
        var updated = new com.example.anuviya.workspace.model.WorkspacePreferences(
            prefs.theme(), prefs.lastSelectedModel(), proj.id().toString(), prefs.sidebarWidth(),
            mainScene.getWidth(), mainScene.getHeight(), stage.isMaximized()
        );
        com.example.anuviya.workspace.WorkspaceManager.getInstance().savePreferences(updated);
        
        codeTabPane.getTabs().clear();
        rightTabPane.getTabs().clear();
        rightPanelTabManager.clear();
        if (tabStateManager != null) tabStateManager.clearAll();
        fileTreeNodeFactory = new FileTreeNodeFactory();
        TreeItem<File> treeRoot = fileTreeNodeFactory.createNode(projectFolder);
        fileTreeView.setRoot(treeRoot);
        fileTreeView.setShowRoot(true);
        optimizeBtn.setVisible(false);
        startBackgroundProjectLoad(projectFolder);
    }

    private void showWorkspaceHome(Stage stage) {
        root.setTop(null);
        root.setBottom(null);
        // Force rebuild state to ensure we render the freshest project list
        com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
        com.example.anuviya.ui.workspace.home.WorkspaceHomeScreen homeScreenPanel =
            new com.example.anuviya.ui.workspace.home.WorkspaceHomeScreen(windowStack, folder -> loadProject(folder, stage));
        root.setCenter(homeScreenPanel);
    }

    private void startBackgroundProjectLoad(File folder) {
        // Tear down any existing dispatcher/watcher for the old project
        stopCurrentProject();

        projectAnalysisState = new ProjectAnalysisState();
        projectAnalysisUi.setAnalysisState(projectAnalysisState);
        overviewPanel.setAnalysisState(projectAnalysisState);

        // Register window container to overlay manager
        com.example.anuviya.orchestration.progress.LoadingOverlayManager.getInstance().setWindowStack(windowStack, root);

        Task<AppController> loadTask = new Task<>() {
            @Override
            protected AppController call() {
                com.example.anuviya.context.state.AppState appState = new com.example.anuviya.context.state.AppState(
                    projectAnalysisState,
                    context -> {},
                    context -> {},
                    context -> {},
                    context -> {}
                );
                AppController ctx = new AppController(appState);

                try { ctx.getRegistry().register(new JavaLanguageFrontend(List.of(folder.toPath()))); }
                catch (Exception e) { System.out.println("[Boot] No Java support: " + e.getMessage()); }

                try { ctx.getRegistry().register(new PythonLanguageFrontend()); }
                catch (Exception e) { System.out.println("[Boot] No Python support: " + e.getMessage()); }

                ctx.getEngine().analyze(folder.toPath());
                return ctx;
            }
        };

        loadTask.setOnSucceeded(e -> {
            initAfterLoad(folder, loadTask.getValue());
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
            String msg = (ex != null && ex.getMessage() != null) ? ex.getMessage() : "Unknown error occurred during background loading.";
            com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
                new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.AnalysisFailed(msg)
            );
        });

        new Thread(loadTask, "Project-Loader-Thread").start();
    }

    private void initAfterLoad(File folder, AppController ctx) {
        this.appController = ctx;

        AnalysisEngine engine = ctx.getEngine();
        ProjectAnalysisState state = (ProjectAnalysisState) ctx.getAnalysisContextManager().getAppState().workspaceState();
        projectAnalysisUi.setAnalysisState(state);
        overviewPanel.setAnalysisState(state);

        // ── 1. Create a fresh per-project UIStore ─────────────────────────────
        uiStore = new UIStore();

        // Seed the store with the initial analysis result (on FX thread — called
        // from loadTask.setOnSucceeded which runs on FX thread)
        uiStore.setProjectInfo(engine.getProjectInfo());
        uiStore.setGraphSnapshot(engine.getGraphSnapshot());
        for (EntityInfo ei : ctx.getAnalysisContextManager().getCurrentContext().getEntities()) {
            uiStore.addEntities(List.of(new EntityViewModel(ei)));
        }


       // Testing
//
//        System.out.println("Architectural analysis testing");
//        AnalysisContext analysisContext = ctx.getAnalysisContextManager().getCurrentContext();
//        ArchitectureEvidenceBuilder architectureEvidenceBuilder=new ArchitectureEvidenceBuilder();
//        ArchitectureAnalysisEvidence evidence=architectureEvidenceBuilder.build(analysisContext, null);
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
                new EntityListHandler(uiStore),
                new LogAndProgressHandler(uiStore)
        );
        dispatcher = new UpdateDispatcher(
                uiEventBus,
                new FixedIntervalPolicy(DispatchProfile.BALANCED),
                handlers
        );
        dispatcher.start();

        // ── 4. Show action buttons ────────────────────────────────────────────
        Platform.runLater(() -> {
            optimizeBtn.setVisible(true);

            rightPanelTabManager.openAnalyzeTab(() -> projectAnalysisUi);

            // Propagate the new engine to every workspace — each updates in-place.
            // OverviewWorkspace wraps the splitPane and calls overviewPanel.update().
            // ArchitectureWorkspace clears stale graph state and rebuilds once.
            workspaceRouter.refreshAll(engine);
            navBar.select(NavTab.WORKSPACE);
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
        var engine = appController.getEngine();
        var frontendRegistry = engine.getFrontendRegistry();
        if (frontendRegistry == null) {
            rightPanelTabManager.openOverviewTab(file, new Label("AST Error: Engine frontend registry not initialized."));
            return;
        }
        var frontendOpt = frontendRegistry.forFile(file.toPath());
        if (frontendOpt.isEmpty()) {
            rightPanelTabManager.openOverviewTab(file, new Label("AST Error: File type not supported."));
            return;
        }
        try {
            var irRoot = frontendOpt.get().parseToIR(file.toPath());
            if (irRoot == null) {
                rightPanelTabManager.openOverviewTab(file, new Label("AST Error: Failed to parse tree. Check syntax."));
                return;
            }
            new ASTViewer().show(file, irRoot);
        } catch (Exception ex) {
            rightPanelTabManager.openOverviewTab(file, new Label("AST Error: " + ex.getMessage()));
        }
    }

    private void applyTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
    }

    private void toggleSidebar() {
        if (splitPane == null || sidebar == null) return;
        sidebarExpanded = !sidebarExpanded;
        if (sidebarExpanded) {
            if (!splitPane.getItems().contains(sidebar)) {
                splitPane.getItems().add(0, sidebar);
            }
            splitPane.setDividerPositions(0.18, 0.58);
        } else {
            splitPane.getItems().remove(sidebar);
            splitPane.setDividerPositions(0.42);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
