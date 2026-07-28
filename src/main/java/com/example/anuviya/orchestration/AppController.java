package com.example.anuviya.orchestration;

import com.example.anuviya.frontend.FrontendRegistry;
import com.example.anuviya.context.DependencyGraph;
import com.example.anuviya.context.AnalysisContextManager;
import com.example.anuviya.context.DefaultAnalysisContextFactory;
import com.example.anuviya.orchestration.incremental.EntityViewModelBuilder;
import com.example.anuviya.context.state.AppState;

/**
 * Controller linking UI / File events to the backend Engine components.
 * Completely decoupled from JavaFX UI packages.
 */
public class AppController {

    private final FrontendRegistry registry;
    private final AnalysisEngine analysisEngine;
    private final EntityViewModelBuilder viewModelBuilder;
    private final AnalysisContextManager analysisContextManager;

    public AppController() {
        this(new AppState(
            context -> {},
            context -> {},
            context -> {},
            context -> {},
            context -> {}
        ));
    }

    public AppController(AppState appState) {
        this.registry = new FrontendRegistry();
        
        DependencyGraph graph = new DependencyGraph();
        this.viewModelBuilder = new EntityViewModelBuilder(graph);
        this.analysisContextManager = new AnalysisContextManager(appState);

        com.example.anuviya.endpoint.EndpointDiscovererRegistry endpointRegistry = new com.example.anuviya.endpoint.EndpointDiscovererRegistry();
        endpointRegistry.registerAll(
                new com.example.anuviya.endpoint.discoverer.spring.SpringBootEndpointDiscoverer()
        );
        com.example.anuviya.endpoint.EndpointDiscoveryEngine endpointEngine = new com.example.anuviya.endpoint.EndpointDiscoveryEngine(endpointRegistry);

        this.analysisEngine = new AnalysisEngine(
                this.registry,
                this.viewModelBuilder,
                endpointEngine,
                this.analysisContextManager,
                new DefaultAnalysisContextFactory()
        );
    }

    public FrontendRegistry getRegistry() {
        return registry;
    }

    public AnalysisEngine getEngine() {
        return analysisEngine;
    }

    public EntityViewModelBuilder getViewModelBuilder() {
        return viewModelBuilder;
    }

    public AnalysisContextManager getAnalysisContextManager() {
        return analysisContextManager;
    }
}
