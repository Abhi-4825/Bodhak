package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager that holds all Analysis Report section states and coordinates their updates
 * when a new AnalysisContext is produced.
 */
public class AnalysisReportState {

    private final ExecutiveSummaryState executiveSummaryState = new ExecutiveSummaryState();
    private final ProjectClassificationState projectClassificationState = new ProjectClassificationState();
    private final ProjectSurfacesState projectSurfacesState = new ProjectSurfacesState();
    private final FrameworkDetectionState frameworkDetectionState = new FrameworkDetectionState();
    private final NamespaceOverviewState namespaceOverviewState = new NamespaceOverviewState();
    private final EntityMetricsState entityMetricsState = new EntityMetricsState();
    private final CompilationUnitOverviewState compilationUnitOverviewState = new CompilationUnitOverviewState();
    private final ReferenceDatabaseSummaryState referenceDatabaseSummaryState = new ReferenceDatabaseSummaryState();
    private final DependencyOverviewState dependencyOverviewState = new DependencyOverviewState();
    private final ArchitectureSummaryState architectureSummaryState = new ArchitectureSummaryState();
    private final RiskHotspotsState riskHotspotsState = new RiskHotspotsState();
    private final DiagnosticsSummaryState diagnosticsSummaryState = new DiagnosticsSummaryState();
    private final CompilerPipelineState compilerPipelineState = new CompilerPipelineState();
    private final AiReadinessSummaryState aiReadinessSummaryState = new AiReadinessSummaryState();
    private final SemanticFlowState semanticFlowState = new SemanticFlowState();
    
    // We will add the other 12 states here as we create them
    private final List<AnalysisReportSection> sections = new ArrayList<>();

    private final javafx.beans.property.ObjectProperty<AnalysisContext> analysisContext = new javafx.beans.property.SimpleObjectProperty<>();

    public AnalysisReportState() {
        sections.add(executiveSummaryState);
        sections.add(projectClassificationState);
        sections.add(projectSurfacesState);
        sections.add(frameworkDetectionState);
        sections.add(namespaceOverviewState);
        sections.add(entityMetricsState);
        sections.add(compilationUnitOverviewState);
        sections.add(referenceDatabaseSummaryState);
        sections.add(dependencyOverviewState);
        sections.add(architectureSummaryState);
        sections.add(riskHotspotsState);
        sections.add(diagnosticsSummaryState);
        sections.add(compilerPipelineState);
        sections.add(aiReadinessSummaryState);
        sections.add(semanticFlowState);
    }

    public void update(AnalysisContext context) {
        analysisContext.set(context);
        if (context == null) return;
        
        for (AnalysisReportSection section : sections) {
            section.update(context);
        }
    }

    public javafx.beans.property.ObjectProperty<AnalysisContext> analysisContextProperty() {
        return analysisContext;
    }

    public ExecutiveSummaryState getExecutiveSummaryState() {
        return executiveSummaryState;
    }

    public ProjectClassificationState getProjectClassificationState() {
        return projectClassificationState;
    }

    public ProjectSurfacesState getProjectSurfacesState() {
        return projectSurfacesState;
    }

    public FrameworkDetectionState getFrameworkDetectionState() {
        return frameworkDetectionState;
    }

    public NamespaceOverviewState getNamespaceOverviewState() {
        return namespaceOverviewState;
    }

    public EntityMetricsState getEntityMetricsState() {
        return entityMetricsState;
    }

    public CompilationUnitOverviewState getCompilationUnitOverviewState() {
        return compilationUnitOverviewState;
    }

    public ReferenceDatabaseSummaryState getReferenceDatabaseSummaryState() {
        return referenceDatabaseSummaryState;
    }

    public DependencyOverviewState getDependencyOverviewState() {
        return dependencyOverviewState;
    }

    public ArchitectureSummaryState getArchitectureSummaryState() {
        return architectureSummaryState;
    }

    public RiskHotspotsState getRiskHotspotsState() {
        return riskHotspotsState;
    }

    public DiagnosticsSummaryState getDiagnosticsSummaryState() {
        return diagnosticsSummaryState;
    }

    public CompilerPipelineState getCompilerPipelineState() {
        return compilerPipelineState;
    }

    public AiReadinessSummaryState getAiReadinessSummaryState() {
        return aiReadinessSummaryState;
    }

    public SemanticFlowState getSemanticFlowState() {
        return semanticFlowState;
    }
}
