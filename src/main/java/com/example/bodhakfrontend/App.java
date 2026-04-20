package com.example.bodhakfrontend;

import com.example.bodhakfrontend.Backend.Analysis.Engine.AnalysisEngine;
import com.example.bodhakfrontend.Backend.ClassGraphBuilder;
import com.example.bodhakfrontend.Backend.IncrementalPart.Update.EventBus;
import com.example.bodhakfrontend.Backend.IncrementalPart.Update.IncrementalAnalyzer;
import com.example.bodhakfrontend.Backend.IncrementalPart.Update.ProjectFileListener;
import com.example.bodhakfrontend.Backend.IncrementalPart.Update.ProjectFileWatcher;
import com.example.bodhakfrontend.Backend.IncrementalPart.UpdateManager;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;
import com.example.bodhakfrontend.Backend.IncrementalPart.Update.UiRefreshEvent;
import com.example.bodhakfrontend.Backend.models.incrementalModel.ClassInfoViewModel;
import com.example.bodhakfrontend.FrontEnd.MainScreen.ASTViewer;
import com.example.bodhakfrontend.FrontEnd.MainScreen.HomeScreen;
import com.example.bodhakfrontend.Parser.AstLabelProvider;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.ui.Front.FileTreeNodeFactory;
import com.example.bodhakfrontend.ui.Optimization.OptimizationController;
import com.example.bodhakfrontend.ui.OverviewContentFactory;
import com.example.bodhakfrontend.ui.PlaceHolderUi;
import com.example.bodhakfrontend.ui.ProjectAnalysis.ProjectAnalysisUi;
import com.example.bodhakfrontend.ui.UiRerfeshController;
import com.example.bodhakfrontend.ui.overviewButton.*;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Parser.javaParseCache;
import com.github.javaparser.ast.CompilationUnit;
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
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.stage.DirectoryChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class App extends Application {
    LanguageDetector detector = new LanguageDetector();
    javaParseCache cache ;
    JavaFileParser javaFileParser ;
    Parsermanager  parsermanager ;
    MultiModuleSourceRootDetector rootDetector = new MultiModuleSourceRootDetector();
    private TextArea outputPanel;
    private TreeView<File> FileTreeView;
    private File projectFolder;
    private List<Path> srcRoot;
    private Set<String> srcClasses;
    private TabPane codeTabPane;
    private TabPane rightTabPane;
    private UiFeatures  uiFeatures;
    private RightPanelTabManager rightPanelTabManager;
    private TreeView<ClassInfo> dependencyTreeView;
    private BorderPane rightPanel;
    private TreeItem<File> selected;

    private ProgressBar globalProgressBar;
    private Label progressLabel;
    private AnalysisEngine analysisEngine;
    private ProjectInfo projectInfo;
    private Button analyzeBtn;
    private Button optimizeBtn;
    private ProjectAnalysisUi projectAnalysisUi;
    private UpdateManager updateManager;
    private Map<String, ClassInfoViewModel> vmMap;
    private ProjectFileWatcher watcher;

    private HomeScreen homeScreen=new  HomeScreen();


    private FileTreeNodeFactory fileTreeNodeFactory;

    /** UI-only: current theme. Light by default. */
    private boolean isDarkMode = false;
    /** UI-only: main scene for theme switching. */
    private Scene mainScene;
    private ClassGraphBuilder classGraphBuilder;

    @Override
    public void start(Stage stage) throws Exception {
        PlaceHolderUi placeHolderUi=new PlaceHolderUi();

        BorderPane root = new BorderPane();
        mainScene = new Scene(root);
        applyTheme(mainScene);
        stage.setScene(mainScene);
        stage.setTitle("Project Analyser");
        stage.setMaximized(true);
        stage.show();
        globalProgressBar=new ProgressBar();

        globalProgressBar.setVisible(false);

        progressLabel = new Label();




        HBox toolbarSpacer = new HBox();
        BorderPane codeEditorPane=new BorderPane();
        HBox.setHgrow(toolbarSpacer, javafx.scene.layout.Priority.ALWAYS);

        root.setTop(homeScreen.createTopBar(button -> {
            button.setOnAction(e -> {
                DirectoryChooser directoryChooser=new DirectoryChooser();
                directoryChooser.setTitle("Select Project Folder");
                File selectedFolder=directoryChooser.showDialog(stage);
                if(selectedFolder==null){return;}
                projectFolder=selectedFolder;
                codeTabPane.getTabs().clear();
                rightTabPane.getTabs().clear();
                rightPanelTabManager.clear();
                fileTreeNodeFactory=new FileTreeNodeFactory();
                TreeItem<File> rootNode=fileTreeNodeFactory.createNode(projectFolder);
                FileTreeView.setRoot(rootNode);
                FileTreeView.setShowRoot(true);

                startBackgroundProjectLoad(projectFolder,globalProgressBar);

            });
        }));

        // for bottom

        analyzeBtn=new Button("Analyze");
        analyzeBtn.setVisible(false);

        // optimization button
        optimizeBtn=new Button("Optimize");
        optimizeBtn.setVisible(false);

        optimizeBtn.getStyleClass().add("action-btn-secondary");
        analyzeBtn.getStyleClass().add("action-btn-primary");


        HBox actionBar=new HBox(15);
        actionBar.getStyleClass().add("action-bar");
        actionBar.setPadding(new Insets(8, 12, 8, 12));
        actionBar.setAlignment(Pos.CENTER);
        HBox.setHgrow(optimizeBtn, Priority.ALWAYS);
        HBox.setHgrow(analyzeBtn, Priority.ALWAYS);
        optimizeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        actionBar.getChildren().addAll(optimizeBtn, analyzeBtn);
        BorderPane bottomBar = new BorderPane();
        bottomBar.setPadding(new Insets(8));
        bottomBar.setLeft(new HBox(10, globalProgressBar, progressLabel));
        bottomBar.setRight(actionBar);
        root.setBottom(homeScreen.createBottomBar(globalProgressBar,progressLabel));
        // for code view
        codeTabPane=new TabPane();
        uiFeatures=new UiFeatures(codeTabPane);
        projectAnalysisUi=new ProjectAnalysisUi(uiFeatures);
//        analysisViewBuilder=new AnalysisViewBuilder(uiFeatures);
        codeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        // for output
        rightTabPane=new TabPane();
        rightPanelTabManager=new RightPanelTabManager(rightTabPane);
        dependencyTreeView=new TreeView<>();
        dependencyTreeView.setShowRoot(false);
        dependencyTreeView.setPrefWidth(Region.USE_COMPUTED_SIZE);
        // default rightpanel
        StackPane rightStack = new StackPane();

        VBox rightPlaceholder = placeHolderUi.createRightPlaceholder();

        rightStack.getChildren().addAll(rightPlaceholder, rightTabPane);

//
        rightPlaceholder.visibleProperty().bind(Bindings.isEmpty(rightTabPane.getTabs()));
        rightPlaceholder.managedProperty().bind(rightPlaceholder.visibleProperty());

        rightTabPane.visibleProperty().bind(Bindings.isNotEmpty(rightTabPane.getTabs()));
        rightTabPane.managedProperty().bind(rightTabPane.visibleProperty());

        rightPanel = new BorderPane();
        rightPanel.setCenter(rightStack);
        rightPanel.setBottom(actionBar);
        // for files
        FileTreeView =new TreeView<>();
        //EditorPane buttons
        Button Ast = new Button("AST");
        Ast.getStyleClass().addAll("btn-secondary", "editor-bottom-btn");

        Button overviewBtn = new Button("OVERVIEW");
        overviewBtn.getStyleClass().addAll("btn-primary", "editor-bottom-btn");

        HBox editorBottom = new HBox(8);
        editorBottom.getStyleClass().add("editor-bottom-bar");
        editorBottom.getChildren().addAll(Ast,overviewBtn);
        editorBottom.setPadding(new Insets(6));
        editorBottom.setAlignment(Pos.CENTER_RIGHT);


        StackPane centerStack = new StackPane();
        Node emptyState = placeHolderUi.createCenterPlaceholder();
        centerStack.getChildren().addAll(emptyState, codeTabPane);
//
        emptyState.visibleProperty().bind(
                Bindings.isEmpty(codeTabPane.getTabs())
        );

        emptyState.managedProperty().bind(emptyState.visibleProperty());

        codeTabPane.visibleProperty().bind(
                Bindings.isNotEmpty(codeTabPane.getTabs())
        );

        codeTabPane.managedProperty().bind(codeTabPane.visibleProperty());

        codeEditorPane.setCenter(centerStack);
        codeEditorPane.setBottom(editorBottom);


        // we will make Split Pane for resizable part of centre
        SplitPane splitPane=new SplitPane();
        splitPane.getItems().addAll(homeScreen.createSidebar(FileTreeView),codeEditorPane,rightPanel);
        splitPane.setDividerPositions(0.18,0.58);
        root.setCenter(splitPane);
        root.getStyleClass().add("app-root");
        FileTreeView.getStyleClass().add("sidebar");
        codeTabPane.getStyleClass().add("editor");
        rightPanel.getStyleClass().add("right-panel");
        //for showing and disabling overview,ast btn
        BooleanBinding hasTab =
                codeTabPane.getSelectionModel()
                        .selectedItemProperty()
                        .isNotNull();

        overviewBtn.disableProperty().bind(hasTab.not());
        Ast.disableProperty().bind(hasTab.not());

        overviewBtn.visibleProperty().bind(hasTab);
        Ast.visibleProperty().bind(hasTab);



        // for only showing names of folder not path
        FileTreeView.setCellFactory(tv->new TreeCell<File>(){
            @Override
            public void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if(empty || file==null){
                    setText(null);
                    setGraphic(null);
                }
                else{
                    setText(file.getName().isEmpty()?file.getAbsolutePath():file.getName());
                    TreeItem<File> treeItem=getTreeItem();
                    if(treeItem!=null){
                        setGraphic(treeItem.getGraphic());
                    }
                }
            }
        });



//        root.setLeft(treeView);


        optimizeBtn.setOnAction(e -> {

            if (projectInfo == null) return;

            OptimizationController controller =
                    new OptimizationController(
                            rightPanelTabManager, projectInfo,uiFeatures
                    );

            controller.startOptimization();

            optimizeBtn.setText("Refresh");
        });


        // for dependencies tree view working
        dependencyTreeView.setOnMouseClicked(e -> {

            if(e.getClickCount()==2 ){
                TreeItem<ClassInfo> treeItem=dependencyTreeView.getSelectionModel().getSelectedItem();
                if(treeItem==null){return;}
                ClassInfo selectedNode=(ClassInfo)treeItem.getValue();
                uiFeatures.openAndHighlight(selectedNode.getClassName(),selectedNode.getBeginLine(),selectedNode.getBeginColumn(),selectedNode.getSourceFile());
            }
        });


        // for getting content of files when a file is clicked in Tree view
        FileTreeView.setOnMouseClicked(event -> {
            selected = FileTreeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            File file = selected.getValue();
            if (file.isFile()) {

//               openFileInViewer(file);
                if(event.getClickCount()==2){
                    uiFeatures.openFile(file);}
                String type = detector.detectFileType(file);

            }
        });



        // Analyze Button
        analyzeBtn.setOnAction(e -> {
            if (projectFolder == null ){ rightPanelTabManager.openAnalyzeTab(()->new Label("No project folder or class file found"));
                return;}

            rightPanelTabManager.openAnalyzeTab(()->projectAnalysisUi.build(projectInfo));
        });
        // Ast button functioning
        Ast.setOnAction(e -> showASTWindow(codeTabPane));
        // overview button
        overviewBtn.setOnAction(e ->{
            MethodView methodView=new MethodView(uiFeatures,vmMap);
            ClassDependencyView classDependencyView=new ClassDependencyView(uiFeatures,vmMap);
            HealthAnalyserView healthAnalyserView=new HealthAnalyserView(vmMap);

            Tab selectedTab = codeTabPane.getSelectionModel().getSelectedItem();
            if(selectedTab==null){return;}
            File file=(File)selectedTab.getUserData();
            List<ClassInfo> classes= analysisEngine.getClassInfoToPathMap().get(file.toPath().toAbsolutePath().normalize());
            OverviewContentFactory factory = classInfo -> {

                if (classInfo == null) {
                    return new Label("Class info not available");
                }

                return new OverviewView(
                        classDependencyView.show(classInfo.getClassName()),

                        methodView.show(classInfo.getClassName()),
                        healthAnalyserView.show(classInfo.getClassName())
                ).getRoot();
            };


            ClassOverviewContainer container=new ClassOverviewContainer(file,classes,factory);

            rightPanelTabManager.openOverviewTab(file,container.getRoot());
        });
    }

    // AST window
    private void showASTWindow(TabPane codeTabPane) {
        Tab selectedTab = codeTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            return;
        }
        File file = (File)selectedTab.getUserData();
        try {
            // Parse AST
            CompilationUnit cu =cache.parse(file.toPath());
            // Get correct label provider
            @SuppressWarnings("unchecked")
            AstLabelProvider<com.github.javaparser.ast.Node> labelProvider =
                    (AstLabelProvider<com.github.javaparser.ast.Node>)
                            parsermanager.getLabelProvider(file);

//            // 3 Build AST tree USING provider
//            TreeItem<String> astRoot = buildASTTree(cu, labelProvider);
//
//            // 4 Show window
//            showASTStage(file, astRoot);

            ASTViewer astViewer=new ASTViewer(labelProvider);
            astViewer.show(file,cu);


        } catch (Exception e) {
            outputPanel.setText("AST Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showASTStage(File file, TreeItem<String> astRoot) {

        TreeView<String> astTree = new TreeView<>(astRoot);
        astTree.setPrefWidth(Region.USE_COMPUTED_SIZE);

        TextArea codeArea = new TextArea();
        codeArea.setEditable(false);

        try {
            codeArea.setText(Files.readString(file.toPath()));
        } catch (Exception ignored) {}

        SplitPane split = new SplitPane(astTree, codeArea);
        split.setDividerPositions(0.4);

        Stage astStage = new Stage();
        Scene astScene = new Scene(split);
        astScene.getStylesheets().clear();
        String css = "/styles.styles.css";
        astScene.getStylesheets().add(getClass().getResource(css).toExternalForm());
        astStage.setScene(astScene);
        astStage.setTitle("AST Viewer - " + file.getName());
        astStage.setWidth(900);
        astStage.setHeight(600);
        astStage.show();
    }


    private TreeItem<String> buildASTTree(
            com.github.javaparser.ast.Node node,
            AstLabelProvider<com.github.javaparser.ast.Node> labelProvider) {

        // Hide low-level technical nodes
        if (node instanceof com.github.javaparser.ast.expr.Name ||
                node instanceof com.github.javaparser.ast.expr.SimpleName ||
                node instanceof com.github.javaparser.ast.Modifier) {
            return null;
        }

        // CompilationUnit → group imports
        if (node instanceof com.github.javaparser.ast.CompilationUnit) {

            com.github.javaparser.ast.CompilationUnit cu =
                    (com.github.javaparser.ast.CompilationUnit) node;

            TreeItem<String> root = new TreeItem<>("CompilationUnit");

            // ---- Imports Group ----
            if (!cu.getImports().isEmpty()) {
                TreeItem<String> importsNode = new TreeItem<>("Imports");

                for (com.github.javaparser.ast.ImportDeclaration imp : cu.getImports()) {
                    importsNode.getChildren().add(
                            new TreeItem<>("Import : " + imp.getNameAsString())
                    );
                }

                root.getChildren().add(importsNode);
            }

            // ---- Other nodes (classes, etc.) ----
            for (com.github.javaparser.ast.Node child : cu.getChildNodes()) {

                // Skip imports (already handled)
                if (child instanceof com.github.javaparser.ast.ImportDeclaration) {
                    continue;
                }

                TreeItem<String> childItem = buildASTTree(child, labelProvider);
                if (childItem != null) {
                    root.getChildren().add(childItem);
                }
            }

            return root;
        }

        // ---- Default behavior ----
        TreeItem<String> item = new TreeItem<>(labelProvider.getLabel(node));

        for (com.github.javaparser.ast.Node child : node.getChildNodes()) {
            TreeItem<String> childItem = buildASTTree(child, labelProvider);
            if (childItem != null) {
                item.getChildren().add(childItem);
            }
        }

        return item;







    }

    /** UI-only: switch scene stylesheet to light or dark. No logic impact. */
    private void applyTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String css = "/styles/styles.css";
        scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    public static void main(String[] args) {

        launch(args);
    }
    private void startBackgroundProjectLoad(File projectFolder, ProgressBar progressBar) {

        Task<ProjectContext> loadTask = new Task<>() {
            @Override
            protected ProjectContext call() {
                return new ProjectContext(
                        projectFolder,
                        detector,
                        (current,total)->{
                            updateProgress(current, total);
                            switch (current) {
                                case 1 -> updateMessage("Detecting source roots...");
                                case 2 -> updateMessage("Initializing parser...");
                                case 3 -> updateMessage("Setting up graph...");
                                case 4 -> updateMessage("Extracting classes...");
                                case 5 -> updateMessage("Running analysis...");
                                case 6 -> updateMessage("Building view models...");
                            }
                        }

                );
            }
        };

        // Unbind old bindings
        progressBar.progressProperty().unbind();
        progressLabel.textProperty().unbind();

       // Bind new task
        progressBar.progressProperty().bind(loadTask.progressProperty());
        progressLabel.textProperty().bind(loadTask.messageProperty());
        progressBar.setVisible(true);
        progressLabel.setVisible(true);

        loadTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            progressLabel.setVisible(false);

            ProjectContext ctx = loadTask.getValue();


            System.out.println("Project loaded successfully");
            initAfterLoad(projectFolder,ctx);
        });

        loadTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            progressLabel.setVisible(false);
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask, "Project-Loader-Thread").start();
    }


    private void initAfterLoad(File projectFolder,ProjectContext ctx) {
        this.srcRoot=ctx.sourceRoots;
        this.srcClasses=ctx.sourceClasses;
        this.cache = ctx.cache;
        this.javaFileParser = ctx.javaFileParser;
        this.parsermanager=ctx.parsermanager;
        this.classGraphBuilder =ctx.classGraphBuilder;
        this.analysisEngine=ctx.analysisEngine;


        this.updateManager=ctx.updateManager;
        this.projectInfo=ctx.projectInfo;
        this.vmMap=ctx.vmMap;
        Platform.runLater(()-> {analyzeBtn.setVisible(true);
        optimizeBtn.setVisible(true);});

        EventBus eventBus=new EventBus();
        new IncrementalAnalyzer(eventBus,updateManager);

        ProjectFileListener listener=new ProjectFileListener(eventBus);
        watcher=new ProjectFileWatcher();
        try {
            watcher.start(projectFolder,listener);
        }catch (Exception e){
            e.printStackTrace();
        }
        UiRerfeshController uiRerfeshController=new UiRerfeshController(FileTreeView,fileTreeNodeFactory,codeTabPane,rightPanelTabManager,projectAnalysisUi,analysisEngine);
        eventBus.subscribe(UiRefreshEvent.class, uiRerfeshController::onUiRefresh);


    }



    @Override
    public void stop() throws Exception {
        System.out.println(" Shutting down application...");
        if (watcher != null) {
            watcher.stop();
            System.out.println("File watcher stopped");
        }
        super.stop();
    }




}
