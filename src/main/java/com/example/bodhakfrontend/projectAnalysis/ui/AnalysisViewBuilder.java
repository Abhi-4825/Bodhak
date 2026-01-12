package com.example.bodhakfrontend.projectAnalysis.ui;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Models.PackageAnalysis.PackageAnalysisInfo;
import com.example.bodhakfrontend.Models.PackageAnalysis.PackageInfo;
import com.example.bodhakfrontend.Models.PackageAnalysis.ProjectAnalysisResult;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.example.bodhakfrontend.util.Exporter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.nio.file.Path;
import java.util.*;

public class AnalysisViewBuilder {
    Exporter exporter=new Exporter();
    private final classHealthAnalyserViewBuilder classHealthAnalyserViewBuilder=new classHealthAnalyserViewBuilder();
    private final UiFeatures uiFeatures;


    public AnalysisViewBuilder( UiFeatures uiFeatures) {

        this.uiFeatures = uiFeatures;

    }

    public Node build(ProjectAnalysisResult projectAnalysisResult) {
       VBox root=new VBox();
        root.setPadding(new Insets(10));
        root.getStyleClass().add("card");
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        HBox exportbar=new HBox();
        exportbar.setAlignment(Pos.CENTER_RIGHT);
        Button exportBtn = new Button("Export");
        exportbar.getChildren().add(exportBtn);
        exportBtn.setOnAction(e -> {
                 exporter.exportAnalysis(projectAnalysisResult);
        });
        container.getChildren().add(exportbar);
        analyzeView(root,projectAnalysisResult.getPackageAnalysisInfo(),
                projectAnalysisResult.getPackageInfoMap(),
                projectAnalysisResult.getClassInfos(),
                projectAnalysisResult.getProjectHealthSummary(),
                projectAnalysisResult.getClassHealthInfoMap(),uiFeatures,
                projectAnalysisResult.getHotspotInfos(),projectAnalysisResult.getUnusedClassInfos()
                );
        container.getChildren().add(root);
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        return scrollPane;
    }

    private void analyzeView(VBox root, PackageAnalysisInfo packageAnalysisInfo, Map<String, PackageInfo> packageInfoMap, List<ClassInfo> classInfos, ProjectHealthSummary projectHealthSummary, Map<String, ClassHealthInfo> classHealthInfoMap, UiFeatures uiFeatures, List<HotspotInfo> hotspots, List<UnusedClassInfo> unusedClasses) {
        createSection(root,"/icons/summary.png","Project Summary",buildProjectSummary(packageAnalysisInfo),"icon-blue",true);
        createSection(root,"/icons/entryPoint.png","Entry Points",buildEntryPointSection(packageAnalysisInfo),"icon-blue",true);
        createSection(root,"/icons/packageOverview.png","Package Overview",buildPackageOverView(packageInfoMap),"icon-blue",false);
        createSection(root,"/icons/dependency.png","Package Dependencies",buildPackageDependencyTreeView(packageInfoMap),"icon-blue",false);
        createSection(root,"/icons/largestFiles.png","Largest Files",buildLargestFileView(packageAnalysisInfo),"icon-blue",false);
        createSection(root,"/icons/classMetric.png","Class Metrics |" + classInfos.size() + " classes",buildClassMetricsView(classInfos),"icon-blue",false);
        createSection(root,"/icons/health.png","Project Health",buildHealthSummary(projectHealthSummary),"icon-blue",false);
        createSection(root,"/icons/health.png","Class Health Details",  classHealthAnalyserViewBuilder.buildClassHealthTree(classHealthInfoMap, uiFeatures),"icon-blue",false);

        createSection(root,"/icons/hotspot.png","Risk Hotspots", buildHotspotView(hotspots, uiFeatures),"icon-blue",false);
        createSection(root,"/icons/unused.png","🧹 Unused or Suspicious Classes",
                buildUnusedClassView(unusedClasses, uiFeatures),"icon-blue",false);



    }


    private GridPane buildProjectSummary(
            PackageAnalysisInfo result
    ) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int row = 0;
        grid.addRow(row++, new Label("Project Type:"), new Label(result.getEntryPointInfo().getProjectType().toString()));
        grid.addRow(row++, new Label("Folders:"), new Label(String.valueOf(result.getFolderCount())));
        grid.addRow(row++, new Label("Files:"), new Label(String.valueOf(result.getFileCount())));
        Map<String, Set<Path>> map = result.getLaguagesCount();
        if (!map.isEmpty()) {
            for (String key : map.keySet()) {
                grid.addRow(row++, new Label(key + ":"), new Label(String.valueOf(map.get(key).size())));
            }
        }

        return grid;
    }

    private Node buildEntryPointSection(PackageAnalysisInfo result) {
        VBox box = new VBox();
        box.getStyleClass().add("card");
        box.setPadding(new Insets(5));
        Label typeLabel = new Label("Project Type:" + result.getEntryPointInfo().getProjectType().toString());
        typeLabel.getStyleClass().add("label-bold");
        box.getChildren().add(typeLabel);
        // for Primary Entry Point
        if (result.getEntryPointInfo().getPrimaryEntry() != null) {
            Label primaryTitle = new Label("Primary Entry point:");
            primaryTitle.getStyleClass().add("label-bold");
            Label primary = new Label("• " + result.getEntryPointInfo().getPrimaryEntry().toString());
            box.getChildren().addAll(primaryTitle, primary);
        }
        // for secondary points
        if (!result.getEntryPointInfo().getSecondaryEntries().isEmpty()) {
            Label secondaryTitle = new Label("Other possible Entry Point:");
            secondaryTitle.getStyleClass().add("label-bold");
            box.getChildren().add(secondaryTitle);
            for (String ep : result.getEntryPointInfo().getSecondaryEntries()) {
                box.getChildren().add(new Label("• " + ep));
            }
        }

        return box;
    }


    private Node buildPackageOverView(Map<String, PackageInfo> result) {
        VBox box = new VBox();
        box.setPadding(new Insets(5));
        if (result.isEmpty()) {
            box.getChildren().add(new Label("No package found!"));
            return box;
        }
        for (String key : result.keySet()) {
            HBox packageHBox = new HBox(2);
            packageHBox.getStyleClass().add("package-row");
            Label packageLabel = new Label(key);
            packageLabel.getStyleClass().add("label-bold");
            int size = result.get(key).getClasses().size();
            Label classCount = new Label("• Classes:" + String.valueOf(size));
            packageHBox.getChildren().addAll(packageLabel, classCount);
            box.getChildren().addAll(packageHBox);
        }
        return box;
    }

    private Node buildLargestFileView(PackageAnalysisInfo result) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));
        if (result == null) {
            box.getChildren().add(new Label("No Such File Found!"));
            return box;
        }
        int rank = 1;
        for (PackageAnalysisInfo.LargestFileInfo file : result.getLargetFiles()) {
            HBox fileHBox = new HBox();
            fileHBox.getStyleClass().add("file-row");
            Label fileLabel = new Label(rank++ + ". " + file.getName());
            fileLabel.getStyleClass().add("label-bold");
            fileHBox.setOnMouseClicked(event -> {
                uiFeatures.openFile(file.getSourceFile());

            });
            Label loc = new Label("• Lines of Code: " + file.getLoc());
            fileHBox.getChildren().addAll(fileLabel, loc);
            box.getChildren().addAll(fileHBox);
        }
        return box;
    }

    private GridPane buildHealthSummary(ProjectHealthSummary summary) {

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        int row = 0;
        grid.addRow(row++, new Label("Total Classes:"), new Label(String.valueOf(summary.getTotalClasses())));
        grid.addRow(row++, new Label("✔ Healthy Classes:"), new Label(String.valueOf(summary.getHealthyClasses())));
        grid.addRow(row++, new Label("⚠ Classes with Warnings:"), new Label(String.valueOf(summary.getClassesWithWarnings())));
        grid.addRow(row++, new Label("🔥 God Classes:"), new Label(String.valueOf(summary.getGodClasses())));
        grid.addRow(row++, new Label("🔁 Classes in Cycles:"), new Label(String.valueOf(summary.getCircularClasses())));
        grid.addRow(row++, new Label("🔗 Highly Coupled:"), new Label(String.valueOf(summary.getHighlyCoupledClasses())));

        return grid;
    }

    private TreeItem<String> buildPackageTree(Map<String, PackageInfo> packageInfos) {
        TreeItem<String> root = new TreeItem<>("📦 Packages");
        root.setExpanded(true);
        for (PackageInfo packageInfo : packageInfos.values()) {
            root.getChildren().add(buildPackageNode(packageInfo));
        }
        return root;
    }

    private TreeItem<String> buildPackageNode(PackageInfo packageInfo) {
        String label = "📦 " + packageInfo.getPackageName()
                + " (" + packageInfo.getClasses().size() + " classes"
                + " | " + packageInfo.getDependsOn().size() + "→"
                + " | " + packageInfo.getUsedBy().size() + "←)";
        TreeItem<String> pkgNode = new TreeItem<>(label);
        pkgNode.setExpanded(false);
        pkgNode.getChildren().add(buildOverviewNode(packageInfo));
        pkgNode.getChildren().add(buildDependsOnNode(packageInfo));
        pkgNode.getChildren().add(buildUsedByNode(packageInfo));

        return pkgNode;


    }

    private TreeItem<String> buildOverviewNode(PackageInfo packageInfo) {
        TreeItem<String> overview = new TreeItem<>("Overview");
        overview.setExpanded(true);
        overview.getChildren().add(new TreeItem<>("Classes:" + packageInfo.getClasses().size()));
        overview.getChildren().add(new TreeItem<>("Depends On:" + packageInfo.getDependsOn().size()));
        overview.getChildren().add(new TreeItem<>("Used by:" + packageInfo.getUsedBy().size()));
        if (!packageInfo.getCircularDependencies().isEmpty()) {
            overview.getChildren().add(new TreeItem<>("🔁 Part of Circular Dependency"));


        }
        for (PackageWarning warning : packageInfo.getWarnings()) {
            overview.getChildren().add(new TreeItem<>("⚠ " + warning.name()));
        }
        return overview;
    }

    private TreeItem<String> buildDependsOnNode(PackageInfo pkg) {

        TreeItem<String> dependsOn =
                new TreeItem<>("Depends On");

        for (String dep : pkg.getDependsOn()) {
            dependsOn.getChildren().add(
                    new TreeItem<>("📦 " + dep)
            );
        }

        if (dependsOn.getChildren().isEmpty()) {
            dependsOn.getChildren().add(
                    new TreeItem<>("— None")
            );
        }

        return dependsOn;
    }

    private TreeItem<String> buildUsedByNode(PackageInfo pkg) {

        TreeItem<String> usedBy =
                new TreeItem<>("Used By");

        for (String user : pkg.getUsedBy()) {
            usedBy.getChildren().add(
                    new TreeItem<>("📦 " + user)
            );
        }

        if (usedBy.getChildren().isEmpty()) {
            usedBy.getChildren().add(
                    new TreeItem<>("— None")
            );
        }

        return usedBy;
    }

    private Node buildPackageDependencyTreeView(
            Map<String, PackageInfo> packageInfos
    ) {
        TreeItem<String> rootItem = buildPackageTree(packageInfos);

        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }

                setText(item);

                if (item.contains("🔁 Part of Circular Dependency")) {
                    getStyleClass().add("health-tree-risky");
                    TreeItem<String> pkgNode =
                            getTreeItem().getParent().getParent();

                    PackageInfo pkg =
                            packageInfos.get(extractPackageName(pkgNode.getValue()));

                    if (pkg != null) {
                        Tooltip tooltip =
                                new Tooltip(buildCycleTooltipText(pkg));
                        tooltip.getStyleClass().add("label-muted");
                        setTooltip(tooltip);
                    }
                } else {
                    setTooltip(null);
                }
            }
        });

        treeView.setShowRoot(false);
        treeView.setPrefHeight(Region.USE_COMPUTED_SIZE);

        return treeView;
    }

    private String extractPackageName(String label) {
        if (label == null) return null;

        // Remove 📦 icon if present
        label = label.replace("📦", "").trim();

        // Cut everything after first "("
        int idx = label.indexOf("(");
        if (idx != -1) {
            label = label.substring(0, idx);
        }

        return label.trim();
    }


    private String buildCycleTooltipText(PackageInfo pkg) {
        StringBuilder sb = new StringBuilder("🔁 Circular Dependency Detected\n\n");

        for (Set<String> cycle : pkg.getCircularDependencies()) {
            Iterator<String> it = cycle.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) sb.append("\n→ ");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }


    // Risk hotspot zone
    private Node buildHotspotView(List<HotspotInfo> hotspots, UiFeatures uiFeatures) {

        VBox box = new VBox(8);
        box.setPadding(new Insets(5));

        if (hotspots.isEmpty()) {
            box.getChildren().add(new Label("✅ No high-risk hotspots detected"));
            return box;
        }

        int rank = 1;
        for (HotspotInfo hs : hotspots) {

            VBox card = new VBox(4);

            DependencyNode dependencyNode = hs.getDependencyNode();
            card.getStyleClass().add("hotspot-card");
            card.setPadding(new Insets(8));

            Label title = new Label(
                    rank++ + ". " + hs.getClassName()
                            + " (Risk Score: " + hs.getScore() + ")"
            );
            title.getStyleClass().add("label-bold");

            Label meta = new Label(
                    "LOC: " + hs.getLoc()
                            + " | Fan-in: " + hs.getFanIn()
                            + " | Fan-out: " + hs.getFanOut()
            );

            Label reasons = new Label("⚠ " + String.join(", ", hs.getReasons()));
            reasons.setWrapText(true);

            card.getChildren().addAll(title, meta, reasons);

            // 🔗 click navigation
            card.setOnMouseClicked(e -> {
                        uiFeatures.openAndHighlight(dependencyNode.getClassName(),dependencyNode.getBeginLine(),dependencyNode.getBeginColumn(),dependencyNode.getSourceFile());
                    }
            );

            box.getChildren().add(card);
        }

        return box;
    }

    // for unused Classes
    private Node buildUnusedClassView(
            List<UnusedClassInfo> unused, UiFeatures uiFeatures
    ) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));

        if (unused.isEmpty()) {
            box.getChildren().add(new Label("✅ No Unused Class Found"));
            return box;
        }


        for (UnusedClassInfo unusedClass : unused) {

            VBox card = new VBox(4);
            card.getStyleClass().add("unused-class-card");
            card.setPadding(new Insets(8));

            Label title = new Label(unusedClass.getClassName() +" | "+ unusedClass.getLoc());
            title.getStyleClass().add("label-bold");

            Label meta = new Label("Package Name: " + unusedClass.getPackageName()
            );

            Label reasons = new Label("⚠ " + unusedClass.getReason());
            reasons.setWrapText(true);

            card.getChildren().addAll(title, meta, reasons);
            DependencyNode dependencyNode = unusedClass.getDependencyNode();

            // 🔗 click navigation
            card.setOnMouseClicked(e -> {
                        uiFeatures.openAndHighlight(dependencyNode.getClassName(),dependencyNode.getBeginLine(),dependencyNode.getBeginColumn(),dependencyNode.getSourceFile());
                    }
            );

            box.getChildren().add(card);
        }

        return box;
    }
    private Node buildClassMetricsView(List<ClassInfo> classes) {

        if (classes == null || classes.isEmpty()) {
            return new Label("No Class Found!");
        }

        List<ClassInfo> sorted =
                classes.stream()
                        .sorted(Comparator.comparing(
                                ClassInfo::getName,
                                String.CASE_INSENSITIVE_ORDER
                        ))
                        .toList();

        ListView<ClassInfo> listView = new ListView<>();
        listView.getItems().setAll(sorted);
        listView.setCellFactory(lv -> new ClassMetricsCell());

        return listView;
    }
    private class ClassMetricsCell extends ListCell<ClassInfo> {

        private final GridPane grid = new GridPane();
        private final Label title = new Label();
        private final Label type = new Label();
        private final Label pkg = new Label();
        private final Label members = new Label();
        private final Label size = new Label();
        private final Label modifier = new Label();

        public ClassMetricsCell() {
            grid.setHgap(10);
            grid.setVgap(6);
            grid.setPadding(new Insets(6));
            grid.getStyleClass().add("class-metrics-grid");

            title.getStyleClass().add("label-subtitle");

            int r = 0;
            grid.add(title, 0, r++, 2, 1);
            grid.addRow(r++, new Label("Type:"), type);
            grid.addRow(r++, new Label("Package:"), pkg);
            grid.addRow(r++, new Label("Members:"), members);
            grid.addRow(r++, new Label("Size:"), size);
            grid.addRow(r++, new Label("Modifier:"), modifier);
        }

        @Override
        protected void updateItem(ClassInfo cls, boolean empty) {
            super.updateItem(cls, empty);

            if (empty || cls == null) {
                setGraphic(null);
                return;
            }

            title.setText(cls.getName());
            type.setText(cls.getKind().toString());
            pkg.setText(cls.getPkg());

            members.setText(
                    "Methods: " + cls.getMethodCount()
                            + " | Fields: " + cls.getFieldCount()
                            + " | Constructors: " + cls.getConstructorCount()
            );

            size.setText(String.valueOf(cls.getLinesOfCode()));

            String mod =
                    (cls.isPublic() ? "public " : "") +
                            (cls.isAbstract() ? "abstract " : "") +
                            (cls.isFinal() ? "final" : "");

            modifier.setText(mod.isBlank() ? "-" : mod.trim());

            setGraphic(grid);

            setOnMouseClicked(e -> {
                uiFeatures.openAndHighlight(
                        cls.getName(),
                        cls.getBeginLine(),
                        cls.getBeginColumn(),
                        cls.getSourceFile()
                );
            });
        }
    }


    private Node iconBadge(String iconPath,String bgClass){
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitHeight(16);
        icon.setFitWidth(16);
        icon.setPreserveRatio(true);
        StackPane pane = new StackPane(icon);
        pane.getStyleClass().addAll("icon-badge",bgClass);
        pane.setMinSize(28,28);
        pane.setMaxSize(28,28);
        return pane;
    }

    private void createSection(VBox root,String iconPath,String title,Node content,String bgClass,boolean expanded){
        Node badge=iconBadge(iconPath,bgClass);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        HBox header = new HBox(10,badge,titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        TitledPane titledPane = new TitledPane();
        titledPane.setGraphic(header);
        titledPane.setContent(content);
        titledPane.setExpanded(expanded);
        root.getChildren().add(titledPane);
        root.getChildren().add(new Separator());

    }


}

