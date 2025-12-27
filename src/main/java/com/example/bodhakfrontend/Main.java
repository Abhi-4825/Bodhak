package com.example.bodhakfrontend;
import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Parser.AstLabelProvider;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.dependency.Dependencies;
import com.example.bodhakfrontend.projectAnalysis.*;
import com.example.bodhakfrontend.projectAnalysis.ui.AnalysisViewBuilder;
import com.example.bodhakfrontend.ui.OverviewContentFactory;
import com.example.bodhakfrontend.ui.overviewButton.*;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.example.bodhakfrontend.util.BuildClassIndex;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main extends Application {

    LanguageDetector detector = new LanguageDetector();
    JavaFileParser javaFileParser = new JavaFileParser();
    Parsermanager  parsermanager = new Parsermanager();
    private ProjectAnalysisResultBuilder projectAnalysisResultBuilder=new ProjectAnalysisResultBuilder();
    MultiModuleSourceRootDetector rootDetector = new MultiModuleSourceRootDetector();private TextArea outputPanel;
    private TreeView<File> FileTreeView;
    private File projectFolder;
    private List<Path> srcRoot;
    private Set<String> srcClasses;
    private TabPane codeTabPane;
    private TabPane rightTabPane;
    private UiFeatures  uiFeatures;
    private RightPanelTabManager rightPanelTabManager;
    private TreeView<DependencyNode> dependencyTreeView;
    private Map<String, DependencyNode> classIndex=new HashMap<>();
    BuildClassIndex buildClassIndex=new BuildClassIndex(classIndex);
    private BorderPane rightPanel;
    private TreeItem<File> selected;
    private ClassDependencyInfo classDependencyInfo;
    private ProjectAnalysisResult projectAnalysisResult;
    private AnalysisViewBuilder analysisViewBuilder;
    private Dependencies dependencies=new Dependencies(rootDetector,javaFileParser);
    @Override
    public void start(Stage stage) throws Exception {

        StaticJavaParser.getConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        BorderPane root=new BorderPane();
        Scene scene=new Scene(root,1200,800);
//        scene.getStylesheets().add(getClass().getResource("/styles/bodhak.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Bodhak");
        stage.show();


        // for upper buttons
        ToolBar toolBar=new ToolBar();
        Button SelectBtn=new Button("Select Folder");
        toolBar.getItems().addAll(SelectBtn);
        root.setTop(toolBar);

        // for bottom
        HBox hBox=new HBox();
        Button AnalyzeBtn=new Button("Analyze");
        //button for Ast
        Button Ast=new Button("Show AST");
        Button overviewBtn=new Button("overview");
        hBox.getChildren().addAll(AnalyzeBtn,Ast,overviewBtn);
        hBox.setPadding(new Insets(10));
        hBox.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(hBox);
        // for code view
        codeTabPane=new TabPane();
        uiFeatures=new UiFeatures(codeTabPane);
        codeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        // for output
        rightTabPane=new TabPane();
        rightPanelTabManager=new RightPanelTabManager(rightTabPane);
        outputPanel=new TextArea();
        outputPanel.setPrefWidth(300);
        outputPanel.setEditable(false);
        dependencyTreeView=new TreeView<>();
        dependencyTreeView.setShowRoot(false);
        dependencyTreeView.setPrefWidth(250);
        // default rightpanel
        rightPanel=new BorderPane();
        rightPanel.setCenter(outputPanel);
        // for files
        FileTreeView =new TreeView<>();
        // we will make Split Pane for resizable part of centre
        SplitPane splitPane=new SplitPane();
        splitPane.getItems().addAll(FileTreeView,codeTabPane,rightTabPane);
        splitPane.setDividerPositions(0.18,0.65);
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
            if(projectFolder!=null){
                TreeItem<File> rootNode=createNode(projectFolder);
                FileTreeView.setRoot(rootNode);
                FileTreeView.setShowRoot(true);
                srcRoot=rootDetector.detectSourceRoots(projectFolder.toPath());
                srcClasses=javaFileParser.getClassesfromSource(srcRoot);
                buildClassIndex.buildClassIndex(projectFolder);
                classDependencyInfo =dependencies.getProjectDependecyInfo(projectFolder,classIndex);
                Set<Set<String>> c= classDependencyInfo.getCircularDependencyGroups();
                projectAnalysisResult =projectAnalysisResultBuilder.build(projectFolder.toPath(), classDependencyInfo);
                Map<String,List<MethodsInfo>> methods=classDependencyInfo.getMethods();


            }
        });
   // for dependencies tree view working
        dependencyTreeView.setOnMouseClicked(e -> {

            if(e.getClickCount()==2 ){
                TreeItem<DependencyNode> treeItem=dependencyTreeView.getSelectionModel().getSelectedItem();
                if(treeItem==null){return;}
                DependencyNode selectedNode=(DependencyNode)treeItem.getValue();
                uiFeatures.openAndHighlight(selectedNode.getClassName(),selectedNode.getBeginLine(),selectedNode.getSourceFile());
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
                outputPanel.setText("File Type: " + type + "\nPath: " + file.getAbsolutePath());
            }
        });



        // Analyze Button
        AnalyzeBtn.setOnAction(e -> {

            if (projectFolder == null || classDependencyInfo == null ) return;
            analysisViewBuilder =new AnalysisViewBuilder(uiFeatures);
            Node analyzeView= analysisViewBuilder.build(projectAnalysisResult);
            rightPanelTabManager.openAnalyzeTab(analyzeView);
        });
        // Ast button functioning
        Ast.setOnAction(e -> showASTWindow());
        // overview button
        overviewBtn.setOnAction(e ->{
            MethodsViewBuilder methodsViewBuilder=new MethodsViewBuilder(uiFeatures,classDependencyInfo);
            ClassDependencyView classDependencyView=new ClassDependencyView(uiFeatures,classDependencyInfo);
            HealthAnalyserViewBuilder healthAnalyserViewBuilder=new HealthAnalyserViewBuilder();

            Tab selectedTab = codeTabPane.getSelectionModel().getSelectedItem();
            if(selectedTab==null){return;}
            File file=(File)selectedTab.getUserData();
            List<String> classes=javaFileParser.getFileClasses(file);
            OverviewContentFactory factory=classname -> {
                ClassHealthInfo healthInfo=projectAnalysisResult.getClassHealthInfoMap().get(classname);
                return new OverviewView(
                        classDependencyView.build(classname), methodsViewBuilder.build(classname),healthAnalyserViewBuilder.build(healthInfo)
                ).getRoot();
            };

            ClassOverviewContainer container=new ClassOverviewContainer(file,classes,factory);

            rightPanelTabManager.openOverviewTab(file,container.getRoot());
           });




    }
    private TreeItem<File> createNode(File file) {
        return new TreeItem<>(file) {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    File f = getValue();
                    isLeaf = f.isFile();
                }
                return isLeaf;
            }

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> treeItem) {
                File f = treeItem.getValue();
                if (f == null || f.isFile()) {
                    return FXCollections.emptyObservableList();
                }
                File[] files = f.listFiles();
                if (files != null) {
                    return FXCollections.observableArrayList(
                            Arrays.stream(files)
                                    .map(f2 ->{
                                        TreeItem<File> node= createNode(f2);
                                        node.setGraphic(getIconForFile(f2));
                                        return node;
                                            }
                                            )
                                    .toList()
                    );
                }
                return FXCollections.emptyObservableList();
            }
        };
    }
 private Node getIconForFile(File f){
     Image folderIcon=new Image(getClass().getResourceAsStream("/icons/folder.png"));
     Image fileIcon =new Image(getClass().getResourceAsStream("/icons/file.png"));
     ImageView icon=f.isDirectory()?new ImageView(folderIcon):new ImageView(fileIcon);
     icon.setFitHeight(16);
     icon.setFitWidth(16);
     return icon;
 }

// AST window
private void showASTWindow() {

    TreeItem<File> selected = FileTreeView.getSelectionModel().getSelectedItem();
    if (selected == null) {
        outputPanel.setText("No File Selected");
        return;
    }

    File file = selected.getValue();

    try {
        // Parse AST
        CompilationUnit cu = (CompilationUnit) parsermanager.parseFile(file);

        // Get correct label provider
        @SuppressWarnings("unchecked")
        AstLabelProvider<com.github.javaparser.ast.Node> labelProvider =
                (AstLabelProvider<com.github.javaparser.ast.Node>)
                        parsermanager.getLabelProvider(file);

        // 3️⃣ Build AST tree USING provider
        TreeItem<String> astRoot = buildASTTree(cu, labelProvider);

        // 4️⃣ Show window
        showASTStage(file, astRoot);

    } catch (Exception e) {
        outputPanel.setText("AST Error: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void showASTStage(File file, TreeItem<String> astRoot) {

        TreeView<String> astTree = new TreeView<>(astRoot);
        astTree.setPrefWidth(400);

        TextArea codeArea = new TextArea();
        codeArea.setEditable(false);

        try {
            codeArea.setText(Files.readString(file.toPath()));
        } catch (Exception ignored) {}

        SplitPane split = new SplitPane(astTree, codeArea);

        Stage astStage = new Stage();
        astStage.setScene(new Scene(split, 900, 600));
        astStage.setTitle("AST Viewer - " + file.getName());
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
}


