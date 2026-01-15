package com.example.bodhakfrontend;

import com.example.bodhakfrontend.IncrementalPart.Builder.*;
import com.example.bodhakfrontend.IncrementalPart.UpdateManager;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.Parser.AstLabelProvider;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.dependency.CircularDependency;
import com.example.bodhakfrontend.incremental.*;
import com.example.bodhakfrontend.projectAnalysis.ui.AnalysisViewBuilder;
import com.example.bodhakfrontend.ui.Front.FileTreeNodeFactory;
import com.example.bodhakfrontend.ui.OverviewContentFactory;
import com.example.bodhakfrontend.ui.overviewButton.*;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.util.ParseCache;
import com.github.javaparser.ast.CompilationUnit;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class App extends Application {

    /* ---------------- Core Infra ---------------- */

    private final LanguageDetector detector = new LanguageDetector();
    private final MultiModuleSourceRootDetector rootDetector =
            new MultiModuleSourceRootDetector();
    private final CircularDependency circularDependency =
            new CircularDependency();
    private final HotspotAnalyzer hotspotAnalyzer =
            new HotspotAnalyzer();
    private final ProjectHealthAnalyzer projectHealthAnalyzer =
            new ProjectHealthAnalyzer();

    /* ---------------- Runtime State ---------------- */

    private File projectFolder;
    private UpdateManager updateManager;
    private ProjectInfo projectInfo;

    /* ---------------- Parsers / Cache ---------------- */

    private ParseCache cache;
    private JavaFileParser javaFileParser;
    private Parsermanager parsermanager;

    /* ---------------- UI ---------------- */

    private TreeView<File> fileTreeView;
    private TabPane codeTabPane;
    private RightPanelTabManager rightPanelTabManager;
    private UiFeatures uiFeatures;
    private AnalysisViewBuilder analysisViewBuilder;
    private ProgressBar globalProgressBar;

    private FileTreeNodeFactory fileTreeNodeFactory;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        scene.getStylesheets()
                .add(getClass().getResource("/styles/bodhak.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Bodhak");
        stage.setMaximized(true);
        stage.show();

        /* ---------------- Top Toolbar ---------------- */

        Button selectBtn = new Button("Select Folder");
        ToolBar toolBar = new ToolBar(selectBtn);
        root.setTop(toolBar);

        /* ---------------- Bottom Bar ---------------- */

        globalProgressBar = new ProgressBar();
        globalProgressBar.setVisible(false);

        Button analyzeBtn = new Button("Analyze");

        BorderPane bottomBar = new BorderPane();
        bottomBar.setPadding(new Insets(8));
        bottomBar.setLeft(globalProgressBar);
        bottomBar.setRight(analyzeBtn);
        root.setBottom(bottomBar);

        /* ---------------- Center Layout ---------------- */

        fileTreeView = new TreeView<>();
        fileTreeView.getStyleClass().add("sidebar");

        codeTabPane = new TabPane();
        codeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        uiFeatures = new UiFeatures(codeTabPane);
        analysisViewBuilder = new AnalysisViewBuilder(uiFeatures);

        TabPane rightTabPane = new TabPane();
        rightPanelTabManager = new RightPanelTabManager(rightTabPane);

        Button astBtn = new Button("AST");
        Button overviewBtn = new Button("Overview");

        BooleanBinding hasTab =
                codeTabPane.getSelectionModel()
                        .selectedItemProperty()
                        .isNotNull();

        astBtn.disableProperty().bind(hasTab.not());
        overviewBtn.disableProperty().bind(hasTab.not());

        HBox editorBottom = new HBox(8, astBtn, overviewBtn);
        editorBottom.setPadding(new Insets(6));
        editorBottom.setAlignment(Pos.CENTER_RIGHT);

        BorderPane editorPane = new BorderPane();
        editorPane.setCenter(codeTabPane);
        editorPane.setBottom(editorBottom);

        SplitPane splitPane = new SplitPane(
                fileTreeView,
                editorPane,
                rightTabPane
        );
        splitPane.setDividerPositions(0.18, 0.6);

        root.setCenter(splitPane);

        /* ---------------- File Tree ---------------- */

        fileTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                } else {
                    setText(file.getName().isEmpty()
                            ? file.getAbsolutePath()
                            : file.getName());
                }
            }
        });

        fileTreeView.setOnMouseClicked(e -> {
            TreeItem<File> selected =
                    fileTreeView.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            File f = selected.getValue();
            if (f.isFile() && e.getClickCount() == 2) {
                uiFeatures.openFile(f);
            }
        });

        /* ---------------- Actions ---------------- */

        selectBtn.setOnAction(e -> chooseProject(stage));
        analyzeBtn.setOnAction(e -> openAnalysis());
        astBtn.setOnAction(e -> showASTWindow());
        overviewBtn.setOnAction(e -> openOverview());
    }

    /* ========================================================= */

    private void chooseProject(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Project Folder");

        projectFolder = chooser.showDialog(stage);
        if (projectFolder == null) return;

        fileTreeNodeFactory = new FileTreeNodeFactory();
        fileTreeView.setRoot(
                fileTreeNodeFactory.createNode(projectFolder)
        );

        startBackgroundLoad(projectFolder);
    }

    private void startBackgroundLoad(File folder) {

        Task<ProjectInfo> task = new Task<>() {
            @Override
            protected ProjectInfo call() {

                cache = new ParseCache(projectFolder.toPath());
                javaFileParser = new JavaFileParser(cache);
                parsermanager = new Parsermanager(new LanguageDetector(),javaFileParser);

                ClassDependecygraphBuilder depBuilder =
                        new ClassDependecygraphBuilder(cache, new ClassNameResolver());

                ClassInfoBuilder classInfoBuilder =
                        new ClassInfoBuilder(cache, depBuilder);

                PackageInfoBuilder packageInfoBuilder =
                        new PackageInfoBuilder(classInfoBuilder);

                ProjectInfoBuilder projectInfoBuilder =
                        new ProjectInfoBuilder(classInfoBuilder, packageInfoBuilder);

                updateManager =
                        new UpdateManager(
                                classInfoBuilder,
                                packageInfoBuilder,
                                projectInfoBuilder,
                                depBuilder,
                                folder.toPath()
                        );

                updateManager.onLoad();
                return updateManager.getProjectInfo();
            }
        };

        globalProgressBar.progressProperty().bind(task.progressProperty());
        globalProgressBar.setVisible(true);

        task.setOnSucceeded(e -> {
            globalProgressBar.setVisible(false);
            projectInfo = task.getValue();
        });

        new Thread(task, "Project-Loader").start();
    }

    /* ========================================================= */

    private void openAnalysis() {
        if (projectInfo == null) return;
        rightPanelTabManager.openAnalyzeTab(
                () -> analysisViewBuilder.build(projectInfo)
        );
    }

    private void openOverview() {

        Tab tab = codeTabPane.getSelectionModel().getSelectedItem();
        if (tab == null || projectInfo == null) return;

        File file = (File) tab.getUserData();
        List<String> classes = javaFileParser.getFileClasses(file);

        MethodsViewBuilder methodsView =
                new MethodsViewBuilder(uiFeatures, projectInfo);

        ClassDependencyView dependencyView =
                new ClassDependencyView(uiFeatures, projectInfo);

        HealthAnalyserViewBuilder healthView =
                new HealthAnalyserViewBuilder();

        OverviewContentFactory factory = className ->
                new OverviewView(
                        dependencyView.build(className),
                        methodsView.build(className),
                        healthView.build(
                                projectInfo.getClassInfoMap().get(className)
                        )
                ).getRoot();

        ClassOverviewContainer container =
                new ClassOverviewContainer(file, classes, factory);

        rightPanelTabManager.openOverviewTab(file, container.getRoot());
    }

    /* ========================================================= */

    private void showASTWindow() {

        Tab tab = codeTabPane.getSelectionModel().getSelectedItem();
        if (tab == null) return;

        File file = (File) tab.getUserData();

        try {
            CompilationUnit cu = cache.get(file.toPath());

            AstLabelProvider<com.github.javaparser.ast.Node> provider =
                    parsermanager.parseFile(file);

            TreeItem<String> root = buildASTTree(cu, provider);
            TreeView<String> tree = new TreeView<>(root);

            TextArea code = new TextArea(Files.readString(file.toPath()));
            code.setEditable(false);

            SplitPane split = new SplitPane(tree, code);
            split.setDividerPositions(0.4);

            Stage stage = new Stage();
            stage.setScene(new Scene(split, 900, 600));
            stage.setTitle("AST Viewer - " + file.getName());
            stage.show();

        } catch (Exception ignored) {}
    }

    private TreeItem<String> buildASTTree(
            com.github.javaparser.ast.Node node,
            AstLabelProvider<com.github.javaparser.ast.Node> provider
    ) {
        TreeItem<String> item = new TreeItem<>(provider.getLabel(node));
        for (var child : node.getChildNodes()) {
            TreeItem<String> c = buildASTTree(child, provider);
            if (c != null) item.getChildren().add(c);
        }
        return item;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
