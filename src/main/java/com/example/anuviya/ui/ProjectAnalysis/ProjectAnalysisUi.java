package com.example.anuviya.ui.ProjectAnalysis;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.ui.ProjectAnalysis.state.*;
import com.example.anuviya.ui.helper.UiFeatures;
import com.example.anuviya.ui.nav.NavTab;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.Consumer;

public class ProjectAnalysisUi extends StackPane {
    private final UiFeatures uiFeatures;
    private ProjectAnalysisState state;
    private Consumer<NavTab> onNavigate;

    // Selected Entity Properties
    private final StringProperty selectedEntityName = new SimpleStringProperty("Select an entity...");
    private final StringProperty selectedEntityFqn = new SimpleStringProperty("");
    private final StringProperty selectedEntityKind = new SimpleStringProperty("");
    private final StringProperty selectedAvatarLetter = new SimpleStringProperty("?");
    private final StringProperty selectedLoc = new SimpleStringProperty("-");
    private final StringProperty selectedMethods = new SimpleStringProperty("-");
    private final StringProperty selectedFields = new SimpleStringProperty("-");
    private final StringProperty selectedDeps = new SimpleStringProperty("-");
    private final StringProperty selectedFanIn = new SimpleStringProperty("-");
    private final StringProperty selectedFanOut = new SimpleStringProperty("-");
    private final ObjectProperty<EntityInfo> currentSelectedEntity = new SimpleObjectProperty<>(null);

    public ProjectAnalysisUi(UiFeatures uiFeatures) {
        this.uiFeatures = uiFeatures;
    }

    public void setOnNavigate(Consumer<NavTab> onNavigate) {
        this.onNavigate = onNavigate;
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

    public Node build(AnalysisContext context) {
        VBox root = new VBox(16);
        root.getStyleClass().add("hub-root");

        // 1. Header (INTELLIGENCE HUB)
        HBox header = new HBox(8);
        header.getStyleClass().add("hub-title-box");
        Label iconLabel = new Label("⚙");
        iconLabel.getStyleClass().add("hub-icon");
        Label titleLabel = new Label("INTELLIGENCE HUB");
        titleLabel.getStyleClass().add("hub-title");
        header.getChildren().addAll(iconLabel, titleLabel);
        root.getChildren().add(header);

        // 2. Project Overview
        root.getChildren().add(buildProjectOverview(context));

        // 3. Language Composition
        root.getChildren().add(buildLanguageComposition(context));

        // 4. Entity Composition
        root.getChildren().add(buildEntityComposition(context));

        // 5. Technologies
        root.getChildren().add(buildTechnologies(context));

        // 6. Entity Explorer
        root.getChildren().add(buildEntityExplorer(context));

        // 7. Selected Entity
        root.getChildren().add(buildSelectedEntity(context));



        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scrollPane;
    }

    private Node buildProjectOverview(AnalysisContext context) {
        VBox card = new VBox(10);
        card.getStyleClass().add("hub-card");

        Label title = new Label("PROJECT OVERVIEW");
        title.getStyleClass().add("hub-card-title");
        card.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("overview-grid");

        String prjName = context.getProjectInfo().projectName();
        String mainLang = "Unknown";
        if (state.getProjectSummary() != null && !state.getProjectSummary().getLanguages().isEmpty()) {
            mainLang = state.getProjectSummary().getLanguages().stream()
                .max(Comparator.comparingInt(LanguageInfo::count))
                .map(LanguageInfo::language)
                .orElse("Unknown");
        }

        String prjType = context.getClassificationResult().primaryType().name();
        String buildTool = context.getProjectModel().buildModel().primaryToolName();
        if (buildTool == null || "unknown".equals(buildTool)) {
            buildTool = "Poetry";
        }

        addRowToOverview(grid, 0, "Project", prjName);
        addRowToOverview(grid, 1, "Language", mainLang);
        addRowToOverview(grid, 2, "Type", prjType);
        addRowToOverview(grid, 3, "Build Tool", buildTool);

        Label statusLabel = new Label("Analysis Ready");
        statusLabel.getStyleClass().add("status-ready");
        HBox statusBox = new HBox(6, new Label("●"), statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label statusText = new Label("Status");
        statusText.getStyleClass().add("overview-label");
        grid.add(statusText, 0, 4);
        grid.add(statusBox, 1, 4);

        card.getChildren().add(grid);
        return card;
    }

    private void addRowToOverview(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("overview-label");
        Label val = new Label(value);
        val.getStyleClass().add("overview-value");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private Node buildLanguageComposition(AnalysisContext context) {
        VBox card = new VBox(10);
        card.getStyleClass().add("hub-card");

        Label title = new Label("LANGUAGE COMPOSITION");
        title.getStyleClass().add("hub-card-title");
        card.getChildren().add(title);

        ProjectSummaryState summary = state.getProjectSummary();
        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(false);
        pieChart.setLabelsVisible(false);
        pieChart.getStyleClass().add("lang-pie-chart");
        pieChart.setMinSize(120, 120);
        pieChart.setPrefSize(120, 120);
        pieChart.setMaxSize(120, 120);

        Circle hole = new Circle(32);
        hole.setStyle("-fx-fill: #0d141a;");

        StackPane chartPane = new StackPane(pieChart, hole);
        chartPane.setMaxSize(120, 120);
        chartPane.setMinSize(120, 120);

        VBox leftBox = new VBox(8);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        VBox rightBox = new VBox(8);
        rightBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        StackPane compositionContainer = new StackPane();
        compositionContainer.setAlignment(Pos.CENTER);

        Runnable rebuildLanguages = () -> {
            leftBox.getChildren().clear();
            rightBox.getChildren().clear();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            List<HBox> legendItems = new ArrayList<>();
            double total = summary.getLanguages().stream().mapToInt(LanguageInfo::count).sum();
            for (LanguageInfo info : summary.getLanguages()) {
                double percent = total == 0 ? 0 : info.count() / total;
                if (percent <= 0) continue;

                pieData.add(new PieChart.Data(info.language(), percent));

                Label dot = new Label("●");
                dot.getStyleClass().add(getLangDotClass(info.language()));
                Label text = new Label(info.language() + " " + (int) (percent * 100) + "%");
                text.getStyleClass().add("label-muted");

                legendItems.add(new HBox(6, dot, text));
            }

            int half = (legendItems.size() + 1) / 2;
            for (int i = 0; i < legendItems.size(); i++) {
                if (i < half) {
                    leftBox.getChildren().add(legendItems.get(i));
                } else {
                    rightBox.getChildren().add(legendItems.get(i));
                }
            }

            pieChart.setData(pieData);

            // Style slices dynamically with our language colors
            javafx.application.Platform.runLater(() -> {
                for (int i = 0; i < pieData.size(); i++) {
                    PieChart.Data data = pieData.get(i);
                    Node slice = data.getNode();
                    if (slice != null) {
                        slice.getStyleClass().clear();
                        slice.getStyleClass().addAll("chart-pie", getLangColorClass(data.getName()));
                    }
                }
            });
        };

        rebuildLanguages.run();
        summary.getLanguages().addListener((ListChangeListener<LanguageInfo>) c -> rebuildLanguages.run());

        Runnable updateLayout = () -> {
            double w = card.getWidth();
            if (w == 0) w = 350;
            compositionContainer.getChildren().clear();
            if (w < 320) {
                VBox vbox = new VBox(12);
                vbox.setAlignment(Pos.CENTER);
                HBox legends = new HBox(20);
                legends.setAlignment(Pos.CENTER);
                legends.getChildren().setAll(leftBox, rightBox);
                vbox.getChildren().setAll(chartPane, legends);
                compositionContainer.getChildren().setAll(vbox);
            } else {
                HBox hbox = new HBox(12);
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().setAll(leftBox, chartPane, rightBox);
                compositionContainer.getChildren().setAll(hbox);
            }
        };

        card.widthProperty().addListener((obs, oldW, newW) -> {
            javafx.application.Platform.runLater(updateLayout);
        });
        javafx.application.Platform.runLater(updateLayout);

        card.getChildren().add(compositionContainer);
        return card;
    }

    private Node buildEntityComposition(AnalysisContext context) {
        VBox card = new VBox(10);
        card.getStyleClass().add("hub-card");

        Label title = new Label("ENTITY COMPOSITION");
        title.getStyleClass().add("hub-card-title");
        card.getChildren().add(title);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("composition-grid");

        int classCount = 0;
        int recordCount = 0;
        int functionCount = 0;
        int interfaceCount = 0;
        int enumCount = 0;
        int moduleCount = 0;
        int packageCount = context.getNamespaces().size();

        for (EntityInfo entity : context.getEntities()) {
            switch (entity.getIdentity().kind()) {
                case CLASS, STRUCT -> classCount++;
                case RECORD -> recordCount++;
                case FUNCTION -> functionCount++;
                case INTERFACE, TRAIT -> interfaceCount++;
                case ENUM -> enumCount++;
                case MODULE -> moduleCount++;
                default -> {}
            }
        }

        List<EntityCompositionItem> candidates = new ArrayList<>();
        candidates.add(new EntityCompositionItem("Classes", classCount, "🅲", "classes"));
        candidates.add(new EntityCompositionItem("Records", recordCount, "🅡", "records"));
        candidates.add(new EntityCompositionItem("Functions", functionCount, "🅵", "functions"));
        candidates.add(new EntityCompositionItem("Interfaces", interfaceCount, "🅸", "interfaces"));
        candidates.add(new EntityCompositionItem("Enums", enumCount, "🅴", "enums"));
        candidates.add(new EntityCompositionItem("Modules", moduleCount, "🅼", "modules"));
        candidates.add(new EntityCompositionItem("Packages", packageCount, "📁", "packages"));

        List<EntityCompositionItem> present = candidates.stream()
            .filter(item -> item.value > 0)
            .toList();

        List<Node> boxes = new ArrayList<>();
        for (EntityCompositionItem item : present) {
            boxes.add(createCompositionBox(item.name, item.value, item.icon, item.styleName));
        }

        Runnable updateGrid = () -> {
            double w = card.getWidth();
            if (w == 0) w = 350;
            grid.getChildren().clear();
            grid.getColumnConstraints().clear();

            if (w < 280) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(100);
                grid.getColumnConstraints().add(col);

                for (int i = 0; i < boxes.size(); i++) {
                    grid.add(boxes.get(i), 0, i);
                }
            } else {
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(50);
                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(50);
                grid.getColumnConstraints().addAll(col1, col2);

                for (int i = 0; i < boxes.size(); i++) {
                    grid.add(boxes.get(i), i % 2, i / 2);
                }
            }
        };

        card.widthProperty().addListener((obs, oldW, newW) -> {
            javafx.application.Platform.runLater(updateGrid);
        });
        javafx.application.Platform.runLater(updateGrid);

        card.getChildren().add(grid);
        return card;
    }

    private static class EntityCompositionItem {
        final String name;
        final int value;
        final String icon;
        final String styleName;

        EntityCompositionItem(String name, int value, String icon, String styleName) {
            this.name = name;
            this.value = value;
            this.icon = icon;
            this.styleName = styleName;
        }
    }

    private Node createCompositionBox(String name, int value, String icon, String styleName) {
        VBox box = new VBox(4);
        box.getStyleClass().addAll("composition-box", "composition-box-" + styleName);

        HBox top = new HBox(6);
        top.getStyleClass().add("composition-label-box");
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().addAll("composition-icon", "composition-icon-" + styleName);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("composition-name");
        top.getChildren().addAll(iconLabel, nameLabel);

        Label valLabel = new Label(String.valueOf(value));
        valLabel.getStyleClass().add("composition-value");

        box.getChildren().addAll(top, valLabel);
        return box;
    }

    private Node buildTechnologies(AnalysisContext context) {
        VBox card = new VBox(10);
        card.getStyleClass().add("hub-card");

        Label title = new Label("TECHNOLOGIES");
        title.getStyleClass().add("hub-card-title");
        card.getChildren().add(title);

        FlowPane pane = new FlowPane();
        pane.setHgap(8);
        pane.setVgap(8);

        Set<String> uniqueTechs = new LinkedHashSet<>();
        if (context.getClassificationResult() != null) {
            context.getClassificationResult().detectedFrameworks().forEach(fw -> {
                if (fw.isDetected()) {
                    uniqueTechs.add(fw.frameworkName());
                }
            });
            context.getClassificationResult().detectedTechnologies().forEach(tech -> {
                uniqueTechs.add(tech.displayName());
            });
        }

        if (uniqueTechs.isEmpty()) {
            Label empty = new Label("None detected");
            empty.getStyleClass().add("label-muted");
            pane.getChildren().add(empty);
        } else {
            for (String tech : uniqueTechs) {
                Label badge = new Label(tech);
                badge.getStyleClass().addAll("tech-badge", getTechColorClass(tech));
                pane.getChildren().add(badge);
            }
        }

        card.getChildren().add(pane);
        return card;
    }

    private Node buildEntityExplorer(AnalysisContext context) {
        VBox card = new VBox(10);
        card.getStyleClass().add("hub-card");

        Label title = new Label("ENTITY EXPLORER");
        title.getStyleClass().add("hub-card-title");
        card.getChildren().add(title);

        // Search Bar
        HBox searchBar = new HBox(8);
        TextField searchField = new TextField();
        searchField.setPromptText("Search entity...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button filterBtn = new Button("🎚");
        filterBtn.getStyleClass().add("filter-btn");
        searchBar.getChildren().addAll(searchField, filterBtn);

        // Filter Tabs
        HBox tabBox = new HBox(4);
        tabBox.getStyleClass().add("explorer-tab-bar");

        List<String> orderedCategories = List.of("Classes", "Records", "Functions", "Interfaces", "Enums", "Modules");
        List<String> categories = new ArrayList<>();
        categories.add("All");
        
        Set<String> presentCats = new HashSet<>();
        for (EntityInfo entity : context.getEntities()) {
            presentCats.add(getCategoryLabel(entity.getIdentity().kind()));
        }
        for (String cat : orderedCategories) {
            if (presentCats.contains(cat)) {
                categories.add(cat);
            }
        }
        for (String cat : presentCats) {
            if (!orderedCategories.contains(cat) && !"Other".equals(cat)) {
                categories.add(cat);
            }
        }

        List<Button> tabButtons = new ArrayList<>();
        final ObjectProperty<String> activeCategory = new SimpleObjectProperty<>("All");

        // TreeView
        TreeView<ExplorerNode> treeView = new TreeView<>();
        treeView.setShowRoot(false);
        treeView.getStyleClass().add("explorer-tree");

        // Search text change listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            treeView.setRoot(buildHierarchy(context.getEntities(), newVal, activeCategory.get()));
        });

        for (String cat : categories) {
            Button btn = new Button(cat);
            btn.getStyleClass().add("explorer-tab-btn");
            if (cat.equals("All")) {
                btn.getStyleClass().add("explorer-tab-btn-active");
            }
            btn.setOnAction(e -> {
                tabButtons.forEach(b -> b.getStyleClass().removeAll("explorer-tab-btn-active"));
                btn.getStyleClass().add("explorer-tab-btn-active");
                activeCategory.set(cat);
                treeView.setRoot(buildHierarchy(context.getEntities(), searchField.getText(), cat));
            });
            tabButtons.add(btn);
            tabBox.getChildren().add(btn);
        }

        // Cell Factory for TreeView
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(ExplorerNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }

                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                Label iconLabel = new Label();
                Label nameLabel = new Label(item.name);
                nameLabel.getStyleClass().add("tree-node-name");

                if (item.isNamespace) {
                    iconLabel.setText("📁");
                    iconLabel.setStyle("-fx-text-fill: #e0a96d;");
                    row.getChildren().addAll(iconLabel, nameLabel);
                } else {
                    String icon = "📄";
                    String kindBadgeText = item.entity.getIdentity().kind().name();
                    String kindClass = "badge-generic";

                    switch (item.entity.getIdentity().kind()) {
                        case CLASS, STRUCT -> {
                            icon = "🅲";
                            kindClass = "badge-class";
                        }
                        case RECORD -> {
                            icon = "🅡";
                            kindClass = "badge-record";
                        }
                        case INTERFACE -> {
                            icon = "🅸";
                            kindClass = "badge-interface";
                        }
                        case ENUM -> {
                            icon = "🅴";
                            kindClass = "badge-enum";
                        }
                        case FUNCTION -> {
                            icon = "🅵";
                            kindClass = "badge-function";
                        }
                        case MODULE -> {
                            icon = "🅼";
                            kindClass = "badge-module";
                        }
                        default -> {}
                    }

                    if (item.name.endsWith("Controller")) {
                        kindBadgeText = "REST";
                        kindClass = "badge-rest";
                    }

                    iconLabel.setText(icon);
                    iconLabel.getStyleClass().addAll("tree-node-icon", "composition-icon-" + kindClass.replace("badge-", ""));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label kindBadge = new Label(kindBadgeText);
                    kindBadge.getStyleClass().addAll("tree-kind-badge", kindClass);

                    int depCount = item.entity.getRelationships().dependsOn().size();
                    Label depLabel = new Label(depCount + " deps");
                    depLabel.getStyleClass().add("tree-dep-label");

                    row.getChildren().addAll(iconLabel, nameLabel, spacer, kindBadge, depLabel);
                }

                if (item.entity != null && item.entity.getDocumentation() != null && 
                    item.entity.getDocumentation().docstring() != null && 
                    !item.entity.getDocumentation().docstring().isEmpty()) {
                    Tooltip tooltip = new Tooltip(item.entity.getDocumentation().docstring());
                    tooltip.setStyle("-fx-background-color: #111819; -fx-text-fill: #dde4e5; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-border-color: #1e2526; -fx-border-width: 1; -fx-padding: 8;");
                    tooltip.setShowDelay(javafx.util.Duration.millis(300));
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(350);
                    setTooltip(tooltip);
                } else {
                    setTooltip(null);
                }

                setGraphic(row);
                setText(null);
            }
        });

        // Click to open source file inside editor tab
        treeView.setOnMouseClicked(event -> {
            TreeItem<ExplorerNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() != null && !selectedItem.getValue().isNamespace) {
                EntityInfo entity = selectedItem.getValue().entity;
                if (entity != null) {
                    uiFeatures.openAndHighlight(
                        getSimpleName(entity.getEntityName()),
                        entity.getBeginLine(),
                        entity.getBeginColumn(),
                        entity.getSourceFile()
                    );
                }
            }
        });

        // TreeView selection listener
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null && !newVal.getValue().isNamespace) {
                updateSelectedEntityView(newVal.getValue().entity);
            }
        });

        treeView.setRoot(buildHierarchy(context.getEntities(), "", "All"));

        card.getChildren().addAll(searchBar, tabBox, treeView);
        return card;
    }

    private Node buildSelectedEntity(AnalysisContext context) {
        VBox card = new VBox(12);
        card.getStyleClass().add("hub-card");

        Label title = new Label("SELECTED ENTITY");
        title.getStyleClass().add("hub-card-title");
        card.getChildren().add(title);

        // Header with Avatar and FQN
        HBox entityHeader = new HBox(12);
        entityHeader.getStyleClass().add("entity-header-box");

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar-circle");
        Label avatarLetter = new Label("?");
        avatarLetter.getStyleClass().add("avatar-letter");
        avatarLetter.textProperty().bind(selectedAvatarLetter);
        avatar.getChildren().add(avatarLetter);

        VBox titleBox = new VBox(2);
        HBox nameAndBadge = new HBox(6);
        nameAndBadge.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Select an entity...");
        nameLabel.getStyleClass().add("entity-title-name");
        nameLabel.textProperty().bind(selectedEntityName);
        Label badgeLabel = new Label();
        badgeLabel.getStyleClass().add("tree-kind-badge");
        badgeLabel.textProperty().bind(selectedEntityKind);

        selectedEntityKind.addListener((obs, oldVal, newVal) -> {
            badgeLabel.getStyleClass().removeAll("badge-class", "badge-rest", "badge-interface", "badge-enum", "badge-function", "badge-module");
            if (newVal.equalsIgnoreCase("CLASS")) {
                badgeLabel.getStyleClass().add("badge-class");
            } else if (newVal.equalsIgnoreCase("REST")) {
                badgeLabel.getStyleClass().add("badge-rest");
            } else if (newVal.equalsIgnoreCase("INTERFACE")) {
                badgeLabel.getStyleClass().add("badge-interface");
            } else if (newVal.equalsIgnoreCase("ENUM")) {
                badgeLabel.getStyleClass().add("badge-enum");
            } else if (newVal.equalsIgnoreCase("FUNCTION")) {
                badgeLabel.getStyleClass().add("badge-function");
            } else if (newVal.equalsIgnoreCase("MODULE")) {
                badgeLabel.getStyleClass().add("badge-module");
            }
        });
        nameAndBadge.getChildren().addAll(nameLabel, badgeLabel);

        Label fqnLabel = new Label("");
        fqnLabel.getStyleClass().add("entity-fqn");
        fqnLabel.textProperty().bind(selectedEntityFqn);
        titleBox.getChildren().addAll(nameAndBadge, fqnLabel);

        entityHeader.getChildren().addAll(avatar, titleBox);
        card.getChildren().add(entityHeader);

        // Metrics grid
        GridPane metrics = new GridPane();
        metrics.getStyleClass().add("metrics-grid");

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(33.3);
        metrics.getColumnConstraints().addAll(cc, cc, cc);

        metrics.add(createMiniMetric("Lines of Code", selectedLoc), 0, 0);
        metrics.add(createMiniMetric("Methods", selectedMethods), 1, 0);
        metrics.add(createMiniMetric("Fields", selectedFields), 2, 0);
        metrics.add(createMiniMetric("Dependencies", selectedDeps), 0, 1);
        metrics.add(createMiniMetric("Fan-In", selectedFanIn), 1, 1);
        metrics.add(createMiniMetric("Fan-Out", selectedFanOut), 2, 1);
        card.getChildren().add(metrics);

        return card;
    }

    private Node createMiniMetric(String name, StringProperty valueProperty) {
        VBox box = new VBox(4);
        box.getStyleClass().add("mini-metric-box");
        box.setAlignment(Pos.CENTER);

        Label val = new Label();
        val.getStyleClass().add("mini-metric-value");
        val.textProperty().bind(valueProperty);

        Label lbl = new Label(name);
        lbl.getStyleClass().add("mini-metric-title");

        box.getChildren().addAll(val, lbl);
        return box;
    }



    private void updateSelectedEntityView(EntityInfo entity) {
        currentSelectedEntity.set(entity);
        if (entity == null) {
            selectedEntityName.set("Select an entity...");
            selectedEntityFqn.set("");
            selectedEntityKind.set("");
            selectedAvatarLetter.set("?");
            selectedLoc.set("-");
            selectedMethods.set("-");
            selectedFields.set("-");
            selectedDeps.set("-");
            selectedFanIn.set("-");
            selectedFanOut.set("-");
        } else {
            selectedEntityName.set(getSimpleName(entity.getEntityName()));
            selectedEntityFqn.set(entity.getEntityName());

            String kind = entity.getIdentity().kind().name();
            if (getSimpleName(entity.getEntityName()).endsWith("Controller")) {
                kind = "REST";
            }
            selectedEntityKind.set(kind);

            String simpleName = getSimpleName(entity.getEntityName());
            selectedAvatarLetter.set(simpleName.isEmpty() ? "?" : simpleName.substring(0, 1).toUpperCase());

            selectedLoc.set(String.valueOf(entity.getLinesOfCode()));
            selectedMethods.set(String.valueOf(entity.getStructure().callableDeclarations().size()));
            selectedFields.set(String.valueOf(entity.getStructure().fieldDeclarations().size()));
            selectedDeps.set(String.valueOf(entity.getRelationships().dependsOn().size()));
            selectedFanIn.set(String.valueOf(entity.getRelationships().usedBy().size()));
            selectedFanOut.set(String.valueOf(entity.getRelationships().dependsOn().size()));
        }
    }

    private TreeItem<ExplorerNode> buildHierarchy(List<EntityInfo> entities, String searchQuery, String activeCategory) {
        TreeItem<ExplorerNode> rootItem = new TreeItem<>(new ExplorerNode("Root", "", null, true));

        List<EntityInfo> filtered = entities.stream()
            .filter(entity -> {
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    String query = searchQuery.toLowerCase();
                    return entity.getEntityName().toLowerCase().contains(query) ||
                           entity.getIdentity().simpleName().toLowerCase().contains(query);
                }
                return true;
            })
            .filter(entity -> {
                if (activeCategory == null || activeCategory.equals("All")) {
                    return true;
                }
                return getCategoryLabel(entity.getIdentity().kind()).equals(activeCategory);
            })
            .toList();

        for (EntityInfo entity : filtered) {
            String fqn = entity.getEntityName();
            String simpleName = getSimpleName(fqn);
            rootItem.getChildren().add(new TreeItem<>(new ExplorerNode(simpleName, fqn, entity, false)));
        }

        return rootItem;
    }

    private String getLangColorClass(String lang) {
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

    private String getTechColorClass(String name) {
        String[] classes = {"tech-fastapi", "tech-sqlalchemy", "tech-pydantic", "tech-jwt", "tech-alembic", "tech-pytest", "tech-generic-1", "tech-generic-2"};
        int idx = Math.abs(name.toLowerCase().hashCode()) % classes.length;
        return classes[idx];
    }

    private String getSimpleName(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    private String getCategoryLabel(com.example.anuviya.model.entity.EntityKind kind) {
        return switch (kind) {
            case CLASS, STRUCT -> "Classes";
            case RECORD -> "Records";
            case FUNCTION -> "Functions";
            case MODULE -> "Modules";
            case INTERFACE, TRAIT -> "Interfaces";
            case ENUM -> "Enums";
            default -> "Other";
        };
    }

    private static class ExplorerNode {
        final String name;
        final String fqn;
        final EntityInfo entity;
        final boolean isNamespace;

        ExplorerNode(String name, String fqn, EntityInfo entity, boolean isNamespace) {
            this.name = name;
            this.fqn = fqn;
            this.entity = entity;
            this.isNamespace = isNamespace;
        }
    }
}
