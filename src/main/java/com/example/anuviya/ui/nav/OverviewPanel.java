package com.example.anuviya.ui.nav;

import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.model.project.ProjectInfo;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.project.ProjectRootInfo;
import com.example.anuviya.model.project.ProjectSurface;
import com.example.anuviya.model.project.RootCapability;
import com.example.anuviya.ui.ProjectAnalysis.state.ProjectAnalysisState;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

/**
 * The Overview Panel shown on the right side of the IDE view (Exploration Mode).
 * Displays a quick project summary and entry points. Built exactly once and reactively bound.
 */
public class OverviewPanel {

    private final StackPane container;
    private final ScrollPane scrollPane;
    private final VBox root;

    // Persistent labels in metrics row
    private final Label typeValueLabel = new Label("—");
    private final Label classValueLabel = new Label("—");
    private final Label langValueLabel = new Label("—");
    private final Label fileValueLabel = new Label("—");

    // Persistent list cards
    private final VBox langCard = new VBox(8);
    private final VBox epCard = new VBox(8);
    private final VBox pkgCard = new VBox(8);

    private ProjectAnalysisState state;

    public OverviewPanel() {
        this.container = new StackPane();
        this.container.setStyle("-fx-background-color: #161d1e;");

        // 1. Build persistent hierarchy once
        root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #161d1e;");
        root.getChildren().add(tabHeader("⬡  Overview", "Project summary and quick health metrics"));

        // Metrics row
        HBox metricsRow = new HBox(10);
        metricsRow.getChildren().addAll(
            metricCard("Project Type",  typeValueLabel,  "#4bf6ff"),
            metricCard("Classes",       classValueLabel, "#8bfd91"),
            metricCard("Languages",     langValueLabel,  "#ffd54f"),
            metricCard("Files",         fileValueLabel,  "#d2a8ff")
        );
        metricsRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        root.getChildren().add(metricsRow);

        // Sections
        langCard.setPadding(new Insets(14));
        langCard.setStyle("-fx-background-color: #090f10; -fx-background-radius: 8;");
        Label langHeading = new Label("🌐  Languages Detected");
        langHeading.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        langCard.getChildren().add(langHeading);
        root.getChildren().add(langCard);

        epCard.setPadding(new Insets(14));
        epCard.setStyle("-fx-background-color: #090f10; -fx-background-radius: 8;");
        Label epHeading = new Label("🚀  Project Surfaces");
        epHeading.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        epCard.getChildren().add(epHeading);
        root.getChildren().add(epCard);

        pkgCard.setPadding(new Insets(14));
        pkgCard.setStyle("-fx-background-color: #090f10; -fx-background-radius: 8;");
        Label pkgHeading = new Label("📦  Namespaces / Packages");
        pkgHeading.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        pkgCard.getChildren().add(pkgHeading);
        root.getChildren().add(pkgCard);

        scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        container.getChildren().setAll(scrollPane);
    }

    public StackPane getRoot() {
        return container;
    }

    /**
     * Binds this panel to the state. All updates happen reactively.
     */
    public void setAnalysisState(ProjectAnalysisState state) {
        this.state = state;

        // Bind metrics row values to state properties
        typeValueLabel.textProperty().bind(state.getProjectTypeState().primaryTypeProperty());
        
        classValueLabel.textProperty().bind(
            Bindings.size(state.getEntityMetricsState().getEntities()).asString()
        );
        
        langValueLabel.textProperty().bind(
            Bindings.size(state.getProjectSummary().getLanguages()).asString()
        );
        
        fileValueLabel.textProperty().bind(
            state.getProjectSummary().totalFilesProperty().asString()
        );

        // Listen to analysis context updates to refresh inner card lists dynamically
        state.analysisContextProperty().addListener((obs, oldCtx, newCtx) -> {
            if (newCtx != null) {
                Platform.runLater(() -> updateCardLists(newCtx));
            }
        });

        if (state.getAnalysisContext() != null) {
            updateCardLists(state.getAnalysisContext());
        }
    }

    /**
     * Compatibility fallback method.
     */
    public void update(AnalysisEngine engine) {
        if (engine == null) {
            return;
        }
        AnalysisContext ctx = engine.getAnalysisContextManager().getCurrentContext();
        if (ctx != null) {
            Platform.runLater(() -> updateCardLists(ctx));
        }
    }

    private void updateCardLists(AnalysisContext ctx) {
        ProjectInfo info = ctx.getProjectInfo();

        // 1. Languages detected list
        if (langCard.getChildren().size() > 1) {
            langCard.getChildren().subList(1, langCard.getChildren().size()).clear();
        }
        if (info.languageCountMap() != null) {
            info.languageCountMap().forEach((lang, paths) -> {
                Label label = chipLabel(lang + "  (" + paths.size() + " files)", "#d2a8ff", "rgba(210,168,255,0.10)");
                langCard.getChildren().add(label);
            });
        }

        // 2. Project Surfaces list
        if (epCard.getChildren().size() > 1) {
            epCard.getChildren().subList(1, epCard.getChildren().size()).clear();
        }
        if (info.projectRootInfo() != null) {
            ProjectRootInfo pri = info.projectRootInfo();
            List<ProjectSurface> surfaces = pri.surfaces();
            ProjectSurface primary = surfaces.stream()
                .filter(s -> s.capabilities().contains(RootCapability.EXECUTABLE) || s.capabilities().contains(RootCapability.API_PROVIDER))
                .findFirst()
                .orElse(surfaces.isEmpty() ? null : surfaces.get(0));

            if (primary != null) {
                epCard.getChildren().add(chipLabel(
                    "★ " + primary.symbol().name() + "  " + primary.capabilities().toString(),
                    "#4bf6ff", "rgba(75,246,255,0.08)"
                ));
            }
            surfaces.stream()
                .filter(e -> e != primary)
                .limit(6)
                .forEach(e -> epCard.getChildren().add(rowLabel("→  " + e.symbol().name() + "  " + e.capabilities().toString())));
        }

        // 3. Namespaces list
        if (pkgCard.getChildren().size() > 1) {
            pkgCard.getChildren().subList(1, pkgCard.getChildren().size()).clear();
        }
        if (ctx.getNamespaces() != null && !ctx.getNamespaces().isEmpty()) {
            // Update heading text dynamically
            Label heading = (Label) pkgCard.getChildren().get(0);
            heading.setText("📦  Namespaces / Packages  (" + ctx.getNamespaces().size() + ")");
            
            ctx.getNamespaces().keySet().stream().sorted().limit(12)
                .forEach(pkg -> pkgCard.getChildren().add(rowLabel(pkg)));
        }
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

    private VBox metricCard(String label, Label valueLabel, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color: #090f10; -fx-background-radius: 8;");

        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: #849494;");

        card.getChildren().addAll(valueLabel, l);
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
