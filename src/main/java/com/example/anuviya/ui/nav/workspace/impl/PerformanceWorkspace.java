package com.example.anuviya.ui.nav.workspace.impl;

import com.example.anuviya.context.ApiSurface;
import com.example.anuviya.model.endpoint.ServiceEndpoint;
import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.ui.nav.workspace.Workspace;
import com.example.anuviya.ui.performance.PerformanceTestingPanel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Persistent workspace for the PERFORMANCE tab.
 *
 * <h3>State preserved across navigation</h3>
 * <ul>
 *   <li>Endpoint inventory table</li>
 *   <li>Base URL, virtual users, ramp-up, duration fields in the load-test form</li>
 *   <li>Load-test output area contents</li>
 *   <li>Scroll position</li>
 * </ul>
 *
 * <p>The {@link PerformanceTestingPanel} instance is created once and reused.
 * When a new project is loaded (different engine), the top metric cards and
 * endpoint list are rebuilt, but the load-test panel itself is the same widget
 * so field values entered by the user are preserved.
 */
public class PerformanceWorkspace implements Workspace {

    // ── Persistent root ───────────────────────────────────────────────────────
    private final ScrollPane root;
    private final StackPane  contentHolder;

    // ── Load-test panel — created once, lives forever ─────────────────────────
    private PerformanceTestingPanel perfPanel;
    private Node                    perfPanelNode;

    // ── Guard ─────────────────────────────────────────────────────────────────
    private AnalysisEngine lastEngine;

    public PerformanceWorkspace() {
        contentHolder = new StackPane();
        contentHolder.setStyle("-fx-background-color: #0e1415;");

        root = new ScrollPane(contentHolder);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: #0e1415; -fx-background: #0e1415;");
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

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

        // Rebuild the static sections; reuse the persistent panel widget
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
                "Runtime Validation",
                "API surface, authentication profiles, scenario planners & runtime diagnostics"
        ));

        if (engine == null) {
            page.getChildren().add(noProjectCard());
            return page;
        }

        // ── API Surface stats ────────────────────────────────────────────────
        ApiSurface api      = engine.getApiSurface();
        int endpointCount   = (api != null) ? api.getEndpoints().size() : 0;
        long getCount       = (api != null) ? api.getEndpoints().stream()
                                .filter(e -> "GET".equalsIgnoreCase(e.httpMethod())).count() : 0;
        long postCount      = (api != null) ? api.getEndpoints().stream()
                                .filter(e -> "POST".equalsIgnoreCase(e.httpMethod())).count() : 0;

        HBox apiRow = new HBox(16);
        apiRow.getChildren().addAll(
            metricCard("Endpoints", String.valueOf(endpointCount), "#4bf6ff"),
            metricCard("GET",       String.valueOf(getCount),       "#8bfd91"),
            metricCard("POST",      String.valueOf(postCount),      "#ffd54f")
        );
        apiRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        page.getChildren().add(apiRow);

        // ── Endpoint inventory ───────────────────────────────────────────────
        VBox endpointCard = sectionCard("🌐  Discovered Endpoints");
        if (api == null || api.getEndpoints().isEmpty()) {
            endpointCard.getChildren().add(mutedLabel(
                    "No endpoints discovered. Ensure project is a Spring Boot REST API."));
        } else {
            api.getEndpoints().forEach(ep -> endpointCard.getChildren().add(buildEndpointRow(ep)));
        }
        page.getChildren().add(endpointCard);

        // ── Gatling load test panel header ───────────────────────────────────
        VBox ltHeader = sectionCard("⚡  Runtime Validation Engine");
        ltHeader.getChildren().add(mutedLabel(
                "Execute planning scenarios, verify environment snapshots, resolve authentications, and view diagnostic alerts."));
        page.getChildren().add(ltHeader);

        // ── Persistent load-test panel ───────────────────────────────────────
        // Build once; never recreate (preserves form fields + output)
        if (perfPanelNode == null) {
            perfPanel     = new PerformanceTestingPanel();
            perfPanelNode = perfPanel.build(api);
        }
        page.getChildren().add(perfPanelNode);

        return page;
    }

    // =========================================================================
    // ── Shared UI building blocks ─────────────────────────────────────────────
    // =========================================================================

    private HBox buildEndpointRow(ServiceEndpoint ep) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));

        String isGet    = "GET".equalsIgnoreCase(ep.httpMethod()) ? "rgba(139,253,145,0.15)" : "rgba(255,213,79,0.15)";
        String txtColor = "GET".equalsIgnoreCase(ep.httpMethod()) ? "#8bfd91" : "#ffd54f";

        Label method = new Label(ep.httpMethod());
        method.setStyle("""
            -fx-font-family: 'JetBrains Mono', monospace;
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-padding: 3 10;
            -fx-background-radius: 4;
            -fx-background-color: %s;
            -fx-text-fill: %s;
            """.formatted(isGet, txtColor));

        Label path = new Label(ep.fullPath());
        path.setStyle("-fx-text-fill: #dde4e5; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 13px;");

        Label source = new Label(ep.sourceEntityName());
        source.setStyle("-fx-text-fill: #849494; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(method, path, spacer, source);
        return row;
    }

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

    private VBox sectionCard(String heading) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: #161d1e;
            -fx-background-radius: 8;
            -fx-border-color: #242b2c;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            """);

        Label h = new Label(heading);
        h.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        card.getChildren().add(h);
        return card;
    }

    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #566465;");
        l.setWrapText(true);
        return l;
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

        Label l = new Label("Load a project first using Select Folder to see API analytics.");
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494;");
        l.setWrapText(true);

        card.getChildren().addAll(icon, l);
        return card;
    }
}
