package com.example.bodhakfrontend.ui.ProjectAnalysis;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Package.PackageInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.EntryPointInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.Hotspots;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;

import com.example.bodhakfrontend.IncrementalPart.model.Project.UnusedClassInfo;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.example.bodhakfrontend.util.Exporter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectAnalysisUi {
    private final Exporter exporter=new Exporter();
    private final UiFeatures uiFeatures;

    public ProjectAnalysisUi(UiFeatures uiFeatures) {
        this.uiFeatures = uiFeatures;
    }


    // completeBuild
    public Node build(ProjectInfo projectInfo) {
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
            exporter.exportAnalysis(projectInfo);
        });
        container.getChildren().add(exportbar);
        analyzeView(root, projectInfo);
        container.getChildren().add(root);
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        return scrollPane;
    }

    private void analyzeView(VBox root,ProjectInfo projectInfo) {
        createSection(root,"/icons/summary.png","Project Summary",buildProjectSummary(projectInfo),"icon-blue",true);
        createSection(root,"/icons/entryPoint.png","Entry Points",buildEntryPointSection(projectInfo),"icon-blue",true);
        createSection(root,"/icons/packageOverview.png","Package Overview",buildPackageOverView(projectInfo),"icon-blue",false);
        createSection(root,"/icons/largestFiles.png","Largest Files",buildLargestFileView(projectInfo),"icon-blue",false);
        createSection(root,"/icons/classMetric.png","Class Metrics |" + projectInfo.getClassInfos().size() + " classes",buildClassMetricsView(projectInfo),"icon-blue",false);
        createSection(root,"/icons/health.png","Project Health",buildHealthSummary(projectInfo),"icon-blue",false);
        createSection(root,"/icons/hotspot.png","Risk Hotspots", buildHotspotView(projectInfo, uiFeatures),"icon-blue",false);
        createSection(root,"/icons/unused.png","🧹 Unused or Suspicious Classes", buildUnusedClassView(projectInfo, uiFeatures),"icon-blue",false);

    }
  // for Project Overview section
    private GridPane buildProjectSummary(
            ProjectInfo projectInfo
    ) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int row = 0;
        grid.addRow(row++, new Label("Project Type:"), new Label(projectInfo.getEntryPointInfo().getProjectFlavors().toString()));
        grid.addRow(row++, new Label("Folders:"), new Label(String.valueOf(projectInfo.getKnownFolders().size())));
        grid.addRow(row++, new Label("Files:"), new Label(String.valueOf(projectInfo.getKnownFiles().size())));
        Map<String, Set<Path>> map = projectInfo.getLaguageCountMap();
        if (!map.isEmpty()) {
            for (String key : map.keySet()) {
                grid.addRow(row++, new Label(key + ":"), new Label(String.valueOf(map.get(key).size())));
            }
        }

        return grid;
    }

//for Entry Point info
private Node buildEntryPointSection(ProjectInfo projectInfo) {
    VBox box = new VBox();
    box.getStyleClass().add("card");
    box.setPadding(new Insets(5));
    Label typeLabel = new Label("Project Type:" + projectInfo.getEntryPointInfo().getProjectFlavors().toString());
    typeLabel.getStyleClass().add("label-bold");
    box.getChildren().add(typeLabel);
    // for Primary Entry Point
    if (projectInfo.getEntryPointInfo().getPrimaryEntry() != null) {
        Label primaryTitle = new Label("Primary Entry point:");
        primaryTitle.getStyleClass().add("label-bold");
        EntryPointInfo.Entry primaryEntry = projectInfo.getEntryPointInfo().getPrimaryEntry();
        Label primary = new Label("• " + getSimpleName(primaryEntry.className())+" ➡ " + primaryEntry.kind());
        box.getChildren().addAll(primaryTitle, primary);
    }
    // for secondary points
    if (!projectInfo.getEntryPointInfo().getSecondaryEntries().isEmpty()) {
        Label secondaryTitle = new Label("Other possible Entry Point:");
        secondaryTitle.getStyleClass().add("label-bold");
        box.getChildren().add(secondaryTitle);
        for (EntryPointInfo.Entry ep : projectInfo.getEntryPointInfo().getSecondaryEntries()) {
            box.getChildren().add(new Label("• " + getSimpleName(ep.className()) +" ➡ " + ep.kind()));
        }
    }

    return box;
}
// package OverView Class which pkg Contains how many classes
private Node buildPackageOverView(ProjectInfo projectInfo) {
    Map<String, PackageInfo> packageInfos = projectInfo.getPackageInfos();
    VBox box = new VBox();
    box.setPadding(new Insets(5));
    if (packageInfos.isEmpty()) {
        box.getChildren().add(new Label("No package found!"));
        return box;
    }
    packageInfos.forEach((packageName, packageInfo) -> {
        HBox packageHBox = new HBox(2);
        packageHBox.getStyleClass().add("package-row");
        Label packageLabel = new Label(packageInfo.getPackageName());
        packageLabel.getStyleClass().add("label-bold");
        int size = packageInfo.getClasses().size();
        Label classCount = new Label("• Classes:" + String.valueOf(size));
        packageHBox.getChildren().addAll(packageLabel, classCount);
        box.getChildren().addAll(packageHBox);

    });

    return box;
}


// for Largest files
private Node buildLargestFileView(ProjectInfo result) {
    VBox box = new VBox(8);
    box.setPadding(new Insets(5));
    if (result == null) {
        box.getChildren().add(new Label("No Such File Found!"));
        return box;
    }
    int rank = 1;
    for (ProjectInfo.LargestFileInfo lf : result.getLargetFiles()) {
        HBox fileHBox = new HBox();
        fileHBox.getStyleClass().add("file-row");
        Label fileLabel = new Label(rank++ + ". " + lf.getName());
        fileLabel.getStyleClass().add("label-bold");
        fileHBox.setOnMouseClicked(event -> {
            uiFeatures.openFile(lf.getSourceFile());

        });
        Label loc = new Label("• Lines of Code: " + lf.getLoc());
        fileHBox.getChildren().addAll(fileLabel, loc);
        box.getChildren().addAll(fileHBox);
    }
    return box;
}

//For the class infos
private Node buildClassMetricsView(ProjectInfo projectInfo) {
    List<ClassInfo> classes=projectInfo.getClassInfos();
    if (classes == null || classes.isEmpty()) {
        return new Label("No Class Found!");
    }

    List<ClassInfo> sorted =
            classes.stream()
                    .sorted(Comparator.comparing(
                            classInfo -> {
                                return getSimpleName(classInfo.getClassName());
                            },
                            String.CASE_INSENSITIVE_ORDER
                    ))
                    .toList();

    ListView<ClassInfo> listView = new ListView<>();
    listView.getItems().setAll(sorted);
    listView.setCellFactory(lv -> new ProjectAnalysisUi.ClassMetricsCell());

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

            title.setText(getSimpleName(cls.getClassName()));
            type.setText(cls.getKind().toString());
            pkg.setText(cls.getPackageName());

            members.setText(
                    "Methods: " + cls.getMethods().size()
                            + " | Fields: " + cls.getFields().size()
                            + " | Constructors: " + cls.getConstructors().size()
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
                        getSimpleName(cls.getClassName()),
                        cls.getBeginLine(),
                        cls.getBeginColumn(),
                        cls.getSourceFile()
                );
            });
        }
    }
// for health summary
private GridPane buildHealthSummary(ProjectInfo projectInfo) {

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(8);

    int row = 0;
    grid.addRow(row++, new Label("Total Classes:"), new Label(String.valueOf(projectInfo.getTotalClasses())));
    grid.addRow(row++, new Label("✔ Healthy Classes:"), new Label(String.valueOf(projectInfo.getHealthyClasses())));
    grid.addRow(row++, new Label("⚠ Classes with Warnings:"), new Label(String.valueOf(projectInfo.getClassesWithWarnings())));
    grid.addRow(row++, new Label("🔥 God Classes:"), new Label(String.valueOf(projectInfo.getGodClasses())));
    grid.addRow(row++, new Label("🔁 Classes in Cycles:"), new Label(String.valueOf(projectInfo.getCircularClasses())));
    grid.addRow(row++, new Label("🔗 Highly Coupled:"), new Label(String.valueOf(projectInfo.getHighlyCoupledClasses())));

    return grid;
}


// for hotspot
private Node buildHotspotView(ProjectInfo projectInfo, UiFeatures uiFeatures) {
    List<Hotspots> hotspots = projectInfo.getHotspotClasses();
    VBox box = new VBox(8);
    box.setPadding(new Insets(5));

    if (hotspots.isEmpty()) {
        box.getChildren().add(new Label("✅ No high-risk hotspots detected"));
        return box;
    }

    int rank = 1;
    for (Hotspots hs : hotspots) {

        ClassInfo ci = hs.getClassInfo();
        VBox card = new VBox(4);

        card.getStyleClass().add("hotspot-card");
        card.setPadding(new Insets(8));

        Label title = new Label(
                rank++ + ". " + ci.getClassName()
                        + " (Risk Score: " + hs.getScore() + ")"
        );
        title.getStyleClass().add("label-bold");

        Label meta = new Label(
                "LOC: " + ci.getLinesOfCode()
                        + " | Fan-in: " + ci.getUsedBy().size()
                        + " | Fan-out: " + ci.getDependsOn().size()
        );

        Label reasons = new Label("⚠ " + String.join(", ", hs.getReasons()));
        reasons.setWrapText(true);

        card.getChildren().addAll(title, meta, reasons);

        // 🔗 click navigation
        card.setOnMouseClicked(e -> {
                    uiFeatures.openAndHighlight(ci.getClassName(),ci.getBeginLine(),ci.getBeginColumn(),ci.getSourceFile());
                }
        );

        box.getChildren().add(card);
    }

    return box;
}

    // for unused Classes
    private Node buildUnusedClassView(
            ProjectInfo projectInfo, UiFeatures uiFeatures
    ) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));
        Set<UnusedClassInfo> unused = projectInfo.getUnusedClassInfos();
        if (unused.isEmpty()) {
            box.getChildren().add(new Label("✅ No Unused Class Found"));
            return box;
        }


        for (UnusedClassInfo unusedClass : unused) {
            ClassInfo ci = unusedClass.getClassInfo();
            VBox card = new VBox(4);
            card.getStyleClass().add("unused-class-card");
            card.setPadding(new Insets(8));

            Label title = new Label(ci.getClassName() +" | "+ ci.getLinesOfCode());
            title.getStyleClass().add("label-bold");

            Label meta = new Label("Package Name: " + ci.getPackageName()
            );

            Label reasons = new Label("⚠ " + unusedClass.getReason());
            reasons.setWrapText(true);

            card.getChildren().addAll(title, meta, reasons);


            // 🔗 click navigation
            card.setOnMouseClicked(e -> {
                        uiFeatures.openAndHighlight(ci.getClassName(),ci.getBeginLine(),ci.getBeginColumn(),ci.getSourceFile());
                    }
            );

            box.getChildren().add(card);
        }

        return box;
    }






    // create a Section for each contents
    private void createSection(VBox root, String iconPath, String title, Node content, String bgClass, boolean expanded){
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

    // get Simplified ClassName
    private String getSimpleName(String name){
        return name.substring(name.lastIndexOf(".")+1);
    }



}
