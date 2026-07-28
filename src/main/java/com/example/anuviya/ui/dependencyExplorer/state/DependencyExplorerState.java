package com.example.anuviya.ui.dependencyExplorer.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class DependencyExplorerState {

    private final ExecutiveSummaryState executiveSummaryState = new ExecutiveSummaryState();
    private final EntityBrowserState entityBrowserState = new EntityBrowserState();
    private final DependencyGraphState dependencyGraphState = new DependencyGraphState();
    private final SelectedEntityState selectedEntityState = new SelectedEntityState();
    private final PathState pathState = new PathState();
    private final BreakdownState breakdownState = new BreakdownState();
    private final HealthState healthState = new HealthState();
    private final CyclesState cyclesState = new CyclesState();

    private final ObjectProperty<EntityInfo> selectedEntity = new SimpleObjectProperty<>();
    private final ObjectProperty<AnalysisContext> analysisContext = new SimpleObjectProperty<>();
    private final ObjectProperty<GraphMode> graphMode = new SimpleObjectProperty<>(GraphMode.ENTITY);

    public DependencyExplorerState() {
        // When selected entity or graph mode or context changes, update entity-dependent states
        selectedEntity.addListener((obs, old, newVal) -> updateEntityDependentStates());
        graphMode.addListener((obs, old, newVal) -> updateEntityDependentStates());
        analysisContext.addListener((obs, old, newVal) -> {
            if (newVal != null) {
                executiveSummaryState.update(newVal);
                entityBrowserState.update(newVal);
                cyclesState.update(newVal);
                
                // Keep selected entity valid or default to first
                EntityInfo current = selectedEntity.get();
                if (current == null || newVal.findEntity(current.getEntityName()).isEmpty()) {
                    if (!newVal.getEntities().isEmpty()) {
                        selectedEntity.set(newVal.getEntities().get(0));
                    } else {
                        selectedEntity.set(null);
                    }
                } else {
                    // Update reference to the new context's EntityInfo
                    selectedEntity.set(newVal.findEntity(current.getEntityName()).orElse(null));
                }
            } else {
                selectedEntity.set(null);
                cyclesState.clear();
            }
        });
    }

    public void update(AnalysisContext context) {
        analysisContext.set(context);
    }

    private void updateEntityDependentStates() {
        AnalysisContext context = analysisContext.get();
        EntityInfo entity = selectedEntity.get();
        GraphMode mode = graphMode.get();

        if (context == null || entity == null) {
            dependencyGraphState.clear();
            selectedEntityState.clear();
            pathState.clear();
            breakdownState.clear();
            healthState.clear();
            return;
        }

        dependencyGraphState.update(context, entity, mode);
        selectedEntityState.update(context, entity);
        pathState.update(context, entity);
        breakdownState.update(context, entity);
        healthState.update(context, entity);
    }

    public ObjectProperty<EntityInfo> selectedEntityProperty() { return selectedEntity; }
    public ObjectProperty<AnalysisContext> analysisContextProperty() { return analysisContext; }
    public ObjectProperty<GraphMode> graphModeProperty() { return graphMode; }

    public ExecutiveSummaryState getExecutiveSummaryState() { return executiveSummaryState; }
    public EntityBrowserState getEntityBrowserState() { return entityBrowserState; }
    public DependencyGraphState getDependencyGraphState() { return dependencyGraphState; }
    public SelectedEntityState getSelectedEntityState() { return selectedEntityState; }
    public PathState getPathState() { return pathState; }
    public BreakdownState getBreakdownState() { return breakdownState; }
    public HealthState getHealthState() { return healthState; }
    public CyclesState getCyclesState() { return cyclesState; }
}
