package com.example.anuviya.ui.analysisReport.framework;

import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import com.example.anuviya.ui.analysisReport.state.FrameworkDetectionState;
import com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import com.example.anuviya.ui.analysisReport.uiComponent.EvidenceStyleUtil;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

/**
 * Framework Detection overlay — rebuilt around the fact that FrameworkDetectionResult
 * actually produces THREE distinct signals per framework, none of which the old UI
 * exposed beyond a single blended "confidence":
 *
 *  - score:       raw sum of matched FrameworkEvidence weights (unbounded)
 *  - confidence:  fraction of the 4 evidence categories that independently agree (0-1)
 *  - detected:    whether score cleared the detector's threshold
 *
 * Plus per-framework capabilities() and the full evidence() trail (category,
 * description, weight, source) — which is what actually answers "how did you
 * conclude this framework is present."
 *
 * Layout:
 *  1. Summary strip — detected count, below-threshold count, avg confidence
 *  2. Methodology note — states the real scoring rule
 *  3. Framework cards, sorted by confidence, each with:
 *       - detected/below-threshold status badge
 *       - dual metric row: signal strength (score) + confidence (%)
 *       - granted-capability chips
 *       - expandable evidence trail (category-color-coded, reuses EvidenceStyleUtil)
 *
 * Requires the FrameworkEntry / FrameworkEvidenceEntry additions described in
 * STATE_MODEL_ADDITIONS.md.
 */
public class FrameworkInspector extends AnalysisInspectorContent {

    private final Label lblDetectedVal = new Label("0");
    private final Label lblBelowVal = new Label("0");
    private final Label lblAvgVal = new Label("0%");

    private final VBox listContainer = new VBox(12);
    private FrameworkDetectionState state;

    private static final String[] ICON_STYLES = {
            "ar-fw-icon-cyan", "ar-fw-icon-green", "ar-fw-icon-orange", "ar-fw-icon-secondary"
    };

    public FrameworkInspector() {
        setSpacing(20);
        setPadding(new Insets(10));
        getChildren().addAll(buildSummaryStrip(), buildMethodologyNote(), buildListSection());
    }

    private GridPane buildSummaryStrip() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setMaxWidth(Double.MAX_VALUE);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / 3);
            grid.getColumnConstraints().add(c);
        }
        grid.add(buildStatTile("DETECTED", lblDetectedVal), 0, 0);
        grid.add(buildStatTile("BELOW THRESHOLD", lblBelowVal), 1, 0);
        grid.add(buildStatTile("AVG CONFIDENCE", lblAvgVal), 2, 0);
        return grid;
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

    private VBox buildMethodologyNote() {
        VBox box = new VBox(6);
        box.getStyleClass().add("ar-methodology-box");
        Label title = new Label("SCORE VS. CONFIDENCE");
        title.getStyleClass().add("ar-label-tiny");
        Label body = new Label(
                "Signal strength is the raw sum of every matched evidence weight for that framework — "
                        + "it has no ceiling, so a framework confirmed by many strong signals can read well "
                        + "over 100%. Confidence is different: it's the share of independent evidence categories "
                        + "(dependency, build file, annotation, code pattern, configuration, file structure, entity "
                        + "tag) that found at least one match, capped at 100% once four categories agree. A "
                        + "framework can have high signal strength from one category and still show low confidence."
        );
        body.getStyleClass().add("ar-body-muted");
        body.setWrapText(true);
        box.getChildren().addAll(title, body);
        return box;
    }

    private VBox buildListSection() {
        VBox section = new VBox(12);
        Label label = new Label("ALL DETECTED FRAMEWORKS");
        label.getStyleClass().add("ar-label-tiny");
        section.getChildren().addAll(label, listContainer);
        return section;
    }

    @Override
    public String getTitle() {
        return "Framework Detection";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("extension");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #00daf3;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getFrameworkDetectionState();
        this.state.getDetectedFrameworks().addListener(
                (ListChangeListener<FrameworkDetectionState.FrameworkEntry>) c -> rebuild());
        rebuild();
    }

    private void rebuild() {
        listContainer.getChildren().clear();

        List<FrameworkDetectionState.FrameworkEntry> sorted = state.getDetectedFrameworks().stream()
                .sorted(Comparator.comparingDouble(FrameworkDetectionState.FrameworkEntry::confidence).reversed())
                .toList();

        updateSummary(sorted);

        for (int i = 0; i < sorted.size(); i++) {
            listContainer.getChildren().add(buildCard(sorted.get(i), i));
        }

        if (sorted.isEmpty()) {
            Label empty = new Label("No frameworks were detected for this project.");
            empty.getStyleClass().add("ar-empty-state");
            listContainer.getChildren().add(empty);
        }
    }

    private void updateSummary(List<FrameworkDetectionState.FrameworkEntry> sorted) {
        long detectedCount = sorted.stream().filter(FrameworkDetectionState.FrameworkEntry::detected).count();
        lblDetectedVal.setText(String.valueOf(detectedCount));
        lblBelowVal.setText(String.valueOf(sorted.size() - detectedCount));

        double avg = sorted.stream()
                .mapToDouble(FrameworkDetectionState.FrameworkEntry::confidence)
                .average()
                .orElse(0.0);
        lblAvgVal.setText(String.format("%.0f%%", avg * 100));
    }

    private VBox buildCard(FrameworkDetectionState.FrameworkEntry fw, int index) {
        VBox card = new VBox(12);
        card.getStyleClass().add("ar-fw-card");

        // ── Header ──
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWrap = new StackPane();
        iconWrap.getStyleClass().addAll("ar-fw-icon-wrap", ICON_STYLES[index % ICON_STYLES.length]);
        iconWrap.setMinSize(40, 40);
        iconWrap.setMaxSize(40, 40);
        Label iconLabel = new Label(fw.icon() != null && !fw.icon().isBlank() ? fw.icon() : "📦");
        iconLabel.getStyleClass().add("ar-fw-icon-glyph");
        iconWrap.getChildren().add(iconLabel);

        VBox titleBlock = new VBox(2);
        HBox.setHgrow(titleBlock, Priority.ALWAYS);
        Label name = new Label(fw.name());
        name.getStyleClass().add("ar-fw-name");
        Label statusLbl = new Label(fw.detected() ? "DETECTED" : "BELOW THRESHOLD");
        statusLbl.getStyleClass().add(fw.detected() ? "ar-fw-status-detected" : "ar-fw-status-below");
        titleBlock.getChildren().addAll(name, statusLbl);

        header.getChildren().addAll(iconWrap, titleBlock);

        // ── Dual metric row: signal strength vs confidence ──
        HBox metricsRow = new HBox(20);

        VBox scoreBlock = buildMetricBlock("SIGNAL STRENGTH", String.format("%.2f", fw.score()),
                Math.min(1.0, fw.score()), confidenceFillStyle(Math.min(1.0, fw.score())));
        HBox.setHgrow(scoreBlock, Priority.ALWAYS);

        VBox confBlock = buildMetricBlock("CONFIDENCE", String.format("%.0f%%", fw.confidence() * 100),
                fw.confidence(), confidenceFillStyle(fw.confidence()));
        HBox.setHgrow(confBlock, Priority.ALWAYS);

        metricsRow.getChildren().addAll(scoreBlock, confBlock);

        card.getChildren().addAll(header, metricsRow);

        // ── Capability chips ──
        if (fw.capabilities() != null && !fw.capabilities().isEmpty()) {
            Label capsLabel = new Label("GRANTS");
            capsLabel.getStyleClass().add("ar-label-tiny");
            FlowPane chips = new FlowPane(6, 6);
            for (String cap : fw.capabilities()) {
                Label chip = new Label(EvidenceStyleUtil.humanize(cap));
                chip.getStyleClass().add(EvidenceStyleUtil.chipStyle(cap));
                chips.getChildren().add(chip);
            }
            VBox capsBox = new VBox(6, capsLabel, chips);
            card.getChildren().add(capsBox);
        }

        // ── Expandable evidence trail ──
        if (fw.evidence() != null && !fw.evidence().isEmpty()) {
            card.getChildren().add(buildEvidenceToggle(fw));
        }

        return card;
    }

    private VBox buildMetricBlock(String label, String value, double fillFraction, String fillStyle) {
        VBox block = new VBox(6);
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("ar-label-tiny");
        HBox.setHgrow(lbl, Priority.ALWAYS);
        Label val = new Label(value);
        val.getStyleClass().add("ar-fw-metric-value");
        top.getChildren().addAll(lbl, val);

        StackPane track = new StackPane();
        track.getStyleClass().add("ar-fw-progress-track");
        Region fill = new Region();
        fill.getStyleClass().add(fillStyle);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        track.getChildren().add(fill);
        double clamped = Math.max(0.0, Math.min(1.0, fillFraction));
        track.widthProperty().addListener((o, ov, w) -> fill.setPrefWidth(w.doubleValue() * clamped));

        block.getChildren().addAll(top, track);
        return block;
    }

    private VBox buildEvidenceToggle(FrameworkDetectionState.FrameworkEntry fw) {
        VBox wrapper = new VBox(8);

        VBox evidencePanel = new VBox(8);
        evidencePanel.getStyleClass().add("ar-tech-evidence-panel");
        evidencePanel.setManaged(false);
        evidencePanel.setVisible(false);
        for (var ev : fw.evidence()) {
            evidencePanel.getChildren().add(buildEvidenceRow(ev));
        }

        HBox toggleRow = new HBox(6);
        toggleRow.setAlignment(Pos.CENTER_LEFT);
        toggleRow.getStyleClass().add("ar-fw-evidence-toggle");

        Label toggleLabel = new Label("EVIDENCE TRAIL (" + fw.evidence().size() + ")");
        toggleLabel.getStyleClass().add("ar-label-tiny");
        Label chevron = new Label("expand_more");
        chevron.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 16px;");
        chevron.getStyleClass().add("ar-tech-chevron");

        toggleRow.getChildren().addAll(toggleLabel, chevron);
        toggleRow.setOnMouseClicked(e -> {
            boolean expanding = !evidencePanel.isVisible();
            evidencePanel.setVisible(expanding);
            evidencePanel.setManaged(expanding);
            chevron.setText(expanding ? "expand_less" : "expand_more");
        });

        wrapper.getChildren().addAll(toggleRow, evidencePanel);
        return wrapper;
    }

    private HBox buildEvidenceRow(FrameworkDetectionState.FrameworkEvidenceEntry ev) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("ar-tech-evidence-row");

        Region dot = new Region();
        dot.setMinSize(6, 6);
        dot.setMaxSize(6, 6);
        dot.getStyleClass().add(EvidenceStyleUtil.dotStyle(ev.category()));

        Label catLbl = new Label(EvidenceStyleUtil.humanize(ev.category()));
        catLbl.getStyleClass().add(EvidenceStyleUtil.textStyle(ev.category()));
        catLbl.setMinWidth(120);

        VBox detail = new VBox(2);
        HBox.setHgrow(detail, Priority.ALWAYS);
        Label descLine = new Label(ev.description());
        descLine.getStyleClass().add("ar-body-sm");
        Label sourceLine = new Label(ev.source() != null ? ev.source() : "");
        sourceLine.getStyleClass().add("ar-body-muted");
        detail.getChildren().addAll(descLine, sourceLine);

        Label weightLbl = new Label(String.format("+%.2f", ev.weight()));
        weightLbl.getStyleClass().add("ar-fw-evidence-weight");

        row.getChildren().addAll(dot, catLbl, detail, weightLbl);
        return row;
    }

    private String confidenceFillStyle(double conf) {
        if (conf >= 0.7) return "ar-fw-progress-fill-high";
        if (conf >= 0.4) return "ar-fw-progress-fill-mid";
        return "ar-fw-progress-fill-low";
    }
}