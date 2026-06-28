package com.example.bodhakfrontend.ui.ProjectAnalysis;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.namespace.NamespaceInfo;
import com.example.bodhakfrontend.core.model.project.*;
import com.example.bodhakfrontend.ui.ProjectAnalysis.state.*;
import com.example.bodhakfrontend.ui.helper.UiFeatures;
import com.example.bodhakfrontend.util.Exporter;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;

import java.util.List;
import java.util.Map;


public class ProjectAnalysisUi extends StackPane {
    private final Exporter exporter=new Exporter();
    private final UiFeatures uiFeatures;
    private  ProjectAnalysisState state;

    public ProjectAnalysisUi(UiFeatures uiFeatures) {
        this.uiFeatures = uiFeatures;

    }
    public void setAnalysisState(ProjectAnalysisState state) {
        this.state = state;
        // Rebuild the dashboard view reactively when context updates
        state.analysisContextProperty().addListener((obs, oldCtx, newCtx) -> {
            if (newCtx != null) {
                javafx.application.Platform.runLater(() -> {
                    getChildren().setAll(build(newCtx));
                });
            } else {
                javafx.application.Platform.runLater(this.getChildren()::clear);
            }
        });
        // Seed initial UI if context is already available
        if (state.getAnalysisContext() != null) {
            getChildren().setAll(build(state.getAnalysisContext()));
        }
    }






    // completeBuild
    public Node build(AnalysisContext context) {

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
            exporter.exportAnalysis(context);
        });
        container.getChildren().add(exportbar);
        analyzeView(root, context);
        container.getChildren().add(root);
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        return scrollPane;
    }

    private void analyzeView(VBox root, AnalysisContext context) {
        ProjectInfo projectInfo = context.getProjectInfo();
        List<EntityInfo> entities = context.getEntities();
        Map<String, NamespaceInfo> namespaces = context.getNamespaces();
        createSection(root,"/icons/summary.png","Project Type Classification",buildProjectTypeView(),"icon-blue",true);
        createSection(root,"/icons/summary.png","Project Summary",buildProjectSummary(),"icon-blue",true);
        createSection(root,"/icons/entryPoint.png","Entry Points",buildEntryPointSection(),"icon-blue",true);
        createSection(root,"/icons/packageOverview.png","Namespace Overview",buildPackageOverView(),"icon-blue",false);
        createSection(root,"/icons/largestFiles.png","Largest Files",buildLargestFileView(),"icon-blue",false);
        createSection(root,"/icons/classMetric.png","Entity Metrics |" + entities.size() + " entities",buildClassMetricsView(),"icon-blue",false);
        createSection(root,"/icons/hotspot.png","Risk Hotspots", buildHotspotView(),"icon-blue",false);
    }


    // projectType
    private Node buildProjectTypeView() {
        ProjectTypeState projectType = state.getProjectTypeState();
        VBox root = new VBox(12);
        root.getStyleClass().add("analysis-card");

        // ===== PRIMARY TYPE CARD =====
        VBox primaryCard = new VBox(6);
        primaryCard.getStyleClass().add("primary-entry-card"); // reuse similar styling card
        Label title = new Label("PRIMARY CLASSIFICATION");
        title.getStyleClass().add("entry-title");
        Label typeName = new Label();
        typeName.getStyleClass().add("entry-main");
        typeName.textProperty().bind(projectType.primaryTypeProperty());
        primaryCard.getChildren().addAll(title, typeName);
        root.getChildren().add(primaryCard);

        // ===== DETECTED TYPES & CONFIDENCES =====
        VBox typesSection = new VBox(8);
        Label typesTitle = new Label("Detected Types & Confidence Levels");
        typesTitle.getStyleClass().add("section-subtitle");
        VBox typesList = new VBox(6);
        typesSection.getChildren().addAll(typesTitle, typesList);
        root.getChildren().add(typesSection);

        // ===== DETECTED FRAMEWORKS =====
        VBox fwSection = new VBox(8);
        Label fwTitle = new Label("Detected Frameworks");
        fwTitle.getStyleClass().add("section-subtitle");
        VBox fwList = new VBox(6);
        fwSection.getChildren().addAll(fwTitle, fwList);
        root.getChildren().add(fwSection);

        // Reactive Rebuilder
        Runnable rebuild = () -> {
            typesList.getChildren().clear();
            fwList.getChildren().clear();

            for (ProjectTypeItem item : projectType.getProjectTypes()) {
                Label label = new Label("• " + item.type().name() + " (" + (int)(item.confidence() * 100) + "%)");
                label.getStyleClass().add("label-muted");
                typesList.getChildren().add(label);
            }

            for (FrameworkItem item : projectType.getDetectedFrameworks()) {
                HBox row = new HBox(8);
                Label name = new Label(item.frameworkName());
                name.getStyleClass().add("entry-name");
                Label badge = new Label(item.detected() ? "DETECTED" : "CANDIDATE");
                badge.getStyleClass().addAll("entry-kind-badge");
                row.getChildren().addAll(badge, name);
                fwList.getChildren().add(row);
            }
        };

        rebuild.run();
        projectType.getProjectTypes().addListener((ListChangeListener<ProjectTypeItem>) c -> rebuild.run());
        projectType.getDetectedFrameworks().addListener((ListChangeListener<FrameworkItem>) c -> rebuild.run());

        return root;
    }


  // for Project Overview section
  private Node buildProjectSummary() {

      ProjectSummaryState summary = state.getProjectSummary();

      VBox root = new VBox(12);
      root.getStyleClass().add("analysis-card");

      // ================= TOP METRICS =================
      HBox topRow = new HBox(10);
      VBox sizeBox = new VBox(4);
      sizeBox.getStyleClass().add("metric-box");

      Label sizeLabel = new Label("SIZE");
      sizeLabel.getStyleClass().add("metric-title");

      Label sizeValue = new Label();
      sizeValue.getStyleClass().add("metric-value");

      sizeValue.textProperty().bind(
              Bindings.createStringBinding(
                      () -> summary.totalFoldersProperty().get()
                              + " Fld / "
                              + summary.totalFilesProperty().get()
                              + " Files",
                      summary.totalFoldersProperty(),
                      summary.totalFilesProperty()
              )
      );

      sizeBox.getChildren().addAll(sizeLabel, sizeValue);

      topRow.getChildren().addAll( sizeBox);

      // ================= LANGUAGES =================
      VBox langSection = new VBox(6);

      Label langTitle = new Label("Languages");
      langTitle.getStyleClass().add("section-subtitle");

      StackPane progressBar = new StackPane();
      progressBar.getStyleClass().add("lang-bar");
      progressBar.setPrefHeight(8);
      progressBar.setMaxWidth(Double.MAX_VALUE);

      HBox segments = new HBox();
      segments.setSpacing(1);
      segments.setAlignment(Pos.CENTER_LEFT);
      segments.maxWidthProperty().bind(progressBar.widthProperty());

      FlowPane legend = new FlowPane();
      legend.setHgap(12);
      legend.setVgap(8);

      Runnable rebuildLanguages = () -> {

          segments.getChildren().clear();
          legend.getChildren().clear();

          double total =
                  summary.getLanguages()
                          .stream()
                          .mapToInt(LanguageInfo::count)
                          .sum();

          for (LanguageInfo info : summary.getLanguages()) {

              double percent =
                      total == 0 ? 0 : info.count() / total;

              Region segment = new Region();
              segment.getStyleClass().addAll(
                      "lang-segment",
                      getLangColorClass(info.language())
              );

              segment.minWidthProperty().bind(
                      progressBar.widthProperty().multiply(percent)
              );

              segment.prefWidthProperty().bind(
                      segment.minWidthProperty()
              );

              segments.getChildren().add(segment);

              Label dot = new Label("●");
              dot.getStyleClass().add(
                      getLangDotClass(info.language())
              );

              Label text = new Label(
                      info.language() + " "
                              + (int) (percent * 100)
                              + "%"
              );

              text.getStyleClass().add("label-muted");

              legend.getChildren().add(
                      new HBox(4, dot, text)
              );
          }
      };

      rebuildLanguages.run();

      summary.getLanguages().addListener(
              (ListChangeListener<LanguageInfo>) c -> rebuildLanguages.run()
      );

      progressBar.getChildren().add(segments);

      langSection.getChildren().addAll(
              langTitle,
              progressBar,
              legend
      );

      root.getChildren().addAll(
              topRow,
              langSection
      );

      return root;
  }
    private String getLangColorClass(String lang) {
        // Dynamic hash-based color assignment — works for any language without modifications.
        String[] colors = {"lang-color-0", "lang-color-1", "lang-color-2", "lang-color-3",
                           "lang-color-4", "lang-color-5", "lang-color-6", "lang-color-7"};
        int idx = Math.abs(lang.toLowerCase().hashCode()) % colors.length;
        return colors[idx];
    }

    private String getLangDotClass(String lang) {
        String[] colors = {"dot-0", "dot-1", "dot-2", "dot-3",
                           "dot-4", "dot-5", "dot-6", "dot-7"};
        int idx = Math.abs(lang.toLowerCase().hashCode()) % colors.length;
        return colors[idx];
    }
//for Entry Point info
private Node buildEntryPointSection() {

    EntryPointState entryPoint = state.getEntryPoint();

    VBox root = new VBox(12);
    root.getStyleClass().add("analysis-card");

    // ================= PRIMARY ENTRY =================

    VBox primaryCard = new VBox(6);
    primaryCard.getStyleClass().add("primary-entry-card");

    Label title = new Label("PRIMARY ENTRY");
    title.getStyleClass().add("entry-title");

    Label className = new Label();
    className.getStyleClass().add("entry-main");
    className.textProperty().bind(entryPoint.primaryNameProperty());

    Label desc = new Label();
    desc.getStyleClass().add("entry-sub");
    desc.textProperty().bind(entryPoint.primaryLabelProperty());

    primaryCard.getChildren().addAll(title, className, desc);

    // Hide the card when there is no primary entry
    primaryCard.visibleProperty().bind(
            entryPoint.primaryNameProperty().isNotEmpty()
    );
    primaryCard.managedProperty().bind(primaryCard.visibleProperty());

    root.getChildren().add(primaryCard);

    // ================= SECONDARY ENTRIES =================

    VBox secondaryCard = new VBox(8);
    secondaryCard.getStyleClass().add("secondary-entry-card");

    Label secTitle = new Label("SECONDARY ENTRIES");
    secTitle.getStyleClass().add("entry-title-muted");

    VBox list = new VBox(6);

    Runnable rebuildSecondary = () -> {

        list.getChildren().clear();

        for (EntryPointItem item : entryPoint.getSecondaryEntries()) {

            HBox row = new HBox(8);
            row.getStyleClass().add("entry-list-item");

            Label kindBadge = new Label(item.label());
            kindBadge.getStyleClass().add("entry-kind-badge");

            Label nameLabel = new Label("• " + item.displayName());
            nameLabel.getStyleClass().add("entry-name");

            row.getChildren().addAll(kindBadge, nameLabel);

            row.setOnMouseClicked(e ->
                    uiFeatures.openAndHighlight(
                            item.qualifiedName(),
                            0,
                            0,
                            null
                    )
            );

            list.getChildren().add(row);
        }

        boolean hasSecondary = !entryPoint.getSecondaryEntries().isEmpty();

        secondaryCard.setVisible(hasSecondary);
        secondaryCard.setManaged(hasSecondary);
    };

    rebuildSecondary.run();

    entryPoint.getSecondaryEntries().addListener(
            (ListChangeListener<EntryPointItem>) change ->
                    rebuildSecondary.run()
    );

    secondaryCard.getChildren().addAll(secTitle, list);

    root.getChildren().add(secondaryCard);

    return root;
}// package OverView Class which pkg Contains how many classes
    private Node buildPackageOverView() {

        NamespaceOverviewState namespaceState =
                state.getNamespaceOverview();

        VBox root = new VBox(10);

        Runnable rebuild = () -> {

            root.getChildren().clear();

            if (namespaceState.getNamespaces().isEmpty()) {

                Label empty = new Label("No package found!");
                empty.getStyleClass().add("label-muted");

                root.getChildren().add(empty);

                return;
            }

            for (NamespaceItem item : namespaceState.getNamespaces()) {

                HBox row = new HBox();
                row.getStyleClass().add("package-card");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10));

                Label packageLabel = new Label(item.namespaceName());
                packageLabel.getStyleClass().add("package-name");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label count = new Label(item.entityCount() + " classes");
                count.getStyleClass().add("package-badge");

                row.getChildren().addAll(
                        packageLabel,
                        spacer,
                        count
                );

                row.setOnMouseEntered(e ->
                        row.getStyleClass().add("package-card-hover"));

                row.setOnMouseExited(e ->
                        row.getStyleClass().remove("package-card-hover"));

                root.getChildren().add(row);
            }
        };

        rebuild.run();

        namespaceState.getNamespaces().addListener(
                (ListChangeListener<NamespaceItem>) change ->
                        rebuild.run()
        );

        return root;
    }
// for Largest files
private Node buildLargestFileView() {

    LargestFilesState largestFiles =
            state.getLargestFiles();

    VBox root = new VBox(10);

    Runnable rebuild = () -> {

        root.getChildren().clear();

        if (largestFiles.getFiles().isEmpty()) {

            Label empty = new Label("No File found!");
            empty.getStyleClass().add("label-muted");

            root.getChildren().add(empty);

            return;
        }

        for (LargestFileItem file : largestFiles.getFiles()) {

            HBox row = new HBox();
            row.getStyleClass().add("package-card");
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));

            Label fileName = new Label(file.name());
            fileName.getStyleClass().add("package-name");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lines = new Label(file.loc() + " lines");
            lines.getStyleClass().add("package-badge");

            row.getChildren().addAll(
                    fileName,
                    spacer,
                    lines
            );

            row.setOnMouseEntered(e ->
                    row.getStyleClass().add("package-card-hover"));

            row.setOnMouseExited(e ->
                    row.getStyleClass().remove("package-card-hover"));

            row.setOnMouseClicked(e ->
                    uiFeatures.openFile(file.sourceFile())
            );

            root.getChildren().add(row);
        }
    };

    rebuild.run();

    largestFiles.getFiles().addListener(
            (ListChangeListener<LargestFileItem>) change ->
                    rebuild.run()
    );

    return root;
}

    private Node buildClassMetricsView() {

        EntityMetricsState entityMetrics = state.getEntityMetricsState();

        ListView<EntityMetricItem> listView = new ListView<>();

        listView.setItems(entityMetrics.getEntities());

        listView.setFocusTraversable(true);

        listView.setOnKeyPressed(event -> {

            EntityMetricItem selected =
                    listView.getSelectionModel().getSelectedItem();

            if (selected == null) {
                return;
            }

            switch (event.getCode()) {

                case ENTER -> uiFeatures.openAndHighlight(
                        selected.simpleName(),
                        selected.beginLine(),
                        selected.beginColumn(),
                        selected.sourceFile()
                );

                case UP -> listView.getSelectionModel().selectPrevious();

                case DOWN -> listView.getSelectionModel().selectNext();
            }
        });

        listView.setCellFactory(lv -> new ClassMetricsCell());

        return listView;
    }
    private class ClassMetricsCell extends ListCell<EntityMetricItem> {

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
        protected void updateItem(EntityMetricItem cls, boolean empty) {
            super.updateItem(cls, empty);

            if (empty || cls == null) {
                setGraphic(null);
                return;
            }

            // ===== DATA =====
            className.setText(getSimpleName(cls.simpleName()));
            packageName.setText(cls.namespace());

            meth.setText(String.valueOf(cls.methods()));
            flds.setText(String.valueOf(cls.fields()));
            cons.setText(String.valueOf(cls.constructors()));
            loc.setText(String.valueOf(cls.loc()));

            if (cls.isAbstract()) {
                badge.setText("ABSTRACT");
                badge.getStyleClass().setAll("class-badge", "badge-abstract");
            } else if (cls.isFinal()) {
                badge.setText("FINAL");
                badge.getStyleClass().setAll("class-badge", "badge-final");
            } else {
                badge.setText(cls.kind().name());
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
            root.setOnMouseClicked(e -> {
                getListView().getSelectionModel().select(getIndex());
                uiFeatures.openAndHighlight(
                        getSimpleName(cls.qualifiedName()),
                        cls.beginLine(),
                        cls.beginColumn(),
                        cls.sourceFile()
                );
            });

            // ===== HOVER =====
            root.setOnMouseEntered(e -> root.getStyleClass().add("class-card-hover"));
            root.setOnMouseExited(e -> root.getStyleClass().remove("class-card-hover"));
        }
    }
// for health summary


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
    private Node buildHotspotView() {

        HotspotState hotspotState = state.getHotspot();

        VBox root = new VBox(12);
        root.setFillWidth(true);

        Runnable rebuild = () -> {

            root.getChildren().clear();

            if (hotspotState.getHotspots().isEmpty()) {

                Label empty = new Label("✅ No high-risk hotspots detected");
                empty.getStyleClass().add("label-muted");

                root.getChildren().add(empty);
                return;
            }

            for (HotspotItem item : hotspotState.getHotspots()) {

                VBox card = new VBox(10);
                card.getStyleClass().add("hotspot-card");
                card.setPadding(new Insets(12));
                card.setMaxWidth(Double.MAX_VALUE);

                // ================= TOP ROW =================

                HBox topRow = new HBox(10);
                topRow.setAlignment(Pos.CENTER_LEFT);

                VBox left = new VBox(4);
                left.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(left, Priority.ALWAYS);

                Label title = new Label(item.simpleName());
                title.getStyleClass().add("hotspot-title");

                Label desc = new Label(item.description());
                desc.getStyleClass().add("hotspot-desc");
                desc.setWrapText(true);

                left.getChildren().addAll(title, desc);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label badge = new Label(item.riskLabel());
                badge.getStyleClass().addAll(
                        "risk-badge",
                        item.riskCss()
                );

                topRow.getChildren().addAll(
                        left,
                        spacer,
                        badge
                );

                // ================= METRICS =================

                HBox metrics = new HBox(30);
                metrics.setAlignment(Pos.CENTER_LEFT);

                metrics.getChildren().addAll(

                        metric(
                                "LOC",
                                String.valueOf(item.loc())
                        ),

                        metric(
                                "FI",
                                String.valueOf(item.fanIn())
                        ),

                        metric(
                                "FO",
                                String.valueOf(item.fanOut())
                        )
                );

                card.getChildren().addAll(
                        topRow,
                        metrics
                );

                card.setOnMouseClicked(e -> {

                    uiFeatures.openAndHighlight(

                            item.qualifiedName(),

                            item.beginLine(),

                            item.beginColumn(),

                            item.sourceFile()
                    );

                });

                card.setOnMouseEntered(e ->
                        card.getStyleClass().add("hotspot-hover"));

                card.setOnMouseExited(e ->
                        card.getStyleClass().remove("hotspot-hover"));

                root.getChildren().add(card);
            }
        };

        rebuild.run();

        hotspotState.getHotspots().addListener(
                (ListChangeListener<HotspotItem>) change ->
                        rebuild.run()
        );

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
            List<EntityInfo> entities, UiFeatures uiFeatures
    ) {
        VBox root = new VBox(12);
        root.setFillWidth(true);

        // Entities with no usedBy references are effectively unused
        List<EntityInfo> unused = entities.stream()
                .filter(e -> e.getUsedBy().isEmpty())
                .toList();

        if (unused.isEmpty()) {
            Label empty = new Label("✅ No Unused Entity Found");
            empty.getStyleClass().add("label-muted");
            root.getChildren().add(empty);
            return root;
        }

        for (EntityInfo ci : unused) {

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
                    getSimpleName(ci.getEntityName()) + "  •  " + ci.getLinesOfCode() + " LOC"
            );
            title.getStyleClass().add("unused-title");

            Label pkg = new Label(ci.getNamespaceName());
            pkg.getStyleClass().add("unused-package");

            left.getChildren().addAll(title, pkg);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label warning = new Label("⚠");
            warning.getStyleClass().add("unused-warning");

            topRow.getChildren().addAll(left, spacer, warning);

            Label reason = new Label("No incoming references detected");
            reason.getStyleClass().add("unused-reason");
            reason.setWrapText(true);

            // ================= CLICK =================
            card.setOnMouseClicked(e -> {
                uiFeatures.openAndHighlight(
                        getSimpleName(ci.getEntityName()),
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
