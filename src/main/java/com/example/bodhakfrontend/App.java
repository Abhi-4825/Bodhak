package com.example.bodhakfrontend;

import com.example.bodhakfrontend.IncrementalPart.Builder.ClassDependecygraphBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ClassInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.PackageInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ProjectInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Update.EventBus;
import com.example.bodhakfrontend.IncrementalPart.Update.IncrementalAnalyzer;
import com.example.bodhakfrontend.IncrementalPart.Update.ProjectFileListener;
import com.example.bodhakfrontend.IncrementalPart.Update.ProjectFileWatcher;
import com.example.bodhakfrontend.IncrementalPart.UpdateManager;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.IncrementalPart.Update.UiRefreshEvent;
import com.example.bodhakfrontend.IncrementalPart.model.incrementalModel.ClassInfoViewModel;
import com.example.bodhakfrontend.Parser.AstLabelProvider;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.ui.Front.FileTreeNodeFactory;
import com.example.bodhakfrontend.ui.OverviewContentFactory;
import com.example.bodhakfrontend.ui.ProjectAnalysis.ProjectAnalysisUi;
import com.example.bodhakfrontend.ui.UiRerfeshController;
import com.example.bodhakfrontend.ui.overviewButton.*;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.util.ParseCache;
import com.github.javaparser.ast.CompilationUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class App extends Application {
    LanguageDetector detector = new LanguageDetector();
    ParseCache cache ;
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
    private ClassDependecygraphBuilder classDependecygraphBuilder;
    private ClassInfoBuilder classInfoBuilder;
    private PackageInfoBuilder packageInfoBuilder;
    private ProjectInfoBuilder projectInfoBuilder;
    private ProjectInfo projectInfo;
    private Button analyzeBtn;
    private ProjectAnalysisUi projectAnalysisUi;
    private UpdateManager updateManager;
    private Map<String, ClassInfoViewModel> vmMap;
    private ProjectFileWatcher watcher;


    private FileTreeNodeFactory fileTreeNodeFactory;

    @Override
    public void start(Stage stage) throws Exception {



        BorderPane root=new BorderPane();
        Scene scene=new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/bodhak.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Bodhak");
        stage.setMaximized(true);
        stage.show();
        globalProgressBar=new ProgressBar();
        globalProgressBar.setPrefWidth(Region.USE_COMPUTED_SIZE);
        globalProgressBar.setVisible(false);


        // for upper buttons
        ToolBar toolBar=new ToolBar();
        Button SelectBtn=new Button("Select Folder");
        toolBar.getItems().addAll(SelectBtn);
        root.setTop(toolBar);

        // for bottom

        analyzeBtn=new Button("Analyze");
        analyzeBtn.setVisible(false);



        BorderPane bottomBar = new BorderPane();
        bottomBar.setPadding(new Insets(8));
        bottomBar.setLeft(globalProgressBar);
        bottomBar.setRight(analyzeBtn);
        root.setBottom(bottomBar);
        // for code view
        codeTabPane=new TabPane();
        uiFeatures=new UiFeatures(codeTabPane);
        projectAnalysisUi=new ProjectAnalysisUi(uiFeatures);
//        analysisViewBuilder=new AnalysisViewBuilder(uiFeatures);
        codeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        // for output
        rightTabPane=new TabPane();
        rightPanelTabManager=new RightPanelTabManager(rightTabPane);
        outputPanel=new TextArea();
        outputPanel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        outputPanel.setEditable(false);
        dependencyTreeView=new TreeView<>();
        dependencyTreeView.setShowRoot(false);
        dependencyTreeView.setPrefWidth(Region.USE_COMPUTED_SIZE);
        // default rightpanel
        rightPanel=new BorderPane();
        rightPanel.setCenter(outputPanel);
        // for files
        FileTreeView =new TreeView<>();
        //EditorPane buttons
        Button Ast=new Button("AST");
        Button overviewBtn=new Button("Overview");
        HBox editorBottom=new HBox(8);
        editorBottom.getChildren().addAll(Ast,overviewBtn);
        editorBottom.setPadding(new Insets(6));
        editorBottom.setAlignment(Pos.CENTER_RIGHT);
        BorderPane codeEditorPane=new BorderPane();
        codeEditorPane.setCenter(codeTabPane);
        codeEditorPane.setBottom(editorBottom);


        // we will make Split Pane for resizable part of centre
        SplitPane splitPane=new SplitPane();
        splitPane.getItems().addAll(FileTreeView,codeEditorPane,rightTabPane);
        splitPane.setDividerPositions(0.18,0.58);
        root.setCenter(splitPane);
        root.getStyleClass().add("app-root");
        FileTreeView.getStyleClass().add("sidebar");
        codeTabPane.getStyleClass().add("editor-area");
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

        SelectBtn.setOnAction(e -> {

            DirectoryChooser directoryChooser=new DirectoryChooser();
            directoryChooser.setTitle("Select Project Folder");
            projectFolder=directoryChooser.showDialog(stage);
            if(projectFolder==null)return;
            codeTabPane.getTabs().clear();
            rightTabPane.getTabs().clear();
            rightPanelTabManager.clear();
            fileTreeNodeFactory=new FileTreeNodeFactory();
            TreeItem<File> rootNode=fileTreeNodeFactory.createNode(projectFolder);
            FileTreeView.setRoot(rootNode);
            FileTreeView.setShowRoot(true);

            startBackgroundProjectLoad(projectFolder,globalProgressBar);



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
            List<ClassInfo> classes=classInfoBuilder.scanFile(file.toPath());
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
            CompilationUnit cu =cache.get(file.toPath());
            // Get correct label provider
            @SuppressWarnings("unchecked")
            AstLabelProvider<com.github.javaparser.ast.Node> labelProvider =
                    (AstLabelProvider<com.github.javaparser.ast.Node>)
                            parsermanager.getLabelProvider(file);

            // 3 Build AST tree USING provider
            TreeItem<String> astRoot = buildASTTree(cu, labelProvider);

            // 4 Show window
            showASTStage(file, astRoot);

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

    public static void main(String[] args) {

        launch(args);
    }
    private void startBackgroundProjectLoad(File projectFolder, ProgressBar progressBar) {

        Task<ProjectContext> loadTask = new Task<>() {
            @Override
            protected ProjectContext call() {
                return new ProjectContext(
                        projectFolder,
                        detector

                );
            }
        };

        progressBar.progressProperty().bind(loadTask.progressProperty());
        progressBar.setVisible(true);

        loadTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);

            ProjectContext ctx = loadTask.getValue();


            System.out.println("Project loaded successfully");
            initAfterLoad(projectFolder,ctx);
        });

        loadTask.setOnFailed(e -> {
            progressBar.setVisible(false);
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
        this.classDependecygraphBuilder=ctx.classDependecygraphBuilder;
        this.classInfoBuilder=ctx.classInfoBuilder;
        this.packageInfoBuilder=ctx.packageInfoBuilder;
        this.projectInfoBuilder=ctx.projectInfoBuilder;

        this.updateManager=ctx.updateManager;
       projectInfoBuilder.buildAll(projectFolder.toPath());
        projectInfo=projectInfoBuilder.getProjectInfo();
        List<ClassInfo> c=projectInfo.getClassInfos();
        this.vmMap=ctx.classInfoViewModelBuilder.initialBuild(projectInfo.getClassInfos());
        Platform.runLater(()-> analyzeBtn.setVisible(true));

        EventBus eventBus=new EventBus();
        new IncrementalAnalyzer(eventBus,updateManager);

        ProjectFileListener listener=new ProjectFileListener(eventBus);
        watcher=new ProjectFileWatcher();
        try {
            watcher.start(projectFolder,listener);
        }catch (Exception e){
            e.printStackTrace();
        }
        UiRerfeshController uiRerfeshController=new UiRerfeshController(FileTreeView,fileTreeNodeFactory,codeTabPane,rightPanelTabManager,projectAnalysisUi,projectInfoBuilder);
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
