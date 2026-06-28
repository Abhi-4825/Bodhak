package com.example.bodhakfrontend.ui.nav;

import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * The Overview Panel shown on the right side of the IDE view (Exploration Mode).
 * Displays a quick project summary and entry points.
 */
public class OverviewPanel {

    private final StackPane container;

    public OverviewPanel() {
        this.container = new StackPane();
        this.container.setStyle("-fx-background-color: #161d1e;");
        update(null); // Show empty state initially
    }

    private AnalysisEngine lastEngine;

    public StackPane getRoot() {
        return container;
    }

    public void clearCache() {
        lastEngine = null;
    }

    /**
     * Refreshes the panel with live engine data.
     */
    public void update(AnalysisEngine engine) {
        if (engine == lastEngine && !container.getChildren().isEmpty()) {
            return;
        }
        lastEngine = engine;
        Node view = buildOverview(engine);

        ScrollPane scroll = new ScrollPane(view);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        container.getChildren().setAll(scroll);
    }

    private Node buildOverview(AnalysisEngine engine) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #161d1e;");

        root.getChildren().add(tabHeader("⬡  Overview", "Project summary and quick health metrics"));

        if (engine == null) {
            root.getChildren().add(noProjectCard());
            return root;
        }

        AnalysisContext ctx = engine.getAnalysisContextManager().getCurrentContext();
        ProjectInfo info = ctx.getProjectInfo();

        // ── Summary metrics row ──────────────────────────────────────────────
        HBox metricsRow = new HBox(10);

        String projectFlavor = "—";
        if (info.entryPointInfo() != null) {
            projectFlavor = info.entryPointInfo().getFlavorSummary();
        }

        int langCount = info.languageCountMap() != null ? info.languageCountMap().size() : 0;

        metricsRow.getChildren().addAll(
            metricCard("Project Type",  projectFlavor,                          "#4bf6ff"),
            metricCard("Classes",       String.valueOf(info.totalEntities()), "#8bfd91"),
            metricCard("Languages",     String.valueOf(langCount),               "#ffd54f"),
            metricCard("Files",         String.valueOf(info.knownFiles() != null ? info.knownFiles().size() : 0), "#d2a8ff")
        );
        metricsRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        root.getChildren().add(metricsRow);

        // ── Health metrics row ───────────────────────────────────────────────
//        int healthy = (int) ctx.getEntities().stream().filter(e -> e.getWarnings().isEmpty()).count();
//        int withWarnings = (int) ctx.getEntities().stream().filter(e -> !e.getWarnings().isEmpty()).count();
//        int godClasses = (int) ctx.getEntities().stream().filter(e -> e.getIssueType() != null &&
//                e.getIssueType().contains(com.example.bodhakfrontend.core.model.entity.IssueType.GOD_CLASS)).count();

        HBox healthRow = new HBox(10);
//        healthRow.getChildren().addAll(
//            metricCard("Healthy",      String.valueOf(healthy),       "#8bfd91"),
//            metricCard("With Warnings",String.valueOf(withWarnings),  "#ffd54f"),
//            metricCard("God Classes",  String.valueOf(godClasses),           "#ff8a80")
//        );
        healthRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        root.getChildren().add(healthRow);

        // ── Languages detected ───────────────────────────────────────────────
        if (info.languageCountMap() != null && !info.languageCountMap().isEmpty()) {
            VBox langCard = sectionCard("🌐  Languages Detected");
            info.languageCountMap().forEach((lang, paths) ->
                langCard.getChildren().add(chipLabel(lang + "  (" + paths.size() + " files)", "#d2a8ff", "rgba(210,168,255,0.10)"))
            );
            root.getChildren().add(langCard);
        }

        // ── Entry points ─────────────────────────────────────────────────────
        if (info.entryPointInfo() != null) {
            EntryPointInfo ep = info.entryPointInfo();
            VBox epCard = sectionCard("🚀  Entry Points");

            if (ep.getPrimaryEntry() != null) {
                epCard.getChildren().add(chipLabel(
                    "★ " + ep.getPrimaryEntry().entityName() + " [" + ep.getPrimaryEntry().label() + "]",
                    "#4bf6ff", "rgba(75,246,255,0.08)"
                ));
            }
            ep.getSecondaryEntries().stream().limit(6).forEach(e ->
                epCard.getChildren().add(rowLabel("→  " + e.entityName() + "  [" + e.kind() + "]"))
            );
            root.getChildren().add(epCard);
        }

        // ── Namespaces / Packages ─────────────────────────────────────────────
        if (ctx.getNamespaces() != null && !ctx.getNamespaces().isEmpty()) {
            VBox pkgCard = sectionCard("📦  Namespaces / Packages  (" + ctx.getNamespaces().size() + ")");
            ctx.getNamespaces().keySet().stream().sorted().limit(12)
                .forEach(pkg -> pkgCard.getChildren().add(rowLabel(pkg)));
            root.getChildren().add(pkgCard);
        }

        return root;
    }

    // =========================================================================
    // ── Shared UI building blocks ─────────────────────────────────────────────
    // =========================================================================

    private Node tabHeader(String title, String subtitle) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 0, 8, 0));
        box.setStyle("-fx-border-color: transparent transparent #2f3637 transparent; -fx-border-width: 0 0 1 0;");

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label s = new Label(subtitle);
        s.setStyle("-fx-font-size: 11px; -fx-text-fill: #849494;");

        box.getChildren().addAll(t, s);
        return box;
    }

    private VBox metricCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color: #090f10; -fx-background-radius: 8;");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: #849494;");

        card.getChildren().addAll(v, l);
        return card;
    }

    private VBox sectionCard(String heading) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: #090f10; -fx-background-radius: 8;");

        Label h = new Label(heading);
        h.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        card.getChildren().add(h);
        return card;
    }

    private Node noProjectCard() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #090f10; -fx-background-radius: 8;");

        Label l = new Label("Load a project first using Select Folder to see data here.");
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #849494;");
        l.setWrapText(true);

        card.getChildren().add(l);
        return card;
    }

    private Label rowLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #849494; -fx-font-size: 11px;");
        l.setOnMouseEntered(e -> l.setStyle("-fx-text-fill: #dde4e5; -fx-font-size: 11px;"));
        l.setOnMouseExited(e -> l.setStyle("-fx-text-fill: #849494; -fx-font-size: 11px;"));
        return l;
    }

    private Label chipLabel(String text, String color, String bg) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: %s; -fx-text-fill: %s; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11px;"
            .formatted(bg, color));
        return l;
    }
}
