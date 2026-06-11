package com.example.bodhakfrontend.ui.nav;

import com.example.bodhakfrontend.core.api.model.ApiSurface;
import com.example.bodhakfrontend.core.api.model.ServiceEndpoint;
import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.engine.GraphSnapshot;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.ui.dashboard.architecture.ArchitectureDashboard;
import com.example.bodhakfrontend.ui.dashboard.architecture.model.ArchitectureGraphState;
import com.example.bodhakfrontend.ui.performance.PerformanceTestingPanel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Generates full-screen analytical dashboards for the non-Overview tabs.
 */
import com.example.bodhakfrontend.ui.dashboard.architecture.model.ArchitectureWorkspaceState;

public class DashboardManager {

    private final ArchitectureGraphState architectureGraphState;
    private final ArchitectureWorkspaceState architectureWorkspaceState;
    private final java.util.Map<NavTab, Node> viewCache = new java.util.HashMap<>();
    private AnalysisEngine lastEngine;

    public DashboardManager() {
        this.architectureGraphState = new ArchitectureGraphState();
        this.architectureWorkspaceState = new ArchitectureWorkspaceState();
    }

    public void clearCache() {
        architectureGraphState.clear();
        architectureWorkspaceState.clear();
        viewCache.clear();
    }

    /**
     * Builds and returns the dashboard view for the given tab.
     */
    public Node getView(NavTab tab, AnalysisEngine engine) {
        if (engine != lastEngine) {
            lastEngine = engine;
            clearCache();
        }

        return viewCache.computeIfAbsent(tab, t -> {
            Node content = switch (t) {
                case OVERVIEW     -> new Region(); // Should never happen, handled by App.java
                case ARCHITECTURE -> new ArchitectureDashboard(engine, architectureGraphState, architectureWorkspaceState).build();
                case CODE_HEALTH  -> buildCodeHealth(engine);
                case PERFORMANCE  -> buildPerformance(engine);
            };

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: #0e1415; -fx-background: #0e1415;");
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            return scroll;
        });
    }

    // =========================================================================
    // ── CODE HEALTH TAB ──────────────────────────────────────────────────────
    // =========================================================================

    private Node buildCodeHealth(AnalysisEngine engine) {
        VBox root = new VBox(24);
        root.setPadding(new Insets(40, 60, 40, 60));
        root.setStyle("-fx-background-color: #0e1415;");

        root.getChildren().add(pageHeader("Code Health", "Issues, severity distribution & growth analysis findings"));

        if (engine == null) {
            root.getChildren().add(noProjectCard());
            return root;
        }

        // ── Quick health indicators ──────────────────────────────────────────
        GraphSnapshot snap = engine.getGraphSnapshot();
        int cycleCount = snap.circularGroups().size();
        String healthStatus = cycleCount == 0 ? "GOOD" : "NEEDS ATTENTION";
        String healthColor  = cycleCount == 0 ? "#8bfd91" : "#ffd54f";

        HBox healthRow = new HBox(16);
        healthRow.getChildren().addAll(
            metricCard("Health",       healthStatus,              healthColor),
            metricCard("Cycles",       String.valueOf(cycleCount),
                cycleCount == 0 ? "#8bfd91" : "#ff8a80"),
            metricCard("Classes",
                String.valueOf(engine.getProjectInfo().getEntities().size()), "#4bf6ff")
        );
        healthRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        root.getChildren().add(healthRow);

        // ── Coming-soon section for rich health features ─────────────────────
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
            comingSoonCard("📊  Severity Distribution", "Issue breakdown by CRITICAL / HIGH / MEDIUM / LOW coming soon."),
            comingSoonCard("🗂  Category Distribution", "Coupling, Cohesion, Complexity, Security categories coming soon."),
            comingSoonCard("🏆  Top Risk Entities", "Classes ranked by composite risk score coming soon."),
            comingSoonCard("🌱  Growth Analysis", "Long-term scalability risks detected by GrowthAnalysisService.")
        );
        root.getChildren().add(flow);

        return root;
    }

    // =========================================================================
    // ── PERFORMANCE TAB ──────────────────────────────────────────────────────
    // =========================================================================

    private Node buildPerformance(AnalysisEngine engine) {
        VBox root = new VBox(24);
        root.setPadding(new Insets(40, 60, 40, 60));
        root.setStyle("-fx-background-color: #0e1415;");

        root.getChildren().add(pageHeader("Performance", "API surface, endpoint inventory & Gatling load testing"));

        if (engine == null) {
            root.getChildren().add(noProjectCard());
            return root;
        }

        // ── API Surface stats ────────────────────────────────────────────────
        ApiSurface api = engine.getApiSurface();
        int endpointCount = (api != null) ? api.getEndpoints().size() : 0;
        long getCount     = (api != null) ? api.getEndpoints().stream()
                                .filter(e -> "GET".equalsIgnoreCase(e.httpMethod())).count() : 0;
        long postCount    = (api != null) ? api.getEndpoints().stream()
                                .filter(e -> "POST".equalsIgnoreCase(e.httpMethod())).count() : 0;

        HBox apiRow = new HBox(16);
        apiRow.getChildren().addAll(
            metricCard("Endpoints",  String.valueOf(endpointCount), "#4bf6ff"),
            metricCard("GET",        String.valueOf(getCount),       "#8bfd91"),
            metricCard("POST",       String.valueOf(postCount),      "#ffd54f")
        );
        apiRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        root.getChildren().add(apiRow);

        // ── Endpoint inventory ───────────────────────────────────────────────
        VBox endpointCard = sectionCard("🌐  Discovered Endpoints");
        if (api == null || api.getEndpoints().isEmpty()) {
            endpointCard.getChildren().add(mutedLabel("No endpoints discovered. Ensure project is a Spring Boot REST API."));
        } else {
            api.getEndpoints().forEach(ep -> {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);

                Label method = new Label(ep.httpMethod());
                method.setStyle("""
                    -fx-font-family: 'JetBrains Mono', monospace;
                    -fx-font-size: 11px;
                    -fx-font-weight: bold;
                    -fx-padding: 3 10;
                    -fx-background-radius: 4;
                    -fx-background-color: %s;
                    -fx-text-fill: %s;
                    """.formatted(
                        "GET".equalsIgnoreCase(ep.httpMethod()) ? "rgba(139,253,145,0.15)" : "rgba(255,213,79,0.15)",
                        "GET".equalsIgnoreCase(ep.httpMethod()) ? "#8bfd91" : "#ffd54f"
                    ));

                Label path = new Label(ep.fullPath());
                path.setStyle("-fx-text-fill: #dde4e5; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 13px;");

                Label source = new Label(ep.sourceEntityName());
                source.setStyle("-fx-text-fill: #849494; -fx-font-size: 11px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(method, path, spacer, source);
                row.setPadding(new Insets(6, 0, 6, 0));
                endpointCard.getChildren().add(row);
            });
        }
        root.getChildren().add(endpointCard);

        // ── Load Test panel ──────────────────────────────────────────────────
        VBox ltHeader = sectionCard("⚡  Gatling Load Test");
        ltHeader.getChildren().add(mutedLabel("Configure and run load tests directly from this panel."));
        root.getChildren().add(ltHeader);

        try {
            Node perfPanel = new PerformanceTestingPanel().build(api);
            root.getChildren().add(perfPanel);
        } catch (Exception ex) {
            root.getChildren().add(mutedLabel("Load test panel unavailable: " + ex.getMessage()));
        }

        return root;
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
        card.setStyle("-fx-background-color: #161d1e; -fx-background-radius: 8; -fx-border-color: #242b2c; -fx-border-radius: 8; -fx-border-width: 1;");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #849494; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");

        card.getChildren().addAll(v, l);
        return card;
    }

    private VBox sectionCard(String heading) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #161d1e; -fx-background-radius: 8; -fx-border-color: #242b2c; -fx-border-radius: 8; -fx-border-width: 1;");

        Label h = new Label(heading);
        h.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        card.getChildren().add(h);
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
        card.setStyle("-fx-background-color: #161d1e; -fx-background-radius: 8; -fx-border-color: #242b2c; -fx-border-radius: 8; -fx-border-width: 1;");

        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #2f3637;");

        Label l = new Label("Load a project first using Select Folder to see analytical dashboards.");
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494;");
        l.setWrapText(true);

        card.getChildren().addAll(icon, l);
        return card;
    }

    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #566465;");
        l.setWrapText(true);
        return l;
    }
}
