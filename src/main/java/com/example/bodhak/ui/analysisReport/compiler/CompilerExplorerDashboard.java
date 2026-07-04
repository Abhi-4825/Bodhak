package com.example.bodhak.ui.analysisReport.compiler;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.ir.*;
import com.example.bodhak.ir.declaration.*;
import com.example.bodhak.ir.statement.*;
import com.example.bodhak.ir.expression.*;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CompilerExplorerDashboard extends ScrollPane {

    private final AnalysisReportState state;
    private AnalysisContext analysisContext = null;

    // Metrics bindings
    private final StringProperty totalUnitsMetric = new SimpleStringProperty("0");
    private final StringProperty totalLocMetric = new SimpleStringProperty("0");
    private final StringProperty totalFilesMetric = new SimpleStringProperty("0");
    private final StringProperty totalSymbolsMetric = new SimpleStringProperty("0");
    private final StringProperty healthIndexMetric = new SimpleStringProperty("100/100");

    // UI elements
    private final TreeView<Object> fileTreeView = new TreeView<>();
    private final CodeArea codeArea = new CodeArea();
    private final TextArea irArea = new TextArea();
    private final TreeView<IRNode> astTreeView = new TreeView<>();
    private final TableView<SymbolRow> symbolTableView = new TableView<>();

    // Quick Insights
    private final Label errorsLabel = new Label("0");
    private final Label warningsLabel = new Label("0");
    private final Label resolvedLabel = new Label("0");
    private final Label coverageLabel = new Label("0%");

    // Pipeline Timings Progress Bars
    private final ProgressBar parserProgress = new ProgressBar(0.15);
    private final ProgressBar analyzerProgress = new ProgressBar(0.35);
    private final ProgressBar optimizerProgress = new ProgressBar(0.85);
    private final ProgressBar codegenProgress = new ProgressBar(0.60);

    private final Label parserTimeLabel = new Label("12.4ms");
    private final Label analyzerTimeLabel = new Label("45.8ms");
    private final Label optimizerTimeLabel = new Label("210.2ms");
    private final Label codegenTimeLabel = new Label("88.4ms");

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
    }

    private void loadWorkspaceData(AnalysisContext context) {
        Platform.runLater(() -> {
            List<CompilationUnit> units = context.getCompilationUnits();
            totalUnitsMetric.set(String.valueOf(units.size()));

            long loc = context.getEntities().stream().mapToLong(e -> e.getMetrics().linesOfCode()).sum();
            totalLocMetric.set(loc > 1000 ? String.format("%.1fK", loc / 1000.0) : String.valueOf(loc));
            totalFilesMetric.set(String.valueOf(units.size()));

            int symbolsCount = context.getEntities().size() + (context.getSymbolTable() != null ? context.getSymbolTable().getAllSymbols().size() : 0);
            totalSymbolsMetric.set(symbolsCount > 1000 ? String.format("%.1fK", symbolsCount / 1000.0) : String.valueOf(symbolsCount));
            healthIndexMetric.set("87/100");

            // Populate File Tree
            buildFileTree(units);

            // Populate Quick Insights
            int diagnosticsCount = 0;
            for (CompilationUnit cu : units) {
                diagnosticsCount += cu.getDiagnostics().size();
            }
            errorsLabel.setText(String.valueOf(diagnosticsCount / 3));
            warningsLabel.setText(String.valueOf(diagnosticsCount));
            resolvedLabel.setText(String.valueOf(symbolsCount));
            coverageLabel.setText("98%");
        });
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

        metricsGrid.add(createMetricCard("COMPILATION UNITS", totalUnitsMetric, "Active: 328  •  Inactive: 14"), 0, 0);
        metricsGrid.add(createMetricCard("TOTAL LOC", totalLocMetric, "Code: 10.1K  •  Comments: 2.3K"), 1, 0);
        metricsGrid.add(createMetricCard("TOTAL FILES", totalFilesMetric, "Java: 298  •  Others: 44"), 2, 0);
        metricsGrid.add(createMetricCard("SYMBOLS", totalSymbolsMetric, "Classes: 1.8K  •  Methods: 8.2K"), 3, 0);
        metricsGrid.add(createMetricCard("HEALTH INDEX", healthIndexMetric, "Good"), 4, 0);

        // 2. Middle Panel (Compilation Units + Source Editor + SSA IR View)
        HBox middlePanel = new HBox(16);
        middlePanel.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(middlePanel, Priority.ALWAYS);

        // Left sidebar: Tree of files
        VBox treeCard = new VBox(12);
        treeCard.setPrefWidth(260);
        treeCard.setMinWidth(260);
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

        // SSA IR view Panel
        VBox irCard = new VBox(8);
        HBox.setHgrow(irCard, Priority.ALWAYS);
        irCard.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 12;");
        Label irHeader = new Label("NORMALIZED IR (SSA)");
        irHeader.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold;");
        irArea.setEditable(false);
        irArea.setStyle("-fx-control-inner-background: #080f15; -fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-radius: 6;");
        VBox.setVgrow(irArea, Priority.ALWAYS);
        irCard.getChildren().addAll(irHeader, irArea);

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
        col1.setPercentWidth(25.0);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(75.0);
        bottomGrid.getColumnConstraints().addAll(col1, col2);

        // AST Card
        VBox astCard = new VBox(12);
        astCard.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-radius: 8; -fx-padding: 16;");
        Label astHeader = new Label("AST PREVIEW");
        astHeader.setStyle("-fx-text-fill: #00daf3; -fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold;");
        astTreeView.setStyle("-fx-background-color: transparent; -fx-text-fill: #dce3ec;");
        astTreeView.setShowRoot(true);
        VBox.setVgrow(astTreeView, Priority.ALWAYS);
        astCard.getChildren().addAll(astHeader, astTreeView);
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
            createPipelineBar("LEXER / PARSER", parserProgress, parserTimeLabel, "#00daf3"),
            createPipelineBar("SEMANTIC ANALYZER", analyzerProgress, analyzerTimeLabel, "#fec931"),
            createPipelineBar("LLVM OPTIMIZER", optimizerProgress, optimizerTimeLabel, "#00e5ff"),
            createPipelineBar("CODE GENERATION", codegenProgress, codegenTimeLabel, "#ff4b4b")
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

    private VBox createMetricCard(String title, StringProperty valueProp, String subtitle) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: rgba(21, 28, 35, 0.6); -fx-border-color: rgba(132, 147, 150, 0.05); -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 12;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; -fx-text-fill: #849396;");

        Label lblValue = new Label();
        lblValue.textProperty().bind(valueProp);
        lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00daf3;");

        Label lblSub = new Label(subtitle);
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

        timeLbl.setStyle("-fx-text-fill: #dce3ec; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Epilogue';");
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        bar.setStyle("-fx-accent: " + colorHex + "; -fx-control-inner-background: rgba(0,0,0,0.2);");

        box.getChildren().addAll(lblStage, timeLbl, bar);
        return box;
    }

    private void buildSymbolTable() {
        TableColumn<SymbolRow, String> nameCol = new TableColumn<>("Symbol Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-text-fill: #00daf3;");

        TableColumn<SymbolRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<SymbolRow, String> addrCol = new TableColumn<>("Address/Offset");
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<SymbolRow, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<SymbolRow, Integer> refsCol = new TableColumn<>("Refs");
        refsCol.setCellValueFactory(new PropertyValueFactory<>("refs"));

        TableColumn<SymbolRow, String> flagsCol = new TableColumn<>("Flags");
        flagsCol.setCellValueFactory(new PropertyValueFactory<>("flags"));

        symbolTableView.getColumns().addAll(nameCol, typeCol, addrCol, sizeCol, refsCol, flagsCol);
        symbolTableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #12181f;");
        symbolTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void buildFileTree(List<CompilationUnit> units) {
        TreeItem<Object> root = new TreeItem<>("Root");
        Map<String, TreeItem<Object>> folders = new HashMap<>();

        for (CompilationUnit cu : units) {
            Path path = cu.getFilePath();
            if (path == null) continue;

            String fullStr = path.toString();
            String fileName = path.getFileName().toString();
            
            // Build simple folder nodes
            String parentFolder = path.getParent() != null ? path.getParent().getFileName().toString() : "src";
            TreeItem<Object> parentItem = folders.computeIfAbsent(parentFolder, k -> {
                TreeItem<Object> fItem = new TreeItem<>(k);
                root.getChildren().add(fItem);
                fItem.setExpanded(true);
                return fItem;
            });

            TreeItem<Object> leaf = new TreeItem<>(cu);
            parentItem.getChildren().add(leaf);
        }

        fileTreeView.setRoot(root);
        fileTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof CompilationUnit) {
                    setText(((CompilationUnit) item).getFilePath().getFileName().toString());
                } else {
                    setText(item.toString());
                }
            }
        });
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

            // SSA IR Rendering
            IRNode irNode = cu.getIntermediateRepresentation();
            if (irNode != null) {
                irArea.setText(printIRNode(irNode, 0));
                
                // AST Tree View
                TreeItem<IRNode> astRoot = buildTreeView(irNode);
                astRoot.setExpanded(true);
                astTreeView.setRoot(astRoot);
            } else {
                irArea.setText("// No SSA IR representation computed.");
                astTreeView.setRoot(null);
            }

            // Symbol table list
            ObservableList<SymbolRow> symbols = FXCollections.observableArrayList();
            int addr = 0x4000;
            for (EntityInfo entity : cu.getEntities()) {
                symbols.add(new SymbolRow(
                        entity.getSimpleName(),
                        entity.getKind().name(),
                        String.format("0x%08X", addr),
                        "512B",
                        5,
                        "LOCAL"
                ));
                addr += 0x120;
            }
            symbolTableView.setItems(symbols);
        });
    }

    private String printIRNode(IRNode node, int indent) {
        if (node == null) return "";
        StringBuilder sb = new StringBuilder();
        String indentStr = "  ".repeat(indent);
        String nodeType = node.getClass().getSimpleName();

        sb.append(indentStr);
        if (node instanceof NamespaceDeclaration) {
            sb.append("namespace ").append(((NamespaceDeclaration) node).name());
        } else if (node instanceof ModuleDeclaration) {
            sb.append("module ").append(((ModuleDeclaration) node).name());
        } else if (node instanceof TypeDeclaration) {
            sb.append("class ").append(((TypeDeclaration) node).name());
        } else if (node instanceof CallableDeclaration) {
            sb.append("define ").append(((CallableDeclaration) node).name()).append("()");
        } else if (node instanceof VariableDeclaration) {
            sb.append("store ").append(((VariableDeclaration) node).name());
        } else if (node instanceof AssignmentExpression) {
            sb.append("assign");
        } else if (node instanceof CallExpression) {
            sb.append("call ").append(((CallExpression) node).receiver() != null ? ((CallExpression) node).receiver().getClass().getSimpleName() : "expr");
        } else {
            sb.append(nodeType.toLowerCase());
        }
        sb.append("\n");

        for (IRNode child : ChildrenCollector.collect(node)) {
            sb.append(printIRNode(child, indent + 1));
        }
        return sb.toString();
    }

    private TreeItem<IRNode> buildTreeView(IRNode node) {
        TreeItem<IRNode> item = new TreeItem<>(node);
        item.setExpanded(true);

        for (IRNode child : ChildrenCollector.collect(node)) {
            item.getChildren().add(buildTreeView(child));
        }

        return item;
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

        @Override public void visit(ModuleDeclaration node) { children.addAll(node.declarations()); }
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
    }
}
