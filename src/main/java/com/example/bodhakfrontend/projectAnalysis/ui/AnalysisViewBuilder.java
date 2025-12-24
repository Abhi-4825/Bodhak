package com.example.bodhakfrontend.projectAnalysis.ui;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.projectAnalysis.ProjectHealthAnalyzer;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.sun.source.tree.Tree;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalysisViewBuilder {
    private classHealthAnalyserViewBuilder classHealthAnalyserViewBuilder = new classHealthAnalyserViewBuilder();
    private final UiFeatures uiFeatures;
    private final VBox root = new VBox();

    public AnalysisViewBuilder(UiFeatures uiFeatures) {
        this.uiFeatures = uiFeatures;
        root.setPadding(new Insets(10));
    }

    private void addSection(String title, Node content) {
        TitledPane titledPane = new TitledPane(title, content);
        titledPane.setExpanded(true);
        root.getChildren().add(titledPane);
    }

    private void addCollapsedSection(String title, Node content) {
        TitledPane titledPane = new TitledPane(title, content);
        titledPane.setExpanded(false);
        root.getChildren().add(titledPane);
    }

    public Node build(ProjectAnalysisResult projectAnalysisResult) {

        analyzeView(projectAnalysisResult.getPackageAnalysisInfo(),
                projectAnalysisResult.getPackageInfoMap(),
                projectAnalysisResult.getClassInfos(),
                projectAnalysisResult.getProjectHealthSummary(),
                projectAnalysisResult.getClassHealthInfoMap(),uiFeatures,
                projectAnalysisResult.getHotspotInfos(),projectAnalysisResult.getUnusedClassInfos()
                );

      return new ScrollPane(root);
    }

    private void analyzeView(PackageAnalysisInfo packageAnalysisInfo, Map<String, PackageInfo> packageInfoMap, List<ClassInfo> classInfos, ProjectHealthSummary projectHealthSummary, Map<String, ClassHealthInfo> classHealthInfoMap, UiFeatures uiFeatures, List<HotspotInfo> hotspots, List<UnusedClassInfo> unusedClasses) {

        addSection("📦 Project Summary", buildProjectSummary(packageAnalysisInfo));
        addSection("\uD83D\uDE80 Entry Points", buildEntryPointSection(packageAnalysisInfo));
        addCollapsedSection("📦 Package Overview", buildPackageOverView(packageInfoMap));
        addCollapsedSection("\uD83E\uDDED Package Dependency Tree", buildPackageDependencyTreeView(packageInfoMap));
        addCollapsedSection("📊 Largest Files (by LOC)", buildLargestFileView(packageAnalysisInfo));
        addCollapsedSection("🏷 Class Metrics", buildClassMetricsView(classInfos));
        addCollapsedSection("🏥 Project Health", buildHealthSummary(projectHealthSummary));
        addCollapsedSection(
                "🏥 Class Health Details",
                classHealthAnalyserViewBuilder.buildClassHealthTree(classHealthInfoMap, uiFeatures)
        );
        addCollapsedSection("🔥 Risk Hotspots", buildHotspotView(hotspots, uiFeatures));
        addCollapsedSection(
                "🧹 Unused Classes",
                buildUnusedClassView(unusedClasses, uiFeatures)
        );


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
        Map<String, Integer> map = result.getLaguagesCount();
        if (!map.isEmpty()) {
            for (String key : map.keySet()) {
                grid.addRow(row++, new Label(key + ":"), new Label(map.get(key).toString()));
            }
        }

        return grid;
    }

    private Node buildEntryPointSection(PackageAnalysisInfo result) {
        VBox box = new VBox();
        box.setPadding(new Insets(5));
        Label typeLabel = new Label("Project Type:" + result.getEntryPointInfo().getProjectType().toString());
        typeLabel.setStyle("-fx-font-weight: bold");
        box.getChildren().add(typeLabel);
        // for Primary Entry Point
        if (result.getEntryPointInfo().getPrimaryEntry() != null) {
            Label primaryTitle = new Label("Primary Entry point:");
            primaryTitle.setStyle("-fx-font-weight: bold");
            Label primary = new Label("• " + result.getEntryPointInfo().getPrimaryEntry().toString());
            box.getChildren().addAll(primaryTitle, primary);
        }
        // for secondary points
        if (!result.getEntryPointInfo().getSecondaryEntries().isEmpty()) {
            Label secondaryTitle = new Label("Other possible Entry Point:");
            secondaryTitle.setStyle("-fx-font-weight: bold");
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
            packageHBox.setStyle(
                    "-fx-border-color: #ddd;" +
                            "-fx-border-radius: 4;" +
                            "-fx-padding: 6;"
            );
            Label packageLabel = new Label(key);
            packageLabel.setStyle("-fx-font-weight: bold");
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
        for (String file : result.getLargetFiles().keySet()) {
            HBox fileHBox = new HBox();
            fileHBox.setStyle("-fx-border-color: #ddd;" +
                    "-fx-border-radius: 4;" +
                    "-fx-padding: 6;");
            Label fileLabel = new Label(rank++ + ". " + file);
            fileLabel.setStyle("-fx-font-weight: bold");
            Label loc = new Label("• Lines of Code: " + result.getLargetFiles().get(file));
            fileHBox.getChildren().addAll(fileLabel, loc);
            box.getChildren().addAll(fileHBox);
        }
        return box;
    }

    private Node buildClassMetricsView(List<ClassInfo> classes) {
        VBox container = new VBox();
        container.setPadding(new Insets(5));
        if (classes == null || classes.isEmpty()) {
            container.getChildren().add(new Label("No Class Found!"));
            return container;
        }
        for (ClassInfo cls : classes) {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(6);
            grid.setPadding(new Insets(6));
            grid.setStyle(
                    "-fx-border-color: #ddd;" +
                            "-fx-border-radius: 4;" +
                            "-fx-background-radius: 4;"
            );
            int r = 0;
            Label title = new Label(cls.getName());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 13");
            grid.add(title, 0, r++, 2, 1);
            grid.addRow(r++, new Label("Type:"), new Label(cls.getKind().toString()));
            grid.addRow(r++, new Label("Package:"), new Label(cls.getPkg()));
            grid.addRow(r++,
                    new Label("Members:"),
                    new Label(
                            "Methods: " + cls.getMethodCount()
                                    + " | Fields: " + cls.getFieldCount()
                                    + " | Constructors: " + cls.getConstructorCount()
                    ));

            grid.addRow(r++, new Label("Size:"),
                    new Label(String.valueOf(cls.getLinesOfCode())));
            String modifier = (cls.isPublic() ? "public" : "")
                    + (cls.isAbstract() ? "abstract" : "") +
                    (cls.isFinal() ? "final" : "");
            grid.addRow(r++, new Label(modifier),
                    new Label(modifier.isBlank() ? "-" : modifier.trim()));
            container.getChildren().add(grid);


        }
        return container;


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
                    setStyle(
                            "-fx-text-fill: #d32f2f;" +
                                    "-fx-font-weight: bold;"
                    );
                    TreeItem<String> pkgNode =
                            getTreeItem().getParent().getParent();

                    PackageInfo pkg =
                            packageInfos.get(extractPackageName(pkgNode.getValue()));

                    if (pkg != null) {
                        Tooltip tooltip =
                                new Tooltip(buildCycleTooltipText(pkg));
                        tooltip.setStyle("-fx-font-size: 12");
                        setTooltip(tooltip);
                    }
                } else {
                    setTooltip(null);
                }
            }
        });

        treeView.setShowRoot(false);
        treeView.setPrefHeight(400);

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
            card.setStyle("""
                        -fx-border-color: #e57373;
                        -fx-border-radius: 6;
                        -fx-padding: 8;
                        -fx-background-color: #fff5f5;
                    """);

            Label title = new Label(
                    rank++ + ". " + hs.getClassName()
                            + " (Risk Score: " + hs.getScore() + ")"
            );
            title.setStyle("-fx-font-weight: bold");

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
                        uiFeatures.openAndHighlight(dependencyNode);
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
            box.getChildren().add(
                    new Label("✅ No unused classes detected")
            );
            return box;
        }

        for (UnusedClassInfo uc : unused) {

            HBox row = new HBox(10);
            row.setStyle("""
                        -fx-border-color: #ddd;
                        -fx-border-radius: 4;
                        -fx-padding: 6;
                    """);

            Label name = new Label("⚠ " + uc.getClassName());
            name.setStyle("-fx-font-weight: bold");

            Label meta = new Label(
                    "Package: "
                            + " | LOC: " + uc.getLoc()
            );

            row.getChildren().addAll(name, meta);

            // 🔗 navigation
            row.setOnMouseClicked(e ->
                    uiFeatures.openAndHighlight(uc.getDependencyNode())
            );

            box.getChildren().add(row);
        }

        return box;
    }

}
