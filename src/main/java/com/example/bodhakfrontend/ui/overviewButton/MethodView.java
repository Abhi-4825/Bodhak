package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.core.model.entity.*;
import com.example.bodhakfrontend.core.model.incremental.EntityViewModel;
import com.example.bodhakfrontend.core.model.entity.MethodFilter;
import com.example.bodhakfrontend.ui.helper.UiFeatures;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Method Intelligence Dashboard — premium futuristic UI for method analysis.
 * Features a sticky Source Preview Card that renders method body on click.
 */
public class MethodView {

    private final UiFeatures uiFeatures;
    private final Map<String, EntityViewModel> vmMap;
    private static final int MIN_PREVIEW_LINES = 6;
    private static final int MAX_PREVIEW_LINES = 10;
    private static final double LINE_HEIGHT = 22;

    // ── Layout Regions ────────────────────────────────────────────────────────
    private final VBox root = new VBox(0);
    private final VBox cardContainer = new VBox(12);
    private final ScrollPane scrollPane;

    // ── Source Preview Panel (shown on click) ─────────────────────────────────
    private final VBox sourcePreviewCard = new VBox(0);
    private final Label previewTitle = new Label();
    private final Label previewMeta  = new Label();
    private final TextArea sourceArea = new TextArea();
    private MemberInfo previewedMember = null;

    // ── State ─────────────────────────────────────────────────────────────────
    private EntityViewModel currentVm;
    private MethodFilter activeFilter = MethodFilter.ALL;
    private final List<Button> filterChips = new ArrayList<>();

    public MethodView(UiFeatures uiFeatures, Map<String, EntityViewModel> vmMap) {
        this.uiFeatures = uiFeatures;
        this.vmMap = vmMap;

        root.getStyleClass().add("method-dashboard");
        cardContainer.setPadding(new Insets(0, 20, 20, 20));

        scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("method-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        buildSourcePreviewCard();
    }

    public Node show(String entityName) {
        unbind();
        bind(entityName);
        rebuild();
        return root;
    }

    // ── Source Preview Card Construction ──────────────────────────────────────

    private void buildSourcePreviewCard() {
        sourcePreviewCard.getStyleClass().add("source-preview-card");
        sourcePreviewCard.setVisible(false);
        sourcePreviewCard.setManaged(false);


        // ── Header row ────────────────────────────────────────────────────
        Label icon = new Label("◈");
        icon.getStyleClass().add("preview-icon");

        VBox titleBox = new VBox(2);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label sectionLabel = new Label("SOURCE INTELLIGENCE PREVIEW");
        sectionLabel.getStyleClass().add("preview-section-label");

        previewTitle.getStyleClass().add("preview-method-title");
        previewMeta.getStyleClass().add("preview-meta");

        titleBox.getChildren().addAll(sectionLabel, previewTitle, previewMeta);

        // Action buttons
        Button copyBtn = new Button("⎘ Copy");
        copyBtn.getStyleClass().add("preview-action-btn");
        copyBtn.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(sourceArea.getText());
            Clipboard.getSystemClipboard().setContent(cc);
            copyBtn.setText("✔ Copied");
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override public void run() {
                    Platform.runLater(() -> copyBtn.setText("⎘ Copy"));
                }
            }, 1500);
        });
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("preview-close-btn");
        closeBtn.setOnAction(e -> hideSourcePreview());

        HBox header = new HBox(12, icon, titleBox, copyBtn, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 10, 16));
        header.getStyleClass().add("preview-header");

        // ── Code body ─────────────────────────────────────────────────────
        sourceArea.setEditable(false);
        sourceArea.setWrapText(false);
        sourceArea.getStyleClass().add("source-preview-area");

        VBox.setVgrow(sourceArea, Priority.ALWAYS);

        // ── Line-number gutter ─────────────────────────────────────────────
        HBox codeBox = new HBox();
        codeBox.setAlignment(Pos.TOP_LEFT);
        codeBox.getStyleClass().add("source-code-box");
        codeBox.setPadding(new Insets(0, 16, 14, 16));

        // Line number column
        TextArea lineNumbers = new TextArea();
        lineNumbers.setEditable(false);
        lineNumbers.setMouseTransparent(true);
        lineNumbers.setFocusTraversable(false);
        lineNumbers.getStyleClass().add("line-number-area");
        lineNumbers.setPrefColumnCount(4);
        lineNumbers.setMinWidth(48);
        lineNumbers.setMaxWidth(48);
        lineNumbers.prefHeightProperty()
                .bind(sourceArea.prefHeightProperty());

        sourceArea.textProperty().addListener((obs, o, text) -> {
            int lines = text.isEmpty() ? 0 : text.split("\n", -1).length;
            StringBuilder nums = new StringBuilder();
            for (int i = 0; i < lines; i++) {
                nums.append(i == 0 && previewedMember != null
                        ? previewedMember.getStartLine()
                        : (previewedMember != null ? previewedMember.getStartLine() + i : i + 1));
                if (i < lines - 1) nums.append("\n");
            }
            lineNumbers.setText(nums.toString());
        });

        // Sync scroll between gutter and code area
        sourceArea.scrollTopProperty().addListener((obs, o, n) -> lineNumbers.setScrollTop(n.doubleValue()));

        HBox.setHgrow(sourceArea, Priority.ALWAYS);
        HBox.setHgrow(codeBox, Priority.ALWAYS);
        codeBox.getChildren().addAll(lineNumbers, sourceArea);

        sourcePreviewCard.getChildren().addAll(header, codeBox);
    }

    private void showSourcePreview(MemberInfo m) {
        previewedMember = m;

        boolean isConstructor = m.getKind() == MemberKind.CONSTRUCTOR;
        String kindTag = isConstructor ? "CONSTRUCTOR" : "METHOD";
        String returnStr = isConstructor ? "" : (" → " + (m.getReturnType() != null && !m.getReturnType().isBlank() ? m.getReturnType() : "void"));

        previewTitle.setText(m.getName() + buildParamStr(m) + returnStr);

        String vis = m.getModifiers().stream()
                .filter(mod -> mod == ModifierKind.PUBLIC || mod == ModifierKind.PRIVATE || mod == ModifierKind.PROTECTED)
                .map(mod -> mod.name().toLowerCase())
                .findFirst().orElse("package-private");

        previewMeta.setText(kindTag + "  ·  " + vis + "  ·  Lines " + m.getStartLine() + "–" + m.getEndLine()
                + "  ·  " + m.getStatementCount() + " statements");

        // Extract source body
        String src = extractSourceBody(m);
        sourceArea.setText(src);
        updatePreviewSizing(src);

        if (!sourcePreviewCard.isVisible()) {
            sourcePreviewCard.setVisible(true);
            sourcePreviewCard.setManaged(true);
            FadeTransition ft = new FadeTransition(Duration.millis(250), sourcePreviewCard);
            ft.setFromValue(0);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    private void hideSourcePreview() {
        FadeTransition ft = new FadeTransition(Duration.millis(200), sourcePreviewCard);
        ft.setFromValue(1.0);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            sourcePreviewCard.setVisible(false);
            sourcePreviewCard.setManaged(false);
        });
        ft.play();
        previewedMember = null;
    }

    private String extractSourceBody(MemberInfo m) {
        try {
            List<String> allLines = Files.readAllLines(m.getSourceFile().toPath());
            int start = Math.max(0, m.getStartLine() - 1);
            int end   = Math.min(allLines.size(), m.getEndLine());
            if (start >= end) return "// Source range out of bounds";

            // Detect leading whitespace of first line and strip it uniformly (dedent)
            List<String> body = allLines.subList(start, end);
            int indent = body.stream()
                    .filter(l -> !l.isBlank())
                    .mapToInt(l -> l.length() - l.stripLeading().length())
                    .min().orElse(0);
            return body.stream()
                    .map(l -> l.length() >= indent ? l.substring(indent) : l)
                    .collect(Collectors.joining("\n"));

        } catch (IOException e) {
            return "// Could not read source: " + e.getMessage();
        }
    }

    private String buildParamStr(MemberInfo m) {
        if (m.getParameters() == null || m.getParameters().isEmpty()) return "()";
        return "(" + m.getParameters().stream()
                .map(p -> p.getType() + " " + p.getName())
                .collect(Collectors.joining(", ")) + ")";
    }

    // ── Bind / Unbind ─────────────────────────────────────────────────────────

    private void bind(String entityName) {
        currentVm = vmMap.get(entityName);
        if (currentVm != null) currentVm.getMembers().addListener(memberListener);

        List<MemberInfo> members = currentVm != null ? new ArrayList<>(currentVm.getMembers()) : List.of();
        root.getChildren().setAll(sourcePreviewCard, buildFilterBar(members), scrollPane);
    }

    private void unbind() {
        if (currentVm == null) return;
        currentVm.getMembers().removeListener(memberListener);
        // Reset preview when switching entities
        sourcePreviewCard.setVisible(false);
        sourcePreviewCard.setManaged(false);
        previewedMember = null;
    }

    private final ListChangeListener<MemberInfo> memberListener =
            c -> Platform.runLater(this::rebuild);

    // ── Rebuild ───────────────────────────────────────────────────────────────

    private void rebuild() {
        cardContainer.getChildren().clear();
        if (currentVm == null) { cardContainer.getChildren().add(emptyState()); return; }

        List<MemberInfo> items = applyFilter(activeFilter, new ArrayList<>(currentVm.getMembers()));
        if (items.isEmpty()) { cardContainer.getChildren().add(emptyState()); return; }

        for (int i = 0; i < items.size(); i++) {
            Node card = buildMemberCard(items.get(i));
            cardContainer.getChildren().add(card);
            animateFadeIn(card, i * 35L);
        }
    }

    // ── Filter Bar ────────────────────────────────────────────────────────────

    private Node buildFilterBar(List<MemberInfo> members) {
        long totalMethods = members.stream().filter(m -> m.getKind() == MemberKind.METHOD || m.getKind() == MemberKind.FUNCTION).count();
        long totalCtors   = members.stream().filter(m -> m.getKind() == MemberKind.CONSTRUCTOR).count();
        long pub          = members.stream().filter(MemberInfo::isPublic).count();
        long priv         = members.stream().filter(m -> m.getModifiers().contains(ModifierKind.PRIVATE)).count();
        long empty        = members.stream().filter(m -> m.getStatementCount() == 0).count();

        filterChips.clear();
        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        chips.setFillHeight(false);
        chips.getChildren().addAll(
                filterChip("All Objects",  members.size(), MethodFilter.ALL),
                filterChip("Methods",       totalMethods,   MethodFilter.METHODS_ONLY),
                filterChip("Constructors",  totalCtors,     MethodFilter.CONSTRUCTORS_ONLY),
                filterChip("Public",        pub,            MethodFilter.PUBLIC),
                filterChip("Private",       priv,           MethodFilter.PRIVATE),
                filterChip("Empty",         empty,          MethodFilter.EMPTY)
        );
        filterChips.forEach(b -> b.getStyleClass().remove("filter-chip-active"));
        if (!filterChips.isEmpty()) filterChips.get(0).getStyleClass().add("filter-chip-active");

        VBox bar = new VBox(12, chips);
        bar.setPadding(new Insets(16, 20, 12, 20));
        bar.getStyleClass().add("method-filter-bar");
        return bar;
    }

    private Button filterChip(String label, long count, MethodFilter filter) {
        Button btn = new Button(label + "  " + count);
        btn.getStyleClass().add("filter-chip");
        filterChips.add(btn);
        btn.setOnAction(e -> {
            activeFilter = filter;
            filterChips.forEach(b -> b.getStyleClass().remove("filter-chip-active"));
            btn.getStyleClass().add("filter-chip-active");
            rebuild();
        });
        return btn;
    }

    // ── Method Intelligence Card ───────────────────────────────────────────────

    private Node buildMemberCard(MemberInfo m) {
        boolean isConstructor = m.getKind() == MemberKind.CONSTRUCTOR;

        VBox card = new VBox(0);
        card.getStyleClass().add("method-card");

        // ── Header ──────────────────────────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.getStyleClass().add("method-card-header");

        Label kindBadge = new Label(isConstructor ? "CTOR" : kindLabel(m.getKind()));
        kindBadge.getStyleClass().addAll("method-kind-badge", isConstructor ? "badge-ctor" : "badge-method");

        VBox sigBox = new VBox(3);
        HBox.setHgrow(sigBox, Priority.ALWAYS);
        Label nameLabel = new Label(buildParamStr_signature(m));
        nameLabel.getStyleClass().add("method-name");
        nameLabel.setWrapText(true);
        String returnStr = isConstructor ? "void" : (m.getReturnType() != null && !m.getReturnType().isBlank() ? m.getReturnType() : "void");
        Label returnLabel = new Label("↩  " + returnStr);
        returnLabel.getStyleClass().add("method-return");
        sigBox.getChildren().addAll(nameLabel, returnLabel);

        // Right stats
        VBox statsBox = new VBox(4);
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        String vis = m.getModifiers().stream()
                .filter(mod -> mod == ModifierKind.PUBLIC || mod == ModifierKind.PRIVATE || mod == ModifierKind.PROTECTED)
                .map(mod -> mod.name().toLowerCase()).findFirst().orElse("pkg");
        Label visBadge = new Label(vis.toUpperCase());
        visBadge.getStyleClass().addAll("visibility-badge", "vis-" + vis);
        HBox metrics = new HBox(6);
        metrics.setAlignment(Pos.CENTER_RIGHT);
        metrics.getChildren().addAll(
                miniMetric("STMTS", String.valueOf(m.getStatementCount())),
                miniMetric("LINES", String.valueOf(m.getEndLine() - m.getStartLine() + 1)),
                miniMetric("DEPS",  String.valueOf(m.getCalledMembers() == null ? 0 : m.getCalledMembers().size()))
        );
        statsBox.getChildren().addAll(visBadge, metrics);

        // Preview toggle button
        Button[] previewBtnRef = new Button[1];
        Button previewBtn = new Button("⊞");
        previewBtn.getStyleClass().add("preview-toggle-btn");
        previewBtnRef[0] = previewBtn;
        previewBtn.setTooltip(new Tooltip("View source body"));

        // Expand button for details
        Button expandBtn = new Button("▸");
        expandBtn.getStyleClass().add("expand-btn");

        header.getChildren().addAll(kindBadge, sigBox, statsBox, previewBtn, expandBtn);

        // ── Expanded Details ─────────────────────────────────────────────
        VBox details = buildExpandedDetails(m);
        details.setVisible(false);
        details.setManaged(false);

        expandBtn.setOnAction(e -> {
            boolean expanding = !details.isVisible();
            details.setVisible(expanding);
            details.setManaged(expanding);
            expandBtn.setText(expanding ? "▾" : "▸");
            expandBtn.getStyleClass().remove(expanding ? "expand-btn" : "expand-btn-active");
            expandBtn.getStyleClass().add(expanding ? "expand-btn-active" : "expand-btn");
            if (expanding) animateFadeIn(details, 0);
        });

        // Preview button shows source body card at top
        previewBtn.setOnAction(e -> {
            if (previewedMember == m) {
                // Toggle off
                hideSourcePreview();
                previewBtn.getStyleClass().remove("preview-toggle-btn-active");
                previewBtn.getStyleClass().add("preview-toggle-btn");
            } else {
                // Deactivate previous preview button if any
                cardContainer.getChildren().forEach(node -> {
                    if (node instanceof VBox cardNode) {
                        cardNode.getChildren().stream()
                                .filter(n -> n instanceof HBox)
                                .findFirst()
                                .ifPresent(hb -> ((HBox) hb).getChildren().stream()
                                        .filter(n -> n instanceof Button && n != previewBtnRef[0])
                                        .forEach(n -> {
                                            n.getStyleClass().remove("preview-toggle-btn-active");
                                            n.getStyleClass().add("preview-toggle-btn");
                                        }));
                    }
                });
                previewBtn.getStyleClass().remove("preview-toggle-btn");
                previewBtn.getStyleClass().add("preview-toggle-btn-active");
                showSourcePreview(m);
            }
        });

        // Click anywhere on header (not buttons) to open in editor
        header.setOnMouseClicked(e -> {
            if (e.getTarget() != expandBtn && e.getTarget() != previewBtn) {
                uiFeatures.openAndHighlight(
                        m.getName(), m.getStartLine(), m.getStartColumn(), m.getSourceFile()
                );
            }
        });

        card.setOnMouseEntered(e -> card.getStyleClass().add("method-card-hover"));
        card.setOnMouseExited(e -> card.getStyleClass().remove("method-card-hover"));
        card.getChildren().addAll(header, details);
        return card;
    }

    private String buildParamStr_signature(MemberInfo m) {
        if (m.getParameters() == null || m.getParameters().isEmpty()) return m.getName() + "()";
        return m.getName() + "(" + m.getParameters().stream()
                .map(p -> p.getType() + " " + p.getName())
                .collect(Collectors.joining(", ")) + ")";
    }

    // ── Expanded Details ──────────────────────────────────────────────────────

    private VBox buildExpandedDetails(MemberInfo m) {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(0, 16, 16, 16));
        panel.getStyleClass().add("method-expanded");

        Separator sep = new Separator();
        sep.getStyleClass().add("method-sep");
        panel.getChildren().add(sep);

        if (m.getParameters() != null && !m.getParameters().isEmpty()) {
            panel.getChildren().add(detailSection("PARAMETERS",
                    m.getParameters().stream().map(p -> p.getType() + "  " + p.getName()).collect(Collectors.toList()),
                    "param-item"));
        }

        if (m.getCalledMembers() != null && !m.getCalledMembers().isEmpty()) {
            panel.getChildren().add(buildDependenciesBlock(m.getCalledMembers()));
        }
        return panel;
    }

    private Node buildDependenciesBlock(List<MethodCallInfo> calls) {
        VBox block = new VBox(10);
        Label title = new Label("DEPENDENCIES");
        title.getStyleClass().add("detail-section-title");
        block.getChildren().add(title);
        var grouped = calls.stream().collect(Collectors.groupingBy(MethodCallInfo::getType));
        addCallGroup(block, "Internal", grouped.get(MethodCallInfo.CallType.INTERNAL), "dep-internal");
        addCallGroup(block, "External", grouped.get(MethodCallInfo.CallType.EXTERNAL), "dep-external");
        addCallGroup(block, "Library",  grouped.get(MethodCallInfo.CallType.LIBRARY),  "dep-library");
        return block;
    }

    private void addCallGroup(VBox parent, String groupName, List<MethodCallInfo> calls, String styleClass) {
        if (calls == null || calls.isEmpty()) return;
        VBox group = new VBox(6);
        Label groupLabel = new Label(groupName);
        groupLabel.getStyleClass().add("dep-group-label");
        group.getChildren().add(groupLabel);
        for (MethodCallInfo call : calls) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 8, 4, 8));
            row.getStyleClass().addAll("dep-row", styleClass);
            Label dot = new Label("◆"); dot.getStyleClass().add("dep-dot");
            Label methodName = new Label(call.getMethodName() + "()"); methodName.getStyleClass().add("dep-method");
            Label entity = new Label("· " + call.getCalledEntity()); entity.getStyleClass().add("dep-entity");
            row.getChildren().addAll(dot, methodName, entity);
            group.getChildren().add(row);
        }
        parent.getChildren().add(group);
    }

    private Node detailSection(String title, List<String> items, String itemStyle) {
        VBox section = new VBox(8);
        Label titleLabel = new Label(title); titleLabel.getStyleClass().add("detail-section-title");
        section.getChildren().add(titleLabel);
        for (String item : items) {
            HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(5, 10, 5, 10));
            row.getStyleClass().add("detail-item-row");
            Label dot = new Label("▸"); dot.getStyleClass().add("detail-dot");
            Label text = new Label(item); text.getStyleClass().addAll("detail-item-text", itemStyle);
            row.getChildren().addAll(dot, text);
            section.getChildren().add(row);
        }
        return section;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Label miniMetric(String key, String value) {
        Label l = new Label(key + " " + value);
        l.getStyleClass().add("mini-metric");
        return l;
    }

    private Node emptyState() {
        VBox box = new VBox(12); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(60));
        Label icon = new Label("◈"); icon.getStyleClass().add("empty-icon");
        Label msg = new Label("No members match the selected filter"); msg.getStyleClass().add("empty-msg");
        msg.setTextAlignment(TextAlignment.CENTER);
        box.getChildren().addAll(icon, msg);
        return box;
    }

    private void animateFadeIn(Node node, long delayMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(280), node);
        ft.setFromValue(0); ft.setToValue(1.0); ft.setDelay(Duration.millis(delayMs));
        ft.play();
    }

    private String kindLabel(MemberKind kind) {
        return switch (kind) { case FUNCTION -> "FN"; case PROPERTY -> "PROP"; default -> "MTH"; };
    }

    private List<MemberInfo> applyFilter(MethodFilter filter, List<MemberInfo> members) {
        return switch (filter) {
            case METHODS_ONLY      -> members.stream().filter(m -> m.getKind() == MemberKind.METHOD || m.getKind() == MemberKind.FUNCTION).toList();
            case CONSTRUCTORS_ONLY -> members.stream().filter(m -> m.getKind() == MemberKind.CONSTRUCTOR).toList();
            case PUBLIC            -> members.stream().filter(MemberInfo::isPublic).toList();
            case PRIVATE           -> members.stream().filter(m -> m.getModifiers().contains(ModifierKind.PRIVATE)).toList();
            case PROTECTED         -> members.stream().filter(m -> m.getModifiers().contains(ModifierKind.PROTECTED)).toList();
            case EMPTY             -> members.stream().filter(m -> m.getStatementCount() == 0).toList();
            case ALL               -> new ArrayList<>(members);
        };
    }

    private void updatePreviewSizing(String text) {

        int lineCount = text.isBlank()
                ? MIN_PREVIEW_LINES
                : text.split("\n", -1).length;

        int visibleLines = Math.max(
                MIN_PREVIEW_LINES,
                Math.min(MAX_PREVIEW_LINES, lineCount)
        );

        double height = visibleLines * LINE_HEIGHT;

        sourceArea.setPrefHeight(height);

        sourceArea.setMinHeight(Region.USE_PREF_SIZE);

        sourceArea.setMaxHeight(
                MAX_PREVIEW_LINES * LINE_HEIGHT
        );
    }
}
