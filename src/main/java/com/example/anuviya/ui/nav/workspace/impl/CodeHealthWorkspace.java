package com.example.anuviya.ui.nav.workspace.impl;

import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.context.GraphSnapshot;
import com.example.anuviya.ui.nav.workspace.Workspace;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Persistent workspace for the CODE HEALTH tab.
 *
 * <p>Builds the dashboard content once and updates metric labels in-place when
 * a new engine is provided.  The root {@link ScrollPane} + container structure
 * is never replaced, so scroll position is preserved across navigation.
 */
public class CodeHealthWorkspace implements Workspace {

    // ── Persistent root ───────────────────────────────────────────────────────
    private final ScrollPane root;
    private final StackPane contentHolder;

    // ── Guard ─────────────────────────────────────────────────────────────────
    private AnalysisEngine lastEngine;

    public CodeHealthWorkspace() {
        contentHolder = new StackPane();
        contentHolder.setStyle("-fx-background-color: #0e1415;");

        root = new ScrollPane(contentHolder);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: #0e1415; -fx-background: #0e1415;");
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Show empty state until a project is loaded
        contentHolder.getChildren().add(buildContent(null));
    }

    // =========================================================================
    // ── Workspace API ─────────────────────────────────────────────────────────
    // =========================================================================

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        if (engine == lastEngine) return;
        lastEngine = engine;

        // Replace content in-place — the ScrollPane root is never touched
        contentHolder.getChildren().setAll(buildContent(engine));
    }

    // =========================================================================
    // ── UI construction ───────────────────────────────────────────────────────
    // =========================================================================

    private Node buildContent(AnalysisEngine engine) {
        VBox page = new VBox(24);
        page.setPadding(new Insets(40, 60, 40, 60));
        page.setStyle("-fx-background-color: #0e1415;");

        page.getChildren().add(pageHeader(
                "Code Health",
                "Issues, severity distribution & growth analysis findings"
        ));

        if (engine == null) {
            page.getChildren().add(noProjectCard());
            return page;
        }

        // ── Quick health indicators ──────────────────────────────────────────
        GraphSnapshot snap     = engine.getGraphSnapshot();
        int cycleCount         = snap.circularGroups().size();
        String healthStatus    = cycleCount == 0 ? "GOOD" : "NEEDS ATTENTION";
        String healthColor     = cycleCount == 0 ? "#8bfd91" : "#ffd54f";

        com.example.anuviya.context.AnalysisContext context = engine.getAnalysisContextManager().getCurrentContext();
        HBox healthRow = new HBox(16);
        healthRow.getChildren().addAll(
            metricCard("Health",  healthStatus,                 healthColor),
            metricCard("Cycles",  String.valueOf(cycleCount),
                cycleCount == 0 ? "#8bfd91" : "#ff8a80"),
            metricCard("Classes",
                String.valueOf(context.getEntities().size()), "#4bf6ff")
        );
        healthRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        page.getChildren().add(healthRow);

        // ── Entity quality distribution ──────────────────────────────────────
        var entities = context.getEntities();
        int total        = entities.size();
//        int withWarnings = (int) entities.stream().filter(e -> !e.getWarnings().isEmpty()).count();
//        int godClasses   = (int) entities.stream().filter(e ->
//                e.getIssueType() != null &&
//                e.getIssueType().contains(com.example.bodhak.model.entity.IssueType.GOD_CLASS)).count();
//        int healthy      = total - withWarnings;

        HBox qualityRow = new HBox(16);
        qualityRow.getChildren().addAll(
            metricCard("Total Entities", String.valueOf(total),        "#d2a8ff")
//                .
        );
        qualityRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        page.getChildren().add(qualityRow);

        // ── Coming-soon feature cards ─────────────────────────────────────────
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
            comingSoonCard("📊  Severity Distribution",
                    "Issue breakdown by CRITICAL / HIGH / MEDIUM / LOW coming soon."),
            comingSoonCard("🗂  Category Distribution",
                    "Coupling, Cohesion, Complexity, Security categories coming soon."),
            comingSoonCard("🏆  Top Risk Entities",
                    "Classes ranked by composite risk score coming soon."),
            comingSoonCard("🌱  Growth Analysis",
                    "Long-term scalability risks detected by GrowthAnalysisService.")
        );
        page.getChildren().add(flow);

        return page;
    }

    // =========================================================================
    // ── Shared UI building blocks ─────────────────────────────────────────────
    // =========================================================================

    private Node pageHeader(String title, String subtitle) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(0, 0, 20, 0));

        Label t = new Label("⬡  " + title);
        t.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label s = new Label(subtitle);
        s.setStyle("-fx-font-size: 13px; -fx-text-fill: #849494;");

        box.getChildren().addAll(t, s);
        return box;
    }

    private VBox metricCard(String label, String value, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("""
            -fx-background-color: #161d1e;
            -fx-background-radius: 8;
            -fx-border-color: #242b2c;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            """);

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #849494; -fx-font-weight: bold;");

        card.getChildren().addAll(v, l);
        return card;
    }

    private Node comingSoonCard(String title, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setStyle("""
            -fx-background-color: #161d1e;
            -fx-background-radius: 8;
            -fx-border-color: #242b2c;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            """);

        Label h = new Label(title);
        h.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #849494;");

        Label d = new Label(description);
        d.setStyle("-fx-font-size: 12px; -fx-text-fill: #566465;");
        d.setWrapText(true);

        Label badge = new Label("Coming Soon");
        badge.setStyle("""
            -fx-background-color: rgba(75,246,255,0.08);
            -fx-text-fill: #4bf6ff;
            -fx-font-size: 10px;
            -fx-font-weight: bold;
            -fx-padding: 4 10;
            -fx-background-radius: 12;
            """);

        card.getChildren().addAll(h, d, badge);
        return card;
    }

    private Node noProjectCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setStyle("""
            -fx-background-color: #161d1e;
            -fx-background-radius: 8;
            -fx-border-color: #242b2c;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            """);

        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #2f3637;");

        Label l = new Label("Load a project first using Select Folder to see code health analytics.");
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494;");
        l.setWrapText(true);

        card.getChildren().addAll(icon, l);
        return card;
    }
}
