package com.example.bodhakfrontend.ui.ProjectAnalysis;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Package.PackageInfo;
import com.example.bodhakfrontend.Backend.models.Project.EntryPointInfo;
import com.example.bodhakfrontend.Backend.models.Project.Hotspots;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;

import com.example.bodhakfrontend.Backend.models.Project.UnusedClassInfo;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import com.example.bodhakfrontend.util.Exporter;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
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
        VBox root=new VBox(10);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("card");
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        HBox exportbar=new HBox();
        exportbar.setAlignment(Pos.CENTER_RIGHT);
        Button exportBtn = new Button("Export");
        exportBtn.getStyleClass().addAll("btn-secondary", "editor-bottom-btn");
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
        createSection(root,"/icons/health.png","Project Health",buildHealthSummary(projectInfo),"icon-blue",true);
        createSection(root,"/icons/hotspot.png","Risk Hotspots", buildHotspotView(projectInfo, uiFeatures),"icon-blue",false);
        createSection(root,"/icons/unused.png","🧹 Unused or Suspicious Classes", buildUnusedClassView(projectInfo, uiFeatures),"icon-blue",false);

    }
  // for Project Overview section
  private Node buildProjectSummary(ProjectInfo projectInfo) {

      VBox root = new VBox(12);
      root.getStyleClass().add("analysis-card");

      // ================= TOP METRICS =================
      HBox topRow = new HBox(10);

      VBox typeBox = new VBox(4);
      typeBox.getStyleClass().add("metric-box");

      Label typeLabel = new Label("TYPE");
      typeLabel.getStyleClass().add("metric-title");

      Label typeValue = new Label(
              projectInfo.getEntryPointInfo().getProjectFlavors().toString()
      );
      typeValue.getStyleClass().add("metric-value");

      typeBox.getChildren().addAll(typeLabel, typeValue);


      VBox sizeBox = new VBox(4);
      sizeBox.getStyleClass().add("metric-box");

      Label sizeLabel = new Label("SIZE");
      sizeLabel.getStyleClass().add("metric-title");

      String sizeText = projectInfo.getKnownFolders().size()
              + " Fld / " + projectInfo.getKnownFiles().size() + " Files";

      Label sizeValue = new Label(sizeText);
      sizeValue.getStyleClass().add("metric-value");

      sizeBox.getChildren().addAll(sizeLabel, sizeValue);

      topRow.getChildren().addAll(typeBox, sizeBox);

      // ================= LANGUAGES =================
      VBox langSection = new VBox(6);

      Label langTitle = new Label("Languages");
      langTitle.getStyleClass().add("section-subtitle");

      Map<String, Set<Path>> map = projectInfo.getLaguageCountMap();

      int total = map.values().stream().mapToInt(Set::size).sum();

      // 🔥 Progress Bar Container
      StackPane progressBar = new StackPane();
      progressBar.getStyleClass().add("lang-bar");
      progressBar.setPrefHeight(8);
      progressBar.setMaxWidth(Double.MAX_VALUE);

      HBox segments = new HBox();
      segments.setSpacing(2);

      HBox legend = new HBox(12);

      for (String key : map.keySet()) {

          int count = map.get(key).size();
          double percent = total == 0 ? 0 : (double) count / total;

          // ===== SEGMENT =====
          Region segment = new Region();
          segment.getStyleClass().addAll("lang-segment", getLangColorClass(key));

          // 🔥 REAL PROPORTIONAL WIDTH (KEY FIX)
          segment.prefWidthProperty().bind(
                  progressBar.widthProperty().multiply(percent)
          );

          segments.getChildren().add(segment);

          // ===== LEGEND =====
          Label dot = new Label("●");
          dot.getStyleClass().add(getLangDotClass(key));

          Label text = new Label(key + " " + (int)(percent * 100) + "%");
          text.getStyleClass().add("label-muted");

          HBox item = new HBox(4, dot, text);
          legend.getChildren().add(item);
      }

      progressBar.getChildren().add(segments);

      langSection.getChildren().addAll(langTitle, progressBar, legend);

      root.getChildren().addAll(topRow, langSection);

      return root;
  }
    private String getLangColorClass(String lang) {
        return switch (lang.toLowerCase()) {
            case "java" -> "lang-java";
            case "xml" -> "lang-xml";
            case "css" -> "lang-css";
            default -> "lang-unknown";
        };
    }

    private String getLangDotClass(String lang) {
        return switch (lang.toLowerCase()) {
            case "java" -> "dot-java";
            case "xml" -> "dot-xml";
            case "css" -> "dot-css";
            default -> "dot-unknown";
        };
    }
//for Entry Point info
private Node buildEntryPointSection(ProjectInfo projectInfo) {

    VBox root = new VBox(12);
    root.getStyleClass().add("analysis-card");

    // ================= PRIMARY ENTRY =================
    EntryPointInfo.Entry primaryEntry = projectInfo.getEntryPointInfo().getPrimaryEntry();

    if (primaryEntry != null) {

        VBox primaryCard = new VBox(6);
        primaryCard.getStyleClass().add("primary-entry-card");

        Label title = new Label("PRIMARY ENTRY");
        title.getStyleClass().add("entry-title");

        Label className = new Label(getSimpleName(primaryEntry.className()) + ".java");
        className.getStyleClass().add("entry-main");

        String kind=primaryEntry.kind().toString();

        Label desc = new Label(kind);
        desc.getStyleClass().add("entry-sub");

        primaryCard.getChildren().addAll(title, className, desc);
        root.getChildren().add(primaryCard);
    }

    // ================= SECONDARY ENTRIES =================
    Set<EntryPointInfo.Entry> secondary = projectInfo.getEntryPointInfo().getSecondaryEntries();

    if (!secondary.isEmpty()) {

        VBox secondaryCard = new VBox(8);
        secondaryCard.getStyleClass().add("secondary-entry-card");

        Label secTitle = new Label("SECONDARY ENTRIES");
        secTitle.getStyleClass().add("entry-title-muted");

        VBox list = new VBox(6);

        for (EntryPointInfo.Entry ep : secondary) {

            Label item = new Label("• " + getSimpleName(ep.className()) + ".java");
            item.getStyleClass().add("entry-list-item");

            item.setOnMouseClicked(e -> {
                uiFeatures.openAndHighlight(
                        getSimpleName(ep.className()),
                        0, 0,
                        null
                );
            });

            list.getChildren().add(item);
        }

        secondaryCard.getChildren().addAll(secTitle, list);
        root.getChildren().add(secondaryCard);
    }

    return root;
}
// package OverView Class which pkg Contains how many classes
private Node buildPackageOverView(ProjectInfo projectInfo) {

    Map<String, PackageInfo> packageInfos = projectInfo.getPackageInfos();

    VBox root = new VBox(10); // spacing between cards

    if (packageInfos.isEmpty()) {
        Label empty = new Label("No package found!");
        empty.getStyleClass().add("label-muted");
        root.getChildren().add(empty);
        return root;
    }

    packageInfos.forEach((packageName, packageInfo) -> {

        HBox row = new HBox();
        row.getStyleClass().add("package-card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        // ===== LEFT (PACKAGE NAME) =====
        Label packageLabel = new Label(packageInfo.getPackageName());
        packageLabel.getStyleClass().add("package-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ===== RIGHT (COUNT BADGE) =====
        int size = packageInfo.getClasses().size();

        Label count = new Label(size + " classes");
        count.getStyleClass().add("package-badge");

        row.getChildren().addAll(packageLabel, spacer, count);

        // optional: hover interaction
        row.setOnMouseEntered(e -> row.getStyleClass().add("package-card-hover"));
        row.setOnMouseExited(e -> row.getStyleClass().remove("package-card-hover"));

        root.getChildren().add(row);
    });

    return root;
}

// for Largest files
private Node buildLargestFileView(ProjectInfo result) {


    VBox root = new VBox(10); // spacing between cards

    if (result==null) {
        Label empty = new Label("No File found!");
        empty.getStyleClass().add("label-muted");
        root.getChildren().add(empty);
        return root;
    }
    List<ProjectInfo.LargestFileInfo> files=result.getLargetFiles();

    for(ProjectInfo.LargestFileInfo lf:files){
        HBox row = new HBox();
        row.getStyleClass().add("package-card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        // ===== LEFT (File name) =====
        Label packageLabel = new Label(lf.getName());
        packageLabel.getStyleClass().add("package-name");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // ===== RIGHT (COUNT BADGE) =====
        int line=lf.getLoc();

        Label count = new Label(line+ " lines");
        count.getStyleClass().add("package-badge");

        row.getChildren().addAll(packageLabel, spacer, count);

        // optional: hover interaction
        row.setOnMouseEntered(e -> row.getStyleClass().add("package-card-hover"));
        row.setOnMouseExited(e -> row.getStyleClass().remove("package-card-hover"));
        row.setOnMouseClicked(event -> {
            uiFeatures.openFile(lf.getSourceFile());

        });
        root.getChildren().add(row);


    }
    return root;
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
    listView.setFocusTraversable(true);
    listView.setOnKeyPressed(event -> {
        ClassInfo selected = listView.getSelectionModel().getSelectedItem();

        if (selected == null) return;

        switch (event.getCode()) {

            case ENTER -> {
                uiFeatures.openAndHighlight(
                        getSimpleName(selected.getClassName()),
                        selected.getBeginLine(),
                        selected.getBeginColumn(),
                        selected.getSourceFile()
                );
            }

            case UP -> {
                listView.getSelectionModel().selectPrevious();
            }

            case DOWN -> {
                listView.getSelectionModel().selectNext();
            }
        }
    });
    listView.getItems().setAll(sorted);
    listView.setCellFactory(lv -> new ProjectAnalysisUi.ClassMetricsCell());

    return listView;
}
    private class ClassMetricsCell extends ListCell<ClassInfo> {

        private final VBox root = new VBox(10);

        private final Label className = new Label();
        private final Label packageName = new Label();
        private final Label badge = new Label();
        private final Label modifier = new Label();

        private final Label meth = new Label();
        private final Label flds = new Label();
        private final Label cons = new Label();
        private final Label loc = new Label();

        public ClassMetricsCell() {

            root.getStyleClass().add("class-card");
            root.setPadding(new Insets(12));

            // ===== TOP ROW =====
            HBox topRow = new HBox();

            VBox left = new VBox(4);
            className.getStyleClass().add("class-title");
            packageName.getStyleClass().add("class-package");

            left.getChildren().addAll(className, packageName);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            VBox right = new VBox(4);
            right.setAlignment(Pos.CENTER_RIGHT);

            badge.getStyleClass().add("class-badge");
            modifier.getStyleClass().add("class-modifier");

            right.getChildren().addAll(badge, modifier);

            topRow.getChildren().addAll(left, spacer, right);

            // ===== METRICS ROW =====
            HBox metricsRow = new HBox(30);
            metricsRow.getStyleClass().add("metrics-row");

            metricsRow.getChildren().addAll(
                    metricBox("METH", meth),
                    metricBox("FLDS", flds),
                    metricBox("CONS", cons),
                    metricBox("LOC", loc)
            );

            root.getChildren().addAll(topRow, metricsRow);
        }

        private VBox metricBox(String label, Label value) {
            VBox box = new VBox(4);
            box.setAlignment(Pos.CENTER);

            Label title = new Label(label);
            title.getStyleClass().add("metric-title-small");

            value.getStyleClass().add("metric-value");

            box.getChildren().addAll(title, value);
            return box;
        }

        @Override
        protected void updateItem(ClassInfo cls, boolean empty) {
            super.updateItem(cls, empty);

            if (empty || cls == null) {
                setGraphic(null);
                return;
            }

            // ===== DATA =====
            className.setText(getSimpleName(cls.getClassName()));
            packageName.setText(cls.getPackageName());

            meth.setText(String.valueOf(cls.getMethods().size()));
            flds.setText(String.valueOf(cls.getFields().size()));
            cons.setText(String.valueOf(cls.getConstructors().size()));
            loc.setText(String.valueOf(cls.getLinesOfCode()));

            // ===== BADGE =====
            if (cls.isAbstract()) {
                badge.setText("ABSTRACT CLASS");
                badge.getStyleClass().setAll("class-badge", "badge-abstract");
            } else if (cls.isFinal()) {
                badge.setText("FINAL CLASS");
                badge.getStyleClass().setAll("class-badge", "badge-final");
            } else {
                badge.setText("CLASS");
                badge.getStyleClass().setAll("class-badge","badge-class");
            }

            // ===== MODIFIER =====
            modifier.setText(cls.isPublic() ? "public" : "");

            setGraphic(root);
            root.pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("selected"),
                    isSelected()
            );

            selectedProperty().addListener((obs, was, isNow) -> {
                root.pseudoClassStateChanged(
                        PseudoClass.getPseudoClass("selected"),
                        isNow
                );
            });
            // ===== CLICK =====
            root.setOnMouseClicked(e -> {
                getListView().getSelectionModel().select(getIndex());
                uiFeatures.openAndHighlight(
                        getSimpleName(cls.getClassName()),
                        cls.getBeginLine(),
                        cls.getBeginColumn(),
                        cls.getSourceFile()
                );
            });

            // ===== HOVER =====
            root.setOnMouseEntered(e -> root.getStyleClass().add("class-card-hover"));
            root.setOnMouseExited(e -> root.getStyleClass().remove("class-card-hover"));
        }
    }
// for health summary
private Node buildHealthSummary(ProjectInfo projectInfo) {

    VBox root = new VBox(12);
    root.getStyleClass().add("analysis-card");

    // ================= TOP METRICS =================
    FlowPane topRow = new FlowPane();
    topRow.setHgap(10);
    topRow.setVgap(10);

    // 🔥 IMPORTANT: allow wrapping based on parent width
    topRow.prefWrapLengthProperty().bind(root.widthProperty());

    // ===== HEALTHY =====
    VBox healthyBox = createHealthBox(
            String.valueOf(projectInfo.getHealthyClasses()),
            "HEALTHY",
            "health-value"
    );

    // ===== WARNINGS =====
    VBox warningBox = createHealthBox(
            String.valueOf(projectInfo.getClassesWithWarnings()),
            "WARNINGS",
            "warning-value"
    );

    // ===== CYCLES =====
    VBox cycleBox = createHealthBox(
            String.valueOf(projectInfo.getCircularClasses()),
            "CYCLES",
            "warning-value"
    );

    // ===== COUPLED =====
    VBox highBoundBox = createHealthBox(
            String.valueOf(projectInfo.getHighlyCoupledClasses()),
            "HIGHLY COUPLED",
            "warning-value"
    );

    topRow.getChildren().addAll(healthyBox, warningBox, cycleBox, highBoundBox);

    // ================= GOD CLASS ALERT =================
    VBox alertBox = new VBox(4);
    alertBox.getStyleClass().add("health-alert");

    int godClasses = projectInfo.getGodClasses();

    Label alertTitle = new Label(godClasses + " God Classes");
    alertTitle.getStyleClass().add("alert-title");

    Label alertSubtitle = new Label("REQUIRES REFACTORING");
    alertSubtitle.getStyleClass().add("alert-subtitle");

    alertBox.getChildren().addAll(alertTitle, alertSubtitle);

    root.getChildren().addAll(topRow, alertBox);

    return root;
}

    private VBox createHealthBox(String value, String label, String valueStyle) {

        VBox box = new VBox(4);
        box.getStyleClass().add("health-box");

        //
        box.setPrefWidth(120);

        Label val = new Label(value);
        val.getStyleClass().add(valueStyle);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("health-label");

        box.getChildren().addAll(val, lbl);

        return box;
    }
// for hotspot
private Node buildHotspotView(ProjectInfo projectInfo, UiFeatures uiFeatures) {

    List<Hotspots> hotspots = projectInfo.getHotspotClasses();

    VBox root = new VBox(12);
    root.setFillWidth(true); // ✅ IMPORTANT

    if (hotspots.isEmpty()) {
        Label empty = new Label("✅ No high-risk hotspots detected");
        empty.getStyleClass().add("label-muted");
        root.getChildren().add(empty);
        return root;
    }

    for (Hotspots hs : hotspots) {

        ClassInfo ci = hs.getClassInfo();

        VBox card = new VBox(10);
        card.getStyleClass().addAll("hotspot-card", getRiskClass(hs.getScore()));
        card.setPadding(new Insets(12));

        card.setMaxWidth(Double.MAX_VALUE); // ✅ CRITICAL FIX

        // ================= TOP ROW =================
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(4);
        left.setMaxWidth(Double.MAX_VALUE); // ✅ IMPORTANT
        HBox.setHgrow(left, Priority.ALWAYS); // ✅ IMPORTANT

        Label title = new Label(getSimpleName(ci.getClassName()));
        title.getStyleClass().add("hotspot-title");

        Label desc = new Label(String.join(", ", hs.getReasons()));
        desc.getStyleClass().add("hotspot-desc");
        desc.setWrapText(true);

        left.getChildren().addAll(title, desc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(getRiskLabel(hs.getScore()));
        badge.getStyleClass().addAll("risk-badge", getRiskClass(hs.getScore()));

        topRow.getChildren().addAll(left, spacer, badge);

        // ================= METRICS =================
        HBox metrics = new HBox(30);
        metrics.setAlignment(Pos.CENTER_LEFT); // ✅ small improvement

        metrics.getChildren().addAll(
                metric("LOC", String.valueOf(ci.getLinesOfCode())),
                metric("FI", String.valueOf(ci.getUsedBy().size())),
                metric("FO", String.valueOf(ci.getDependsOn().size()))
        );

        // ================= CLICK =================
        card.setOnMouseClicked(e -> {
            uiFeatures.openAndHighlight(
                    getSimpleName(ci.getClassName()),
                    ci.getBeginLine(),
                    ci.getBeginColumn(),
                    ci.getSourceFile()
            );
        });

        // hover
        card.setOnMouseEntered(e -> card.getStyleClass().add("hotspot-hover"));
        card.setOnMouseExited(e -> card.getStyleClass().remove("hotspot-hover"));

        card.getChildren().addAll(topRow, metrics); // ✅ YOU MISSED THIS EARLIER

        root.getChildren().add(card);
    }

    return root;
}
private VBox metric(String title, String value) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label(title);
        t.getStyleClass().add("metric-title-small");

        Label v = new Label(value);
        v.getStyleClass().add("metric-value");

        box.getChildren().addAll(t, v);
        return box;
    }private String getRiskClass(int score) {
        if (score > 9) return "risk-critical";
        if (score > 6) return "risk-high";
        return "risk-medium";
    }

    private String getRiskLabel(int score) {
        if (score > 9) return "CRITICAL";
        if (score > 6) return "HIGH";
        return "MEDIUM";
    }

    // for unused Classes
    private Node buildUnusedClassView(
            ProjectInfo projectInfo, UiFeatures uiFeatures
    ) {
        VBox root = new VBox(12);
        root.setFillWidth(true);

        Set<UnusedClassInfo> unused = projectInfo.getUnusedClassInfos();

        if (unused.isEmpty()) {
            Label empty = new Label("✅ No Unused Class Found");
            empty.getStyleClass().add("label-muted");
            root.getChildren().add(empty);
            return root;
        }

        for (UnusedClassInfo unusedClass : unused) {

            ClassInfo ci = unusedClass.getClassInfo();

            VBox card = new VBox(8);
            card.getStyleClass().add("unused-card");
            card.setPadding(new Insets(12));
            card.setMaxWidth(Double.MAX_VALUE);

            // ================= TOP ROW =================
            HBox topRow = new HBox(10);
            topRow.setAlignment(Pos.CENTER_LEFT);

            VBox left = new VBox(4);
            left.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(left, Priority.ALWAYS);

            Label title = new Label(
                    getSimpleName(ci.getClassName()) + "  •  " + ci.getLinesOfCode() + " LOC"
            );
            title.getStyleClass().add("unused-title");

            Label pkg = new Label(ci.getPackageName());
            pkg.getStyleClass().add("unused-package");

            left.getChildren().addAll(title, pkg);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label warning = new Label("⚠");
            warning.getStyleClass().add("unused-warning");

            topRow.getChildren().addAll(left, spacer, warning);

            // ================= REASON =================
            Label reason = new Label(unusedClass.getReason());
            reason.getStyleClass().add("unused-reason");
            reason.setWrapText(true);

            // ================= CLICK =================
            card.setOnMouseClicked(e -> {
                uiFeatures.openAndHighlight(
                        getSimpleName(ci.getClassName()),
                        ci.getBeginLine(),
                        ci.getBeginColumn(),
                        ci.getSourceFile()
                );
            });

            // hover
            card.setOnMouseEntered(e -> card.getStyleClass().add("unused-hover"));
            card.setOnMouseExited(e -> card.getStyleClass().remove("unused-hover"));

            card.getChildren().addAll(topRow, reason);

            root.getChildren().add(card);
        }

        return root;
    }





    // create a Section for each contents
//    private void createSection(VBox root, String iconPath, String title, Node content, String bgClass, boolean expanded){
//        Node badge=iconBadge(iconPath,bgClass);
//        Label titleLabel = new Label(title);
//        titleLabel.getStyleClass().add("section-title");
//        HBox header = new HBox(10,badge,titleLabel);
//        header.setAlignment(Pos.CENTER_LEFT);
//        TitledPane titledPane = new TitledPane();
//        titledPane.setGraphic(header);
//        titledPane.setContent(content);
//        titledPane.setExpanded(expanded);
//        root.getChildren().add(titledPane);
//        root.getChildren().add(new Separator());
//
//    }
    private void createSection(VBox root, String iconPath, String title, Node content, String bgClass, boolean expanded){

        VBox container = new VBox();
        container.getStyleClass().add("analysis-section");

        HBox header = new HBox(10);
        header.getStyleClass().add("section-header");

        Node badge = iconBadge(iconPath, bgClass);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title-modern");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label(expanded ? "▲" : "▼");
        arrow.getStyleClass().add("section-arrow");

        header.getChildren().addAll(badge, titleLabel, spacer, arrow);


        VBox contentWrapper = new VBox(content);
        contentWrapper.getStyleClass().add("section-content");

        contentWrapper.setVisible(expanded);
        contentWrapper.setManaged(expanded);

        // ===== ANIMATION (IMPORTANT) =====
        contentWrapper.setMaxHeight(expanded ? Region.USE_COMPUTED_SIZE : 0);

        header.setOnMouseClicked(e -> {
            boolean isOpen = contentWrapper.isVisible();

            if (isOpen) {
                // collapse
                contentWrapper.setVisible(false);
                contentWrapper.setManaged(false);
                contentWrapper.setMaxHeight(0);
                arrow.setText("▼");
            } else {
                // expand
                contentWrapper.setVisible(true);
                contentWrapper.setManaged(true);
                contentWrapper.setMaxHeight(Region.USE_COMPUTED_SIZE);
                arrow.setText("▲");
            }
        });

        container.getChildren().addAll(header, contentWrapper);

        root.getChildren().add(container);
    }
    private Node iconBadge(String iconPath,String bgClass){

        URL url=getClass().getResource(iconPath);
        ImageView icon;
        if(url!=null){
            icon=new ImageView(new Image(url.toExternalForm()));
        }
        else
        {icon=new ImageView();}
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
