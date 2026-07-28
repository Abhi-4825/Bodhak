package com.example.anuviya.ui.analysisReport;

import com.example.anuviya.ui.analysisReport.executive.ExecutiveOverviewCard;
import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import javafx.geometry.Insets;
import javafx.scene.layout.*;

/**
 * Main layout container for the Analysis Report dashboard.
 *
 * Bento-grid structure changes dynamically depending on width:
 * 
 * - WIDE Mode (> 1150px):
 *   - Row 1: Executive Banner (HBox of 7 metric tiles)
 *   - Row 2: Classification (4/12 weight) + Framework Detection (8/12 weight)
 *   - Row 3: Namespace (7/12 weight) + [Semantic Flow + Project Health] (5/12 weight)
 *   - Row 4: Risk Hotspots (6/12 weight) + Live Diagnostics (6/12 weight)
 *   - Row 5: Compiler Pipeline (Full Width)
 * 
 * - MEDIUM Mode (800px to 1150px):
 *   - Row 1: Executive Banner (2 Rows: 4 tiles / 3 tiles)
 *   - Row 2: Classification + Framework Detection (Side-by-side HBox)
 *   - Row 3: Namespace Overview (Full Width VBox)
 *   - Row 4: Semantic Flow + Project Health (Side-by-side HBox)
 *   - Row 5: Risk Hotspots (Full Width VBox)
 *   - Row 6: Live Diagnostics (Full Width VBox)
 *   - Row 7: Compiler Pipeline (Full Width VBox)
 * 
 * - NARROW Mode (< 800px):
 *   - All cards stacked vertically (taking 100% width)
 */
public class DashboardGrid extends VBox {

    public enum LayoutMode { WIDE, MEDIUM, NARROW }

    private final AnalysisReportState state;
    private final java.util.function.Consumer<com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent> inspectHandler;

    // Layout elements cache
    private ExecutiveOverviewCard execBanner;
    private com.example.anuviya.ui.analysisReport.classification.ProjectClassificationCard classificationCard;
    private com.example.anuviya.ui.analysisReport.framework.FrameworkDetectionCard frameworkCard;
    private com.example.anuviya.ui.analysisReport.surface.ProjectSurfacesCard surfacesCard;
    private com.example.anuviya.ui.analysisReport.namespace.NamespaceOverviewCard namespaceCard;
    private com.example.anuviya.ui.analysisReport.reference.SemanticFlowCard semanticFlowCard;
    private com.example.anuviya.ui.analysisReport.reference.ProjectHealthCard healthCard;
    private com.example.anuviya.ui.analysisReport.entity.EntityMetricsCard entityMetricsCard;
    private com.example.anuviya.ui.analysisReport.compilationUnit.CompilationUnitOverviewCard compilationUnitCard;

    private com.example.anuviya.ui.analysisReport.risk.RiskHotspotsCard riskCard;
    private com.example.anuviya.ui.analysisReport.diagnostic.DiagnosticsSummaryCard diagnosticsCard;
    private com.example.anuviya.ui.analysisReport.compiler.CompilerPipelineCard pipelineCard;

    private LayoutMode currentMode = null;

    public DashboardGrid(AnalysisReportState state, java.util.function.Consumer<com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent> inspectHandler) {
        this.state = state;
        this.inspectHandler = inspectHandler;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("ar-dashboard-bg");
        setSpacing(16);
        setPadding(new Insets(24));
        setFillWidth(true);

        // Instantiate components once
        execBanner = new ExecutiveOverviewCard(state.getExecutiveSummaryState());
        classificationCard = new com.example.anuviya.ui.analysisReport.classification.ProjectClassificationCard(state.getProjectClassificationState());
        frameworkCard = new com.example.anuviya.ui.analysisReport.framework.FrameworkDetectionCard(state.getFrameworkDetectionState());
        surfacesCard = new com.example.anuviya.ui.analysisReport.surface.ProjectSurfacesCard(state.getProjectSurfacesState());
        namespaceCard = new com.example.anuviya.ui.analysisReport.namespace.NamespaceOverviewCard(state.getNamespaceOverviewState());
        semanticFlowCard = new com.example.anuviya.ui.analysisReport.reference.SemanticFlowCard(state.getSemanticFlowState());
        healthCard = new com.example.anuviya.ui.analysisReport.reference.ProjectHealthCard(state.getExecutiveSummaryState(), state.getDiagnosticsSummaryState());
        entityMetricsCard = new com.example.anuviya.ui.analysisReport.entity.EntityMetricsCard(state.getEntityMetricsState());
        compilationUnitCard = new com.example.anuviya.ui.analysisReport.compilationUnit.CompilationUnitOverviewCard(state.getCompilationUnitOverviewState());
        riskCard = new com.example.anuviya.ui.analysisReport.risk.RiskHotspotsCard(state.getRiskHotspotsState());
        diagnosticsCard = new com.example.anuviya.ui.analysisReport.diagnostic.DiagnosticsSummaryCard(state.getDiagnosticsSummaryState());
        pipelineCard = new com.example.anuviya.ui.analysisReport.compiler.CompilerPipelineCard(state.getCompilerPipelineState());

        setupCardClicks();

        // Listen to width changes to adapt the bento structure
        widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double w = newWidth.doubleValue();
            LayoutMode targetMode;
            if (w > 1150) {
                targetMode = LayoutMode.WIDE;
            } else if (w > 800) {
                targetMode = LayoutMode.MEDIUM;
            } else {
                targetMode = LayoutMode.NARROW;
            }

            if (currentMode != targetMode) {
                currentMode = targetMode;
                rebuildLayout();
            }
        });

        // Trigger initial layout
        currentMode = LayoutMode.WIDE;
        rebuildLayout();
    }

    private void setupCardClicks() {
        setupCardClick(classificationCard, () -> new com.example.anuviya.ui.analysisReport.classification.ProjectClassificationInspector());
        setupCardClick(frameworkCard, () -> new com.example.anuviya.ui.analysisReport.framework.FrameworkInspector());
        setupCardClick(surfacesCard, () -> new com.example.anuviya.ui.analysisReport.surface.ProjectSurfacesInspector());
        setupCardClick(namespaceCard, () -> new com.example.anuviya.ui.analysisReport.namespace.NamespaceInspector());
        setupCardClick(entityMetricsCard, () -> new com.example.anuviya.ui.analysisReport.entity.EntityMetricsInspector());
        setupCardClick(compilationUnitCard, () -> new com.example.anuviya.ui.analysisReport.compilationUnit.CompilationUnitOverviewInspector());
        setupCardClick(semanticFlowCard, () -> new com.example.anuviya.ui.analysisReport.reference.SemanticFlowInspector());
        setupCardClick(healthCard, () -> new com.example.anuviya.ui.analysisReport.reference.ProjectHealthInspector());
        setupCardClick(riskCard, () -> new com.example.anuviya.ui.analysisReport.risk.RiskHotspotsInspector());
        setupCardClick(diagnosticsCard, () -> new com.example.anuviya.ui.analysisReport.diagnostic.DiagnosticsInspector());
        setupCardClick(pipelineCard, () -> new com.example.anuviya.ui.analysisReport.compiler.CompilerPipelineInspector());
    }

    private void setupCardClick(Region card, java.util.function.Supplier<com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent> inspectorSupplier) {
        card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
        card.setOnMouseClicked(event -> {
            javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
            while (target != null && target != card) {
                if (target instanceof javafx.scene.control.TableCell ||
                    target instanceof javafx.scene.control.ScrollBar ||
                    target instanceof javafx.scene.control.Button ||
                    target instanceof javafx.scene.control.TextField) {
                    return;
                }
                target = target.getParent();
            }
            if (inspectHandler != null) {
                inspectHandler.accept(inspectorSupplier.get());
            }
        });
    }

    private void rebuildLayout() {
        getChildren().clear();

        // Update banner's internal layout mode
        execBanner.setLayoutMode(currentMode);

        switch (currentMode) {
            case WIDE:
                buildWideLayout();
                break;
            case MEDIUM:
                buildMediumLayout();
                break;
            case NARROW:
                buildNarrowLayout();
                break;
        }
    }

    private void buildWideLayout() {
        // Row 2: Classification (4/12 weight) + Framework (8/12 weight)
        HBox row2 = buildRow(classificationCard, frameworkCard);
        HBox.setHgrow(classificationCard, Priority.ALWAYS);
        HBox.setHgrow(frameworkCard, Priority.ALWAYS);

        // Row 3: Surfaces + Dependency Overview
        HBox row3 = buildRow(surfacesCard);
        HBox.setHgrow(surfacesCard, Priority.ALWAYS);


        // Row 4: Namespace (7/12 weight) + [Semantic + Health] (5/12 weight)
        VBox rightCol4 = new VBox(16);
        rightCol4.setFillWidth(true);
        rightCol4.getChildren().addAll(semanticFlowCard, healthCard);
        HBox.setHgrow(rightCol4, Priority.ALWAYS);

        HBox row4 = buildRow(namespaceCard, rightCol4);
        HBox.setHgrow(namespaceCard, Priority.ALWAYS);

        // Row 5: Entity Metrics + Compilation Units
        HBox row5 = buildRow(entityMetricsCard, compilationUnitCard);
        HBox.setHgrow(entityMetricsCard, Priority.ALWAYS);
        HBox.setHgrow(compilationUnitCard, Priority.ALWAYS);

        // Row 6: Risk Hotspots
        HBox row6 = buildRow(riskCard);

        getChildren().addAll(execBanner, row2, row3, row4, row5, row6);
    }

    private void buildMediumLayout() {
        // Row 2: Classification + Framework (Side-by-side)
        HBox row2 = buildRow(classificationCard, frameworkCard);

        // Row 3: Surfaces + Dependency (Side-by-side)
        HBox row3 = buildRow(surfacesCard);

        // Row 4: Namespace Overview takes full width
        VBox row4 = new VBox(namespaceCard);
        VBox.setVgrow(namespaceCard, Priority.ALWAYS);

        // Row 5: Semantic Flow + Project Health (Side-by-side)
        HBox row5 = buildRow(semanticFlowCard, healthCard);

        // Row 6: Entity Metrics + Compilation Units (Side-by-side)
        HBox row6 = buildRow(entityMetricsCard, compilationUnitCard);

        // Row 7: Risk Hotspots
        VBox row7 = new VBox(riskCard);

        getChildren().addAll(execBanner, row2, row3, row4, row5, row6, row7);
    }

    private void buildNarrowLayout() {
        // All cards stacked vertically, taking full width
        getChildren().addAll(
                execBanner,
                classificationCard,
                frameworkCard,
                surfacesCard,

                namespaceCard,
                semanticFlowCard,
                healthCard,
                entityMetricsCard,
                compilationUnitCard,
                riskCard
        );
    }

    private HBox buildRow(Region... cells) {
        HBox row = new HBox(16);
        row.setFillHeight(true);
        for (Region cell : cells) {
            HBox.setHgrow(cell, Priority.ALWAYS);
            cell.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().add(cell);
        }
        return row;
    }
}
