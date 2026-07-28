package com.example.anuviya.ui.analysisReport.classification;

import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import com.example.anuviya.ui.analysisReport.state.ExecutiveSummaryState;
import com.example.anuviya.ui.analysisReport.state.FrameworkDetectionState;
import com.example.anuviya.ui.analysisReport.state.ProjectClassificationState;
import com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import com.example.anuviya.ui.analysisReport.uiComponent.EvidenceStyleUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Project Classification overlay — rebuilt around what ProjectClassificationResult
 * actually contains, not around the previous layout.
 *
 * Sections, top to bottom:
 *  1. Hero — primary type, gauge, ALL secondary types (full projectTypes map)
 *  2. Type Confidence Distribution — every ProjectType the classifier scored,
 *     not a filtered "candidates" shortlist
 *  3. Capability Matrix — plus total evidence count backing it
 *     (CapabilityProfile.totalEvidenceCount())
 *  4. Technology Intelligence — NEW. One expandable card per DetectedTechnology
 *     showing family/category/version and the matched-evidence trail
 *     (pattern -> value -> source file) that DetectedTechnology.matchedEvidence()
 *     carries and the old UI discarded entirely.
 *  5. Methodology note — states the actual scoring rules, not marketing copy.
 *  6. Evidence Ledger — the classification-level evidence table, category-coded.
 *
 * Requires the state additions in STATE_MODEL_ADDITIONS.md (TechnologyEntry gains
 * family/category/version/matchedEvidence/metadata; a totalCapabilityEvidenceCount
 * property is added to ProjectClassificationState).
 */
public class ProjectClassificationInspector extends AnalysisInspectorContent {

    private ProjectClassificationState classState;
    private ExecutiveSummaryState execState;
    private FrameworkDetectionState fwState;

    private final GridPane mainGrid = new GridPane();
    private final VBox leftCol = new VBox(20);
    private final VBox rightCol = new VBox(20);

    // Hero
    private final Canvas gaugeCanvas = new Canvas(100, 100);
    private final Label pctText = new Label("0%");
    private final Label primaryTitle = new Label("Unknown");
    private final HBox badgeContainer = new HBox(8);
    private final Label lblTypesVal = new Label("0");
    private final Label lblLibsVal = new Label("0");
    private final Label lblCapsVal = new Label("0");
    private final Label lblTechVal = new Label("0");

    // Type distribution
    private final VBox typeDistBox = new VBox(10);
    
    // Reasoning Panel
    private final VBox reasoningBox = new VBox(8);

    // Capability matrix
    private final GridPane matrixGrid = new GridPane();
    private final Label capEvidenceCaption = new Label();

    // Technology intelligence
    private final VBox techBox = new VBox(10);
    
    // Intelligence Graph
//    private final IntelligenceGraphView graphView = new IntelligenceGraphView();

    // Evidence ledger
    private final TableView<ProjectClassificationState.EvidenceEntry> evidenceTable = new TableView<>();

    public ProjectClassificationInspector() {
        setSpacing(24);
        setPadding(new Insets(10));

        mainGrid.setHgap(24);
        mainGrid.setVgap(20);
        mainGrid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        mainGrid.getColumnConstraints().addAll(col1, col2);

        leftCol.setFillWidth(true);
        rightCol.setFillWidth(true);
        mainGrid.add(leftCol, 0, 0);
        mainGrid.add(rightCol, 1, 0);

        buildHero();
        buildTypeDistribution();
//        buildReasoningPanel();
        buildCapabilityMatrix();
        buildEvidenceRegistry();

        getChildren().addAll(
                mainGrid,

                buildTechnologyIntelligenceSection(),
                buildMethodologyNote(),
                buildDividerSection("EVIDENCE LEDGER"),
                evidenceTable
        );
    }

    // ── HERO ─────────────────────────────────────────────────

    private void buildHero() {
        VBox heroCard = new VBox(20);
        heroCard.getStyleClass().add("ar-hero-overview-card");

        HBox topPart = new HBox(20);
        topPart.setAlignment(Pos.CENTER_LEFT);

        StackPane progressPane = new StackPane();
        progressPane.setMinSize(100, 100);
        progressPane.getChildren().addAll(gaugeCanvas, pctText);
        pctText.getStyleClass().add("ar-hero-pct");

        VBox titles = new VBox(6);
        primaryTitle.getStyleClass().add("ar-hero-title");
        badgeContainer.setAlignment(Pos.CENTER_LEFT);
        titles.getChildren().addAll(primaryTitle, badgeContainer);

        topPart.getChildren().addAll(progressPane, titles);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(12);
        statsGrid.setVgap(12);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(25);
            statsGrid.getColumnConstraints().add(c);
        }
        statsGrid.add(buildStatTile("TYPES DETECTED", lblTypesVal), 0, 0);
        statsGrid.add(buildStatTile("FRAMEWORKS", lblLibsVal), 1, 0);
        statsGrid.add(buildStatTile("CAPABILITIES", lblCapsVal), 2, 0);
        statsGrid.add(buildStatTile("TECHNOLOGIES", lblTechVal), 3, 0);

        heroCard.getChildren().addAll(topPart, statsGrid);
        leftCol.getChildren().add(heroCard);
    }

    private VBox buildStatTile(String label, Label valueVal) {
        VBox tile = new VBox(4);
        tile.getStyleClass().add("ar-stat-tile");
        tile.setAlignment(Pos.CENTER);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("ar-stat-tile-label");
        valueVal.getStyleClass().add("ar-stat-tile-value");
        tile.getChildren().addAll(valueVal, lbl);
        return tile;
    }

    // ── TYPE CONFIDENCE DISTRIBUTION (full projectTypes map) ───

    private void buildTypeDistribution() {
        VBox container = new VBox(12);
        container.getStyleClass().add("ar-candidates-box");
        Label title = new Label("TYPE CONFIDENCE DISTRIBUTION");
        title.getStyleClass().add("ar-label-tiny");
        Label sub = new Label("Every archetype the classifier scored — a project can carry several at once.");
        sub.getStyleClass().add("ar-body-muted");
        sub.setWrapText(true);
        container.getChildren().addAll(title, sub, typeDistBox);
        leftCol.getChildren().add(container);
    }
    
    // ── REASONING PANEL ────────────────────────────────────────

//    private void buildReasoningPanel() {
//        VBox container = new VBox(12);
//        container.getStyleClass().add("ar-candidates-box");
//        Label title = new Label("CLASSIFICATION REASONING");
//        title.getStyleClass().add("ar-label-tiny");
//        Label sub = new Label("What was missing to reach 100% confidence for the primary archetype.");
//        sub.getStyleClass().add("ar-body-muted");
//        sub.setWrapText(true);
//        container.getChildren().addAll(title, sub, reasoningBox);
//        leftCol.getChildren().add(container);
//    }

    // ── CAPABILITY MATRIX ───────────────────────────────────────

    private void buildCapabilityMatrix() {
        VBox container = new VBox(12);
        container.getStyleClass().add("ar-matrix-box");
        Label title = new Label("INFERRED CAPABILITY MATRIX");
        title.getStyleClass().add("ar-label-tiny");

        matrixGrid.setHgap(20);
        matrixGrid.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        matrixGrid.getColumnConstraints().addAll(c1, c2);

        capEvidenceCaption.getStyleClass().add("ar-body-muted");
        capEvidenceCaption.setWrapText(true);

        container.getChildren().addAll(title, matrixGrid, capEvidenceCaption);
        rightCol.getChildren().add(container);
    }

    // ── TECHNOLOGY INTELLIGENCE (new section) ──────────────────

    private VBox buildTechnologyIntelligenceSection() {
        VBox section = new VBox(12);
        Label title = new Label("TECHNOLOGY INTELLIGENCE");
        title.getStyleClass().add("ar-label-tiny");
        Label sub = new Label("Every technology matched, with the exact evidence that identified it — pattern, matched value, and source file.");
        sub.getStyleClass().add("ar-body-muted");
        sub.setWrapText(true);
        section.getChildren().addAll(title, sub, techBox);
        return section;
    }

    private VBox buildTechnologyCard(ProjectClassificationState.TechnologyEntry tech) {
        VBox card = new VBox(0);
        card.getStyleClass().add("ar-tech-card");

        VBox evidencePanel = new VBox(8);
        evidencePanel.getStyleClass().add("ar-tech-evidence-panel");
        evidencePanel.setManaged(false);
        evidencePanel.setVisible(false);

        Label chevron = new Label("expand_more");
        chevron.getStyleClass().add("ar-tech-chevron");
        chevron.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 18px;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("ar-tech-header");

        VBox nameBlock = new VBox(4);
        HBox.setHgrow(nameBlock, Priority.ALWAYS);
        Label name = new Label(tech.name());
        name.getStyleClass().add("ar-tech-name");

        HBox tagRow = new HBox(6);
        tagRow.setAlignment(Pos.CENTER_LEFT);
        if (tech.family() != null && !tech.family().isBlank()) {
            Label familyChip = new Label(tech.family().toUpperCase());
            familyChip.getStyleClass().add(EvidenceStyleUtil.chipStyle(tech.family()));
            tagRow.getChildren().add(familyChip);
        }
        if (tech.category() != null && !tech.category().isBlank()) {
            Label catLbl = new Label(tech.category());
            catLbl.getStyleClass().add("ar-body-muted");
            tagRow.getChildren().add(catLbl);
        }
        if (tech.version() != null && !tech.version().isBlank()) {
            Label versionChip = new Label("v" + tech.version());
            versionChip.getStyleClass().add("ar-tech-version-chip");
            tagRow.getChildren().add(versionChip);
        }
        nameBlock.getChildren().addAll(name, tagRow);
        
        if (tech.providedCapabilities() != null && !tech.providedCapabilities().isEmpty()) {
            HBox capsRow = new HBox(4);
            capsRow.setAlignment(Pos.CENTER_LEFT);
            for (String cap : tech.providedCapabilities()) {
                Label capChip = new Label(cap);
                capChip.getStyleClass().add("ar-tech-provided-cap-chip");
                capsRow.getChildren().add(capChip);
            }
            nameBlock.getChildren().add(capsRow);
        }

        Label confBadge = new Label(String.format("%.0f%%", tech.confidence() * 100));
        confBadge.getStyleClass().add(confidenceBadgeStyle(tech.confidence()));

        int evidenceCount = tech.matchedEvidence() == null ? 0 : tech.matchedEvidence().size();
        Label evCountLbl = new Label(evidenceCount + " SIGNAL" + (evidenceCount == 1 ? "" : "S"));
        evCountLbl.getStyleClass().add("ar-label-tiny");

        header.getChildren().addAll(nameBlock, evCountLbl, confBadge, chevron);
        header.setOnMouseClicked(e -> {
            boolean expanding = !evidencePanel.isVisible();
            evidencePanel.setVisible(expanding);
            evidencePanel.setManaged(expanding);
            chevron.setText(expanding ? "expand_less" : "expand_more");
        });

        if (tech.matchedEvidence() != null) {
            for (var ev : tech.matchedEvidence()) {
                evidencePanel.getChildren().add(buildEvidenceRow(ev));
            }
        }
        if (tech.metadata() != null && !tech.metadata().isEmpty()) {
            FlowPane metaRow = new FlowPane(6, 6);
            for (Map.Entry<String, String> entry : tech.metadata().entrySet()) {
                Label chip = new Label(entry.getKey() + ": " + entry.getValue());
                chip.getStyleClass().add("ar-meta-chip");
                metaRow.getChildren().add(chip);
            }
            evidencePanel.getChildren().add(metaRow);
        }

        card.getChildren().addAll(header, evidencePanel);
        return card;
    }

    private HBox buildEvidenceRow(ProjectClassificationState.TechnologyEvidenceEntry ev) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("ar-tech-evidence-row");

        Region dot = new Region();
        dot.setMinSize(6, 6);
        dot.setMaxSize(6, 6);
        dot.getStyleClass().add(EvidenceStyleUtil.dotStyle(ev.type()));

        Label typeLbl = new Label(EvidenceStyleUtil.humanize(ev.type()));
        typeLbl.getStyleClass().add(EvidenceStyleUtil.textStyle(ev.type()));
        typeLbl.setMinWidth(140);

        VBox detail = new VBox(2);
        HBox.setHgrow(detail, Priority.ALWAYS);
        Label matchLine = new Label(ev.pattern() + "  →  " + ev.value());
        matchLine.getStyleClass().add("ar-tech-evidence-match");
        Label descLine = new Label(
                (ev.description() != null ? ev.description() + "  " : "")
                        + (ev.sourceFile() != null ? "· " + ev.sourceFile() : "")
        );
        descLine.getStyleClass().add("ar-body-muted");
        detail.getChildren().addAll(matchLine, descLine);

        row.getChildren().addAll(dot, typeLbl, detail);
        return row;
    }

    private String confidenceBadgeStyle(double conf) {
        if (conf >= 0.7) return "ar-fw-conf-badge-high";
        if (conf >= 0.4) return "ar-fw-conf-badge-mid";
        return "ar-fw-conf-badge-low";
    }

    // ── METHODOLOGY NOTE ─────────────────────────────────────

    private VBox buildMethodologyNote() {
        VBox box = new VBox(6);
        box.getStyleClass().add("ar-methodology-box");
        Label title = new Label("HOW THESE SCORES ARE COMPUTED");
        title.getStyleClass().add("ar-label-tiny");
        Label body = new Label(
                "Framework confidence is the share of independent evidence categories that agree "
                        + "(dependency, build file, annotation, code pattern, configuration, file structure, "
                        + "entity tag) — capped at 100% once four or more categories align. Framework signal "
                        + "strength is the raw sum of matched evidence weights and is not capped, so a heavily "
                        + "confirmed framework can exceed 100%. Capability and technology confidence reflect the "
                        + "strength of their own matched evidence independently of the type classification."
        );
        body.getStyleClass().add("ar-body-muted");
        body.setWrapText(true);
        box.getChildren().addAll(title, body);
        return box;
    }

    private VBox buildDividerSection(String label) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("ar-label-tiny");
        Region div = new Region();
        div.getStyleClass().add("ar-divider");
        box.getChildren().addAll(lbl, div);
        return box;
    }

    // ── EVIDENCE LEDGER TABLE ────────────────────────────────

    @SuppressWarnings("unchecked")
    private void buildEvidenceRegistry() {
        evidenceTable.getStyleClass().add("ar-table");
        evidenceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        evidenceTable.setPrefHeight(230);

        TableColumn<ProjectClassificationState.EvidenceEntry, String> catCol = new TableColumn<>("CATEGORY");
        catCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().category()));
        catCol.setMinWidth(150);
        catCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                HBox wrap = new HBox(6);
                wrap.setAlignment(Pos.CENTER_LEFT);
                Region dot = new Region();
                dot.setMinSize(6, 6);
                dot.setMaxSize(6, 6);
                dot.getStyleClass().add(EvidenceStyleUtil.dotStyle(item));
                Label lbl = new Label(item.replace("_", " ").toUpperCase());
                lbl.getStyleClass().add(EvidenceStyleUtil.textStyle(item));
                wrap.getChildren().addAll(dot, lbl);
                setGraphic(wrap);
                setText(null);
            }
        });

        TableColumn<ProjectClassificationState.EvidenceEntry, String> descCol = new TableColumn<>("DESCRIPTION");
        descCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().description()));
        descCol.setMinWidth(280);
        descCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (!empty) getStyleClass().setAll("table-cell", "ar-evidence-desc-cell");
            }
        });

        TableColumn<ProjectClassificationState.EvidenceEntry, Double> wtCol = new TableColumn<>("WEIGHT");
        wtCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().weight()));
        wtCol.setMinWidth(100);
        wtCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("+%.2f", item));
                setStyle("-fx-alignment: CENTER-RIGHT;");
                getStyleClass().setAll("table-cell", "ar-table-namespace-cell");
            }
        });

        evidenceTable.getColumns().addAll(catCol, descCol, wtCol);
        evidenceTable.setRowFactory(tv -> {
            TableRow<ProjectClassificationState.EvidenceEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    // ── AnalysisInspectorContent ─────────────────────────────

    @Override
    public String getTitle() {
        return "Project Classification";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("layers");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #00daf3;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return classState != null ? classState.primaryConfidenceProperty().get() + " CONFIDENCE" : "";
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.classState = state.getProjectClassificationState();
        this.execState = state.getExecutiveSummaryState();
        this.fwState = state.getFrameworkDetectionState();

        primaryTitle.textProperty().bind(classState.primaryClassificationProperty());
        pctText.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%.0f%%", classState.primaryConfidenceProperty().get() * 100),
                classState.primaryConfidenceProperty()
        ));

        lblTypesVal.textProperty().bind(Bindings.size(classState.getDetectedTypes()).asString());
        lblLibsVal.textProperty().bind(Bindings.size(fwState.getDetectedFrameworks()).asString());
        lblCapsVal.textProperty().bind(Bindings.size(classState.getCapabilities()).asString());
        lblTechVal.textProperty().bind(Bindings.size(classState.getDetectedTechnologies()).asString());

        classState.primaryConfidenceProperty().addListener((o, ov, nv) -> drawGauge());
        drawGauge();

        classState.getDetectedTypes().addListener((ListChangeListener<ProjectClassificationState.ClassificationEntry>) c -> {
            rebuildBadges();
            rebuildTypeDistribution();
        });
        rebuildBadges();
        rebuildTypeDistribution();
        
//        classState.getMissingRequirements().addListener((ListChangeListener<ProjectClassificationState.MissingRequirementEntry>) c -> rebuildReasoning());
//        rebuildReasoning();

        classState.getCapabilities().addListener((ListChangeListener<ProjectClassificationState.CapabilityEntry>) c -> rebuildMatrix());
        rebuildMatrix();

        classState.totalCapabilityEvidenceCountProperty().addListener((o, ov, nv) -> updateCapEvidenceCaption());
        updateCapEvidenceCaption();

        classState.getDetectedTechnologies().addListener((ListChangeListener<ProjectClassificationState.TechnologyEntry>) c -> {
            rebuildTechnologies();
//            graphView.update(classState);
        });
        rebuildTechnologies();
//        graphView.update(classState);

        evidenceTable.setItems(classState.getEvidence());
    }

    private void updateCapEvidenceCaption() {
        int n = classState.totalCapabilityEvidenceCountProperty().get();
        capEvidenceCaption.setText("Backed by " + n + " evidence signal" + (n == 1 ? "" : "s") + " across the codebase.");
    }

    private void drawGauge() {
        GraphicsContext gc = gaugeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 100, 100);

        double cx = 50, cy = 50, r = 42, sw = 6;
        double fraction = classState.primaryConfidenceProperty().get();
        double clamp = Math.max(0.0, Math.min(1.0, fraction));

        gc.setStroke(Color.web("#1a2027"));
        gc.setLineWidth(sw);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

        if (clamp > 0) {
            String hex = clamp >= 0.7 ? "#00e5ff" : clamp >= 0.4 ? "#fec931" : "#ff4b4b";
            gc.setStroke(Color.web(hex));
            gc.setLineWidth(sw);
            gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 90, -(360 * clamp), ArcType.OPEN);
        }
    }

    private void rebuildBadges() {
        badgeContainer.getChildren().clear();
        Label primBadge = new Label("PRIMARY");
        primBadge.getStyleClass().add("ar-hero-badge-primary");
        badgeContainer.getChildren().add(primBadge);

        String primaryName = primaryTitle.getText();
        classState.getDetectedTypes().stream()
                .filter(e -> !e.name().equalsIgnoreCase(primaryName))
                .sorted(Comparator.comparingDouble(ProjectClassificationState.ClassificationEntry::confidence).reversed())
                .forEach(e -> {
                    Label badge = new Label(e.name().toUpperCase());
                    badge.getStyleClass().add("ar-hero-badge");
                    badgeContainer.getChildren().add(badge);
                });
    }

    private void rebuildTypeDistribution() {
        typeDistBox.getChildren().clear();
        classState.getDetectedTypes().stream()
                .sorted(Comparator.comparingDouble(ProjectClassificationState.ClassificationEntry::confidence).reversed())
                .forEach(entry -> {
                    HBox row = new HBox(12);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("ar-candidate-row");

                    Label name = new Label(entry.name().toUpperCase());
                    name.getStyleClass().add("ar-candidate-name");

                    StackPane track = new StackPane();
                    track.getStyleClass().add("ar-candidate-track");
                    Region fill = new Region();
                    fill.getStyleClass().add("ar-candidate-fill");
                    StackPane.setAlignment(fill, Pos.CENTER_LEFT);
                    track.getChildren().add(fill);
                    HBox.setHgrow(track, Priority.ALWAYS);

                    double conf = entry.confidence();
                    track.widthProperty().addListener((o, ov, w) -> fill.setPrefWidth(w.doubleValue() * conf));

                    Label val = new Label(String.format("%.1f%%", conf * 100));
                    val.getStyleClass().add("ar-candidate-val");

                    row.getChildren().addAll(name, track, val);
                    typeDistBox.getChildren().add(row);
                });
    }

//    private void rebuildReasoning() {
//        reasoningBox.getChildren().clear();
//        if (classState.getMissingRequirements().isEmpty()) {
//            Label full = new Label("All required and boosting capabilities matched.");
//            full.getStyleClass().add("ar-reasoning-full");
//            reasoningBox.getChildren().add(full);
//        } else {
//            for (var missing : classState.getMissingRequirements()) {
//                HBox row = new HBox(8);
//                row.setAlignment(Pos.CENTER_LEFT);
//                Label dot = new Label("remove");
//                dot.getStyleClass().add("ar-reasoning-missing-icon");
//                Label lbl = new Label("Missing: " + missing.name() + " (" + missing.category() + ")");
//                lbl.getStyleClass().add("ar-body-muted");
//                row.getChildren().addAll(dot, lbl);
//                reasoningBox.getChildren().add(row);
//            }
//        }
//    }

    private void rebuildMatrix() {
        matrixGrid.getChildren().clear();
        var caps = classState.getCapabilities();

        Map<String, List<ProjectClassificationState.CapabilityEntry>> grouped = new LinkedHashMap<>();
        for (var cap : caps) {
            grouped.computeIfAbsent(cap.category(), k -> new ArrayList<>()).add(cap);
        }

        int row = 0;
        for (Map.Entry<String, List<ProjectClassificationState.CapabilityEntry>> entry : grouped.entrySet()) {
            Label catLbl = new Label(entry.getKey());
            catLbl.getStyleClass().addAll("ar-label-tiny", "ar-matrix-category-label");
            GridPane.setColumnSpan(catLbl, 2);
            matrixGrid.add(catLbl, 0, row++);

            var capList = entry.getValue();
            for (int i = 0; i < capList.size(); i += 2) {
                matrixGrid.add(buildCapabilityCell(capList.get(i)), 0, row);
                if (i + 1 < capList.size()) {
                    matrixGrid.add(buildCapabilityCell(capList.get(i + 1)), 1, row);
                }
                row++;
            }
        }
    }

    private HBox buildCapabilityCell(ProjectClassificationState.CapabilityEntry cap) {
        HBox cell = new HBox(8);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.getStyleClass().add("ar-capability-cell");

        double conf = cap.confidence();
        String icon, stateStyle;
        if (conf > 0.66) { icon = "check_circle"; stateStyle = "ar-capability-active"; }
        else if (conf > 0.33) { icon = "adjust"; stateStyle = "ar-capability-partial"; }
        else { icon = "radio_button_unchecked"; stateStyle = "ar-capability-inactive"; }

        Label check = new Label(icon);
        check.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 14px;");
        check.getStyleClass().add(stateStyle);

        Label capName = new Label(cap.name());
        capName.getStyleClass().add("ar-capability-name");
        HBox.setHgrow(capName, Priority.ALWAYS);

        Label capPct = new Label(String.format("%.0f%%", conf * 100));
        capPct.getStyleClass().add("ar-capability-pct");

        cell.getChildren().addAll(check, capName, capPct);
        return cell;
    }

    private void rebuildTechnologies() {
        techBox.getChildren().clear();
        classState.getDetectedTechnologies().stream()
                .sorted(Comparator.comparingDouble(ProjectClassificationState.TechnologyEntry::confidence).reversed())
                .forEach(tech -> techBox.getChildren().add(buildTechnologyCard(tech)));

        if (classState.getDetectedTechnologies().isEmpty()) {
            Label empty = new Label("No technologies were matched for this project.");
            empty.getStyleClass().add("ar-empty-state");
            techBox.getChildren().add(empty);
        }
    }
}