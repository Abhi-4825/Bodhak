package com.example.anuviya.ui.analysisReport.compiler;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.ir.*;
import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.ir.statement.*;
import com.example.anuviya.ir.expression.*;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import com.example.anuviya.ui.analysisReport.state.CompilerPipelineState;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CompilerExplorerDashboard extends ScrollPane {

    private final AnalysisReportState state;
    private AnalysisContext analysisContext = null;

    // Metrics bindings
    private final StringProperty totalUnitsMetric = new SimpleStringProperty("0");
    private final StringProperty totalLocMetric = new SimpleStringProperty("0");
    private final StringProperty totalFilesMetric = new SimpleStringProperty("0");
    private final StringProperty totalSymbolsMetric = new SimpleStringProperty("0");
    private final StringProperty healthIndexMetric = new SimpleStringProperty("100/100");

    // Subtitle properties for metric cards
    private final StringProperty compilationUnitsSub = new SimpleStringProperty("Active Units");
    private final StringProperty totalLocSub = new SimpleStringProperty("Lines of Code");
    private final StringProperty totalFilesSub = new SimpleStringProperty("Source Files");
    private final StringProperty totalSymbolsSub = new SimpleStringProperty("Resolved Entities");
    private final StringProperty healthIndexSub = new SimpleStringProperty("Analysis Status");

    // UI elements
    private final TreeView<Object> fileTreeView = new TreeView<>();
    private final CodeArea codeArea = new CodeArea();
    private final CodeArea irArea = new CodeArea();
    private final TreeView<IRNode> astTreeView = new TreeView<>();
    private final TableView<SymbolRow> symbolTableView = new TableView<>();
    private final Label astHeaderLabel = new Label("AST PREVIEW");

    // Quick Insights
    private final Label errorsLabel = new Label("0");
    private final Label warningsLabel = new Label("0");
    private final Label resolvedLabel = new Label("0");
    private final Label coverageLabel = new Label("0%");

    // Pipeline Timings Progress Bars
    private final ProgressBar parserProgress = new ProgressBar(0.0);
    private final ProgressBar analyzerProgress = new ProgressBar(0.0);
    private final ProgressBar optimizerProgress = new ProgressBar(0.0);
    private final ProgressBar codegenProgress = new ProgressBar(0.0);

    private final Label parserTimeLabel = new Label("Ready");
    private final Label analyzerTimeLabel = new Label("Ready");
    private final Label optimizerTimeLabel = new Label("Ready");
    private final Label codegenTimeLabel = new Label("Ready");

    public CompilerExplorerDashboard(AnalysisReportState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        setFitToWidth(true);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: #0d141a; -fx-background-color: #0d141a; -fx-border-color: transparent;");

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(24));
        mainLayout.setStyle("-fx-background-color: #0d141a;");
        setContent(mainLayout);

        buildDashboard(mainLayout);

        // Listen for context updates
        state.analysisContextProperty().addListener((obs, oldVal, context) -> {
            if (context != null) {
                analysisContext = context;
                loadWorkspaceData(context);
            }
        });

        if (state.analysisContextProperty().get() != null) {
            analysisContext = state.analysisContextProperty().get();
            loadWorkspaceData(analysisContext);
        }

        // Listen for pipeline state updates
        state.getCompilerPipelineState().getPhases().addListener((javafx.collections.ListChangeListener<CompilerPipelineState.PipelinePhase>) c -> updatePipelineUI());
        updatePipelineUI();
    }

    private void loadWorkspaceData(AnalysisContext context) {
        Platform.runLater(() -> {
            List<CompilationUnit> units = context.getCompilationUnits();
            totalUnitsMetric.set(String.valueOf(units.size()));
            compilationUnitsSub.set("Analyzed Compilation Units");

            long loc = context.getEntities().stream().mapToLong(e -> e.getMetrics() != null ? e.getMetrics().linesOfCode() : 0).sum();
            totalLocMetric.set(loc > 1000 ? String.format("%.1fK", loc / 1000.0) : String.valueOf(loc));
            totalLocSub.set("Source Lines of Code");

            totalFilesMetric.set(String.valueOf(units.size()));

            // Compute file breakdown by extension
            Map<String, Long> extCounts = units.stream()
                    .map(u -> u.getFilePath() != null ? u.getFilePath().getFileName().toString() : "")
                    .filter(name -> name.contains("."))
                    .map(name -> name.substring(name.lastIndexOf('.')).toLowerCase())
                    .collect(Collectors.groupingBy(ext -> ext, Collectors.counting()));

            long javaCount = extCounts.getOrDefault(".java", 0L);
            long pyCount = extCounts.getOrDefault(".py", 0L);
            long otherCount = units.size() - javaCount - pyCount;
            totalFilesSub.set(String.format("Java: %d • Py: %d • Other: %d", javaCount, pyCount, otherCount));

            int symbolsCount = context.getEntities().size() + (context.getSymbolTable() != null ? context.getSymbolTable().getAllSymbols().size() : 0);
            totalSymbolsMetric.set(symbolsCount > 1000 ? String.format("%.1fK", symbolsCount / 1000.0) : String.valueOf(symbolsCount));
            totalSymbolsSub.set(String.format("Classes: %d • Methods: %d",
                    context.getEntities().stream().filter(e -> e.getKind().name().equalsIgnoreCase("CLASS")).count(),
                    context.getEntities().stream().filter(e -> e.getKind().name().equalsIgnoreCase("FUNCTION") || e.getKind().name().equalsIgnoreCase("METHOD")).count()));

            // Compute health index based on diagnostics
            int diagnosticsCount = 0;
            int errorCount = 0;
            int warningCount = 0;
            for (CompilationUnit cu : units) {
                diagnosticsCount += cu.getDiagnostics().size();
                for (var d : cu.getDiagnostics()) {
                    if (d.severity() != null && d.severity().equalsIgnoreCase("ERROR")) {
                        errorCount++;
                    } else {
                        warningCount++;
                    }
                }
            }
            int health = Math.max(50, 100 - (errorCount * 5 + warningCount));
            healthIndexMetric.set(health + "/100");
            healthIndexSub.set(health >= 90 ? "Excellent" : health >= 75 ? "Good" : "Needs Attention");

            // Populate Quick Insights
            errorsLabel.setText(String.valueOf(errorCount));
            warningsLabel.setText(String.valueOf(warningCount));
            resolvedLabel.setText(String.valueOf(symbolsCount));
            coverageLabel.setText(units.isEmpty() ? "0%" : "100%");

            // Populate File Tree
            buildFileTree(units);
        });
    }

    private void updatePipelineUI() {
        var phases = state.getCompilerPipelineState().getPhases();
        if (phases == null || phases.isEmpty()) {
            parserProgress.setProgress(1.0);
            parserTimeLabel.setText("Pass 1: OK");
            analyzerProgress.setProgress(1.0);
            analyzerTimeLabel.setText("Pass 2: OK");
            optimizerProgress.setProgress(1.0);
            optimizerTimeLabel.setText("Pass 3: OK");
            codegenProgress.setProgress(1.0);
            codegenTimeLabel.setText("Pass 4: OK");
            return;
        }

        ProgressBar[] bars = {parserProgress, analyzerProgress, optimizerProgress, codegenProgress};
        Label[] labels = {parserTimeLabel, analyzerTimeLabel, optimizerTimeLabel, codegenTimeLabel};

        for (int i = 0; i < Math.min(phases.size(), 4); i++) {
            var phase = phases.get(i);
            boolean isDone = "OK".equalsIgnoreCase(phase.status()) || "DONE".equalsIgnoreCase(phase.status());
            bars[i].setProgress(isDone ? 1.0 : 0.5);
            labels[i].setText(phase.duration().equals("Placeholder") ? "OK" : phase.duration());
        }
    }

    private void buildDashboard(VBox layout) {
        // 1. Metrics Ribbon
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        metricsGrid.setVgap(16);
        for (int i = 0; i < 5; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 5.0);
            metricsGrid.getColumnConstraints().add(cc);
        }

        metricsGrid.add(createMetricCard("COMPILATION UNITS", totalUnitsMetric, compilationUnitsSub), 0, 0);
        metricsGrid.add(createMetricCard("TOTAL LOC", totalLocMetric, totalLocSub), 1, 0);
        metricsGrid.add(createMetricCard("TOTAL FILES", totalFilesMetric, totalFilesSub), 2, 0);
        metricsGrid.add(createMetricCard("SYMBOLS", totalSymbolsMetric, totalSymbolsSub), 3, 0);
        metricsGrid.add(createMetricCard("HEALTH INDEX", healthIndexMetric, healthIndexSub), 4, 0);

        // 2. Middle Panel (Compilation Units + Source Editor + IR View)
        HBox middlePanel = new HBox(16);
        middlePanel.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(middlePanel, Priority.ALWAYS);

        // Left sidebar: Tree of files
        VBox treeCard = new VBox(12);
        treeCard.setPrefWidth(280);
        treeCard.setMinWidth(280);
        treeCard.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 16;");
        Label treeHeader = new Label("COMPILATION UNITS");
        treeHeader.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 0.15em;");
        fileTreeView.setStyle("-fx-background-color: transparent; -fx-text-fill: #dce3ec;");
        fileTreeView.setShowRoot(false);
        fileTreeView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.getValue() instanceof CompilationUnit) {
                loadCompilationUnitDetails((CompilationUnit) newVal.getValue());
            }
        });
        VBox.setVgrow(fileTreeView, Priority.ALWAYS);
        treeCard.getChildren().addAll(treeHeader, fileTreeView);

        // Code Editor Panel
        VBox codeCard = new VBox(8);
        HBox.setHgrow(codeCard, Priority.ALWAYS);
        codeCard.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 12;");
        Label codeHeader = new Label("SOURCE PREVIEW");
        codeHeader.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold;");
        codeArea.setEditable(false);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setStyle("-fx-background-color: #080f15; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px;");
        ScrollPane editorScroll = new ScrollPane(codeArea);
        editorScroll.setFitToWidth(true);
        editorScroll.setFitToHeight(true);
        editorScroll.setStyle("-fx-background: #080f15; -fx-background-color: #080f15; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-radius: 6;");
        VBox.setVgrow(editorScroll, Priority.ALWAYS);
        codeCard.getChildren().addAll(codeHeader, editorScroll);

        // IR View Panel (using CodeArea for syntax highlighting)
        VBox irCard = new VBox(8);
        HBox.setHgrow(irCard, Priority.ALWAYS);
        irCard.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 12;");
        HBox irHeaderBox = new HBox(8);
        irHeaderBox.setAlignment(Pos.CENTER_LEFT);
        Label irHeader = new Label("IR STRUCTURE");
        irHeader.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label irSubHeader = new Label("(Language-Neutral Representation)");
        irSubHeader.setStyle("-fx-text-fill: #849396; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px;");
        irHeaderBox.getChildren().addAll(irHeader, irSubHeader);

        irArea.setEditable(false);
        irArea.setParagraphGraphicFactory(LineNumberFactory.get(irArea));
        irArea.setStyle("-fx-background-color: #080f15; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px;");
        ScrollPane irScroll = new ScrollPane(irArea);
        irScroll.setFitToWidth(true);
        irScroll.setFitToHeight(true);
        irScroll.setStyle("-fx-background: #080f15; -fx-background-color: #080f15; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-radius: 6;");
        VBox.setVgrow(irScroll, Priority.ALWAYS);
        irCard.getChildren().addAll(irHeaderBox, irScroll);

        // Wrap editor and IR side-by-side in a split pane
        SplitPane workspaceSplit = new SplitPane(codeCard, irCard);
        workspaceSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        workspaceSplit.setDividerPositions(0.5);
        HBox.setHgrow(workspaceSplit, Priority.ALWAYS);
        middlePanel.getChildren().addAll(treeCard, workspaceSplit);

        // 3. Bento Bottom Grid (AST Preview + Timing Pipeline + Symbol Table)
        GridPane bottomGrid = new GridPane();
        bottomGrid.setHgap(16);
        bottomGrid.setVgap(16);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30.0);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70.0);
        bottomGrid.getColumnConstraints().addAll(col1, col2);

        // AST Card
        VBox astCard = new VBox(12);
        astCard.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 16;");
        astHeaderLabel.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        astTreeView.setStyle("-fx-background-color: transparent; -fx-text-fill: #dce3ec;");
        astTreeView.setShowRoot(true);
        setupAstTreeCellFactory();
        VBox.setVgrow(astTreeView, Priority.ALWAYS);
        astCard.getChildren().addAll(astHeaderLabel, astTreeView);
        bottomGrid.add(astCard, 0, 0);

        // Pipeline + Symbols side card
        VBox rightCardContainer = new VBox(16);
        
        // Pipeline Row
        VBox pipelineBox = new VBox(12);
        pipelineBox.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 16;");
        Label pipelineHeader = new Label("COMPILER PIPELINE PERFORMANCE");
        pipelineHeader.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        HBox progressRow = new HBox(12);
        progressRow.getChildren().addAll(
            createPipelineBar("PARSER", parserProgress, parserTimeLabel, "#00daf3"),
            createPipelineBar("SEMANTIC ANALYSIS", analyzerProgress, analyzerTimeLabel, "#fec931"),
            createPipelineBar("IR GENERATION", optimizerProgress, optimizerTimeLabel, "#00e5ff"),
            createPipelineBar("METRICS & RELATIONS", codegenProgress, codegenTimeLabel, "#ff4b4b")
        );
        pipelineBox.getChildren().addAll(pipelineHeader, progressRow);

        // Symbols Table
        VBox symbolsBox = new VBox(12);
        symbolsBox.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 16;");
        Label symbolsHeader = new Label("SYMBOL TABLE & REFERENCES");
        symbolsHeader.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        buildSymbolTable();
        VBox.setVgrow(symbolTableView, Priority.ALWAYS);
        symbolsBox.getChildren().addAll(symbolsHeader, symbolTableView);

        rightCardContainer.getChildren().addAll(pipelineBox, symbolsBox);
        bottomGrid.add(rightCardContainer, 1, 0);

        layout.getChildren().addAll(metricsGrid, middlePanel, bottomGrid);
    }

    private VBox createMetricCard(String title, StringProperty valueProp, StringProperty subProp) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 12;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; -fx-text-fill: #849396;");

        Label lblValue = new Label();
        lblValue.textProperty().bind(valueProp);
        lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00daf3;");

        Label lblSub = new Label();
        lblSub.textProperty().bind(subProp);
        lblSub.setStyle("-fx-text-fill: #607274; -fx-font-size: 8px;");

        card.getChildren().addAll(lblTitle, lblValue, lblSub);
        return card;
    }

    private VBox createPipelineBar(String stage, ProgressBar bar, Label timeLbl, String colorHex) {
        VBox box = new VBox(4);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setStyle("-fx-background-color: #12181f; -fx-padding: 10; -fx-background-radius: 6;");

        Label lblStage = new Label(stage);
        lblStage.setStyle("-fx-text-fill: #849396; -fx-font-size: 9px; -fx-font-family: 'JetBrains Mono';");

        timeLbl.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Epilogue';");
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setProgress(1.0);
        bar.setStyle("-fx-accent: " + colorHex + "; -fx-control-inner-background: rgba(0,0,0,0.2);");

        box.getChildren().addAll(lblStage, timeLbl, bar);
        return box;
    }

    private void buildSymbolTable() {
        TableColumn<SymbolRow, String> nameCol = new TableColumn<>("Symbol Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-text-fill: #00daf3;");

        TableColumn<SymbolRow, String> typeCol = new TableColumn<>("Kind");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<SymbolRow, String> fqnCol = new TableColumn<>("Fully Qualified Name");
        fqnCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<SymbolRow, String> locCol = new TableColumn<>("LOC");
        locCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<SymbolRow, Integer> complexityCol = new TableColumn<>("Cyclomatic");
        complexityCol.setCellValueFactory(new PropertyValueFactory<>("refs"));

        TableColumn<SymbolRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("flags"));

        symbolTableView.getColumns().addAll(nameCol, typeCol, fqnCol, locCol, complexityCol, statusCol);
        symbolTableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #12181f;");
        symbolTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void buildFileTree(List<CompilationUnit> units) {
        TreeItem<Object> root = new TreeItem<>("Workspace Root");
        Map<String, TreeItem<Object>> dirMap = new HashMap<>();

        // Set cell factory BEFORE assigning root
        fileTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof CompilationUnit) {
                    Path p = ((CompilationUnit) item).getFilePath();
                    String fileName = p != null ? p.getFileName().toString() : "Unit";
                    String icon = fileName.endsWith(".java") ? "☕ " : fileName.endsWith(".py") ? "🐍 " : "📄 ";
                    setText(icon + fileName);
                } else {
                    setText("📁 " + item.toString());
                }
            }
        });

        TreeItem<Object> firstSelectableLeaf = null;

        for (CompilationUnit cu : units) {
            Path path = cu.getFilePath();
            if (path == null) continue;

            // Compute relative directory structure
            Path parent = path.getParent();
            TreeItem<Object> parentNode = root;
            if (parent != null) {
                String[] parts = parent.toString().replace('\\', '/').split("/");
                StringBuilder currPath = new StringBuilder();
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    if (currPath.length() > 0) currPath.append("/");
                    currPath.append(part);
                    String key = currPath.toString();
                    
                    parentNode = dirMap.computeIfAbsent(key, k -> {
                        TreeItem<Object> folderItem = new TreeItem<>(part);
                        folderItem.setExpanded(true);
                        return folderItem;
                    });
                }
                
                // Attach orphan folders to root if not attached
                if (parentNode.getParent() == null && parentNode != root) {
                    root.getChildren().add(parentNode);
                }
            }

            TreeItem<Object> leaf = new TreeItem<>(cu);
            parentNode.getChildren().add(leaf);
            if (firstSelectableLeaf == null) {
                firstSelectableLeaf = leaf;
            }
        }

        fileTreeView.setRoot(root);
        
        // Auto-select first compilation unit
        if (firstSelectableLeaf != null) {
            fileTreeView.getSelectionModel().select(firstSelectableLeaf);
            loadCompilationUnitDetails((CompilationUnit) firstSelectableLeaf.getValue());
        }
    }

    private void loadCompilationUnitDetails(CompilationUnit cu) {
        Path path = cu.getFilePath();
        if (path == null) return;

        Platform.runLater(() -> {
            // Source preview
            try {
                String code = Files.readString(path);
                codeArea.replaceText(code);
                codeArea.setStyleSpans(0, UniversalSyntaxHighlighter.computeHighlighting(code, path.getFileName().toString()));
            } catch (Exception e) {
                codeArea.replaceText("// Failed to read source file:\n// " + path.toAbsolutePath());
            }

            // Rich IR Structure
            IRNode irNode = cu.getIntermediateRepresentation();
            if (irNode != null) {
                String irText = printRichIR(irNode);
                irArea.replaceText(irText);
                irArea.setStyleSpans(0, UniversalSyntaxHighlighter.computeHighlighting(irText, "ir.java"));

                // AST Tree View
                int[] count = new int[]{0};
                TreeItem<IRNode> astRoot = buildTreeView(irNode, 0, count);
                astRoot.setExpanded(true);
                astTreeView.setRoot(astRoot);
                astHeaderLabel.setText(String.format("AST PREVIEW (%d Nodes)", count[0]));
            } else {
                irArea.replaceText("// No IR representation available for this unit.");
                astTreeView.setRoot(null);
                astHeaderLabel.setText("AST PREVIEW (0 Nodes)");
            }

            // Symbol table list
            ObservableList<SymbolRow> symbols = FXCollections.observableArrayList();
            for (EntityInfo entity : cu.getEntities()) {
                symbols.add(new SymbolRow(
                        entity.getSimpleName(),
                        entity.getKind().name(),
                        entity.getEntityName(),
                        entity.getMetrics() != null ? String.valueOf(entity.getMetrics().linesOfCode()) : "0",
                        entity.getMetrics() != null ? entity.getMetrics().cyclomaticComplexity() : 1,
                        "RESOLVED"
                ));
            }
            symbolTableView.setItems(symbols);
        });
    }

    // ── AST TreeView Cell Renderer ──────────────────────────────────────────

    private void setupAstTreeCellFactory() {
        astTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(IRNode node, boolean empty) {
                super.updateItem(node, empty);
                if (empty || node == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    String formattedText = getAstNodeSummary(node);
                    setText(formattedText);
                    
                    // Style by category
                    if (node instanceof DeclarationNode) {
                        setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
                    } else if (node instanceof StatementNode) {
                        setStyle("-fx-text-fill: #fec931; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
                    } else if (node instanceof ExpressionNode) {
                        setStyle("-fx-text-fill: #56d69b; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
                    } else {
                        setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
                    }
                }
            }
        });
    }

    private String getAstNodeSummary(IRNode node) {
        if (node instanceof ModuleDeclaration mod) {
            return String.format("🅓 Module '%s' [%s]", mod.name(), mod.language());
        } else if (node instanceof TypeDeclaration type) {
            return String.format("🅓 %s %s", type.typeKind(), type.name());
        } else if (node instanceof CallableDeclaration callable) {
            String ret = callable.returnType() != null ? callable.returnType().qualifiedName().toString() : "void";
            return String.format("🅓 define %s %s()", ret, callable.name());
        } else if (node instanceof VariableDeclaration var) {
            String typeStr = var.type() != null ? var.type().qualifiedName().toString() : "var";
            return String.format("🅓 %s %s : %s", var.kind(), var.name(), typeStr);
        } else if (node instanceof ImportDeclaration imp) {
            return String.format("🅸 import %s", imp.path());
        } else if (node instanceof BlockStatement block) {
            return String.format("🇸 { block } (%d stmts)", block.statements().size());
        } else if (node instanceof IfStatement) {
            return "🇸 if (...)";
        } else if (node instanceof LoopStatement) {
            return "🇸 loop (...)";
        } else if (node instanceof ReturnStatement) {
            return "🇸 return";
        } else if (node instanceof ExpressionStatement) {
            return "🇸 expr statement";
        } else if (node instanceof CallExpression call) {
            return String.format("🅔 call %s()", call.callableName());
        } else if (node instanceof AssignmentExpression) {
            return "🅔 assign (=)";
        } else if (node instanceof BinaryExpression bin) {
            return String.format("🅔 binary (%s)", bin.operator());
        } else if (node instanceof LiteralExpression lit) {
            return String.format("🅔 literal (%s)", lit.value());
        } else if (node instanceof VariableReferenceExpression ref) {
            return String.format("🅔 ref '%s'", ref.variableName());
        } else if (node instanceof TypeReferenceExpression tref) {
            return String.format("🅔 typeRef '%s'", tref.qualifiedName().toString());
        }
        return "🅝 " + node.getClass().getSimpleName();
    }

    private TreeItem<IRNode> buildTreeView(IRNode node, int depth, int[] count) {
        count[0]++;
        TreeItem<IRNode> item = new TreeItem<>(node);
        // Expand up to depth 2 (root + top-level declarations) to keep tree clean
        item.setExpanded(depth <= 2);

        if (depth > 25) {
            return item;
        }

        for (IRNode child : ChildrenCollector.collect(node)) {
            if (child != null && child != node) {
                item.getChildren().add(buildTreeView(child, depth + 1, count));
            }
        }

        return item;
    }

    // ── Rich IR Printer ──────────────────────────────────────────────────────

    private String printRichIR(IRNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("// ==========================================================\n");
        sb.append("// Anuviya IR (Intermediate Representation) - Structure View\n");
        sb.append("// Language Neutral Abstract Syntax Tree & Symbol Graph\n");
        sb.append("// ==========================================================\n\n");
        
        IrPrinterVisitor visitor = new IrPrinterVisitor();
        node.accept(visitor);
        sb.append(visitor.getOutput());
        return sb.toString();
    }

    private static class IrPrinterVisitor implements IRVisitor {
        private final StringBuilder sb = new StringBuilder();
        private int indent = 0;

        public String getOutput() {
            return sb.toString();
        }

        private void writeIndent() {
            sb.append("  ".repeat(indent));
        }

        @Override
        public void visit(ModuleDeclaration node) {
            writeIndent();
            sb.append("; Module: ").append(node.name()).append("  [language=").append(node.language()).append("]\n");
            if (node.filePath() != null) {
                writeIndent();
                sb.append("; Source: ").append(node.filePath()).append("\n");
            }
            sb.append("\n");

            if (node.imports() != null && !node.imports().isEmpty()) {
                writeIndent();
                sb.append("// Imports\n");
                node.imports().forEach(imp -> imp.accept(this));
                sb.append("\n");
            }

            if (node.declarations() != null) {
                node.declarations().forEach(d -> d.accept(this));
            }

            if (node.moduleStatements() != null && !node.moduleStatements().isEmpty()) {
                writeIndent();
                sb.append("// Module Body Statements\n");
                node.moduleStatements().forEach(s -> s.accept(this));
            }
        }

        @Override
        public void visit(ImportDeclaration node) {
            writeIndent();
            sb.append("import ").append(node.path());
            if (node.isStatic()) sb.append(" [static]");
            sb.append(";\n");
        }

        @Override
        public void visit(TypeDeclaration node) {
            if (node.documentation() != null && !node.documentation().isEmpty()) {
                writeIndent();
                sb.append("/** ").append(node.documentation().trim()).append(" */\n");
            }
            writeIndent();
            if (node.modifiers() != null && !node.modifiers().isEmpty()) {
                sb.append(node.modifiers().stream().map(m -> m.name().toLowerCase()).collect(Collectors.joining(" "))).append(" ");
            }
            sb.append(node.typeKind().name().toLowerCase()).append(" ").append(node.name());

            if (node.extendsTypes() != null && !node.extendsTypes().isEmpty()) {
                sb.append(" extends ").append(node.extendsTypes().stream().map(t -> t.qualifiedName().toString()).collect(Collectors.joining(", ")));
            }
            if (node.implementsTypes() != null && !node.implementsTypes().isEmpty()) {
                sb.append(" implements ").append(node.implementsTypes().stream().map(t -> t.qualifiedName().toString()).collect(Collectors.joining(", ")));
            }
            sb.append(" {\n");

            indent++;
            if (node.members() != null) {
                node.members().forEach(m -> m.accept(this));
            }
            indent--;

            writeIndent();
            sb.append("}\n\n");
        }

        @Override
        public void visit(CallableDeclaration node) {
            if (node.documentation() != null && !node.documentation().isEmpty()) {
                writeIndent();
                sb.append("/** ").append(node.documentation().trim()).append(" */\n");
            }
            writeIndent();
            sb.append("define ");
            if (node.modifiers() != null && !node.modifiers().isEmpty()) {
                sb.append(node.modifiers().stream().map(m -> m.name().toLowerCase()).collect(Collectors.joining(" "))).append(" ");
            }
            String retType = node.returnType() != null ? node.returnType().qualifiedName().toString() : (node.isConstructor() ? "" : "void");
            if (!retType.isEmpty()) sb.append(retType).append(" ");
            
            sb.append(node.name()).append("(");
            if (node.parameters() != null) {
                sb.append(node.parameters().stream().map(p -> (p.type() != null ? p.type().qualifiedName().toString() : "var") + " " + p.name()).collect(Collectors.joining(", ")));
            }
            sb.append(")");

            if (node.throwsTypes() != null && !node.throwsTypes().isEmpty()) {
                sb.append(" throws ").append(node.throwsTypes().stream().map(t -> t.qualifiedName().toString()).collect(Collectors.joining(", ")));
            }

            if (node.body() != null) {
                sb.append(" {\n");
                indent++;
                node.body().accept(this);
                indent--;
                writeIndent();
                sb.append("}\n");
            } else {
                sb.append(";\n");
            }
        }

        @Override
        public void visit(VariableDeclaration node) {
            writeIndent();
            String kindStr = node.kind() != null ? node.kind().name().toLowerCase() : "var";
            sb.append(kindStr).append(" ");
            if (node.type() != null) {
                sb.append(node.type().qualifiedName().toString()).append(" ");
            }
            sb.append(node.name());
            if (node.initializer() != null) {
                sb.append(" = ");
                node.initializer().accept(this);
            }
            sb.append(";\n");
        }

        @Override
        public void visit(BlockStatement node) {
            if (node.statements() != null) {
                node.statements().forEach(s -> s.accept(this));
            }
        }

        @Override
        public void visit(IfStatement node) {
            writeIndent();
            sb.append("if (");
            if (node.condition() != null) node.condition().accept(this);
            sb.append(") {\n");
            indent++;
            if (node.thenBranch() != null) node.thenBranch().accept(this);
            indent--;
            if (node.elseBranch() != null) {
                writeIndent();
                sb.append("} else {\n");
                indent++;
                node.elseBranch().accept(this);
                indent--;
            }
            writeIndent();
            sb.append("}\n");
        }

        @Override
        public void visit(LoopStatement node) {
            writeIndent();
            sb.append("loop (");
            if (node.condition() != null) node.condition().accept(this);
            sb.append(") {\n");
            indent++;
            if (node.body() != null) node.body().accept(this);
            indent--;
            writeIndent();
            sb.append("}\n");
        }

        @Override
        public void visit(ReturnStatement node) {
            writeIndent();
            sb.append("return ");
            if (node.expression() != null) node.expression().accept(this);
            sb.append(";\n");
        }

        @Override
        public void visit(ExpressionStatement node) {
            writeIndent();
            if (node.expression() != null) node.expression().accept(this);
            sb.append(";\n");
        }

        @Override
        public void visit(CallExpression node) {
            if (node.receiver() != null) {
                node.receiver().accept(this);
                sb.append(".");
            }
            sb.append(node.callableName()).append("(");
            if (node.arguments() != null) {
                for (int i = 0; i < node.arguments().size(); i++) {
                    if (i > 0) sb.append(", ");
                    node.arguments().get(i).accept(this);
                }
            }
            sb.append(")");
        }

        @Override
        public void visit(AssignmentExpression node) {
            if (node.target() != null) node.target().accept(this);
            sb.append(" = ");
            if (node.value() != null) node.value().accept(this);
        }

        @Override
        public void visit(BinaryExpression node) {
            if (node.left() != null) node.left().accept(this);
            sb.append(" ").append(node.operator()).append(" ");
            if (node.right() != null) node.right().accept(this);
        }

        @Override
        public void visit(LiteralExpression node) {
            if (node.value() instanceof String) {
                sb.append("\"").append(node.value()).append("\"");
            } else {
                sb.append(node.value());
            }
        }

        @Override
        public void visit(VariableReferenceExpression node) {
            sb.append(node.variableName());
        }

        @Override
        public void visit(TypeReferenceExpression node) {
            if (node.qualifiedName() != null) sb.append(node.qualifiedName().toString());
        }
    }

    // Helper Static Row classes for JavaFX TableView mapping
    public static class SymbolRow {
        private final String name;
        private final String type;
        private final String address;
        private final String size;
        private final int refs;
        private final String flags;

        public SymbolRow(String name, String type, String address, String size, int refs, String flags) {
            this.name = name;
            this.type = type;
            this.address = address;
            this.size = size;
            this.refs = refs;
            this.flags = flags;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getAddress() { return address; }
        public String getSize() { return size; }
        public int getRefs() { return refs; }
        public String getFlags() { return flags; }
    }

    private static class ChildrenCollector implements IRVisitor {
        private final List<IRNode> children = new ArrayList<>();

        public static List<IRNode> collect(IRNode node) {
            ChildrenCollector collector = new ChildrenCollector();
            node.accept(collector);
            return collector.children;
        }

        @Override public void visit(ModuleDeclaration node) { children.addAll(node.declarations()); if (node.moduleStatements() != null) children.addAll(node.moduleStatements()); }
        @Override public void visit(TypeDeclaration node) { children.addAll(node.decorators()); children.addAll(node.members()); }
        @Override public void visit(CallableDeclaration node) { children.addAll(node.decorators()); children.addAll(node.parameters()); if (node.body() != null) children.add(node.body()); }
        @Override public void visit(VariableDeclaration node) { children.addAll(node.decorators()); if (node.initializer() != null) children.add(node.initializer()); }
        @Override public void visit(DecoratorNode node) { children.addAll(node.arguments()); }
        @Override public void visit(BlockStatement node) { children.addAll(node.statements()); }
        @Override public void visit(IfStatement node) { if (node.condition() != null) children.add(node.condition()); if (node.thenBranch() != null) children.add(node.thenBranch()); if (node.elseBranch() != null) children.add(node.elseBranch()); }
        @Override public void visit(LoopStatement node) { if (node.condition() != null) children.add(node.condition()); if (node.body() != null) children.add(node.body()); }
        @Override public void visit(TryStatement node) { if (node.tryBlock() != null) children.add(node.tryBlock()); children.addAll(node.catchBlocks()); if (node.finallyBlock() != null) children.add(node.finallyBlock()); }
        @Override public void visit(ReturnStatement node) { if (node.expression() != null) children.add(node.expression()); }
        @Override public void visit(ExpressionStatement node) { if (node.expression() != null) children.add(node.expression()); }
        @Override public void visit(CallExpression node) { if (node.receiver() != null) children.add(node.receiver()); children.addAll(node.arguments()); }
        @Override public void visit(ObjectCreationExpression node) { if (node.type() != null) children.add(node.type()); children.addAll(node.arguments()); }
        @Override public void visit(AssignmentExpression node) { if (node.target() != null) children.add(node.target()); if (node.value() != null) children.add(node.value()); }
        @Override public void visit(FieldAccessExpression node) { if (node.receiver() != null) children.add(node.receiver()); }
        @Override public void visit(CastExpression node) { if (node.targetType() != null) children.add(node.targetType()); if (node.expression() != null) children.add(node.expression()); }
        @Override public void visit(BinaryExpression node) { if (node.left() != null) children.add(node.left()); if (node.right() != null) children.add(node.right()); }
        @Override public void visit(UnaryExpression node) { if (node.expression() != null) children.add(node.expression()); }
        @Override public void visit(ConditionalExpression node) { if (node.condition() != null) children.add(node.condition()); if (node.thenExpr() != null) children.add(node.thenExpr()); if (node.elseExpr() != null) children.add(node.elseExpr()); }
        @Override public void visit(ArrayAccessExpression node) { if (node.array() != null) children.add(node.array()); if (node.index() != null) children.add(node.index()); }
        @Override public void visit(ArrayCreationExpression node) { if (node.type() != null) children.add(node.type()); children.addAll(node.dimensions()); if (node.initializer() != null) children.add(node.initializer()); }
        @Override public void visit(LambdaExpression node) { children.addAll(node.parameters()); if (node.body() != null) children.add(node.body()); }
        @Override public void visit(AnnotationExpression node) { children.addAll(node.arguments()); }
        @Override public void visit(LocalVariableStatement node) { if (node.declaration() != null) children.add(node.declaration()); }
        @Override public void visit(ThrowStatement node) { if (node.expression() != null) children.add(node.expression()); }
        @Override public void visit(MatchStatement node) { if (node.subject() != null) children.add(node.subject()); if (node.cases() != null) node.cases().forEach(c -> { if (c.pattern() != null) children.add(c.pattern()); if (c.body() != null) children.add(c.body()); }); }
        @Override public void visit(ResourceStatement node) { if (node.resources() != null) children.addAll(node.resources()); if (node.body() != null) children.add(node.body()); }
        @Override public void visit(AwaitExpression node) { if (node.expression() != null) children.add(node.expression()); }
        @Override public void visit(YieldExpression node) { if (node.expression() != null) children.add(node.expression()); }
        @Override public void visit(ComprehensionExpression node) { if (node.element() != null) children.add(node.element()); if (node.key() != null) children.add(node.key()); if (node.generators() != null) node.generators().forEach(g -> { if (g.variable() != null) children.add(g.variable()); if (g.iterable() != null) children.add(g.iterable()); if (g.conditions() != null) children.addAll(g.conditions()); }); }
        @Override public void visit(CollectionLiteralExpression node) { if (node.elements() != null) children.addAll(node.elements()); }
        @Override public void visit(DictionaryExpression node) { if (node.entries() != null) node.entries().forEach(e -> { if (e.key() != null) children.add(e.key()); if (e.value() != null) children.add(e.value()); }); }
    }
}
