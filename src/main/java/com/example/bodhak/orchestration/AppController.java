package com.example.bodhak.orchestration;

import com.example.bodhak.frontend.FrontendRegistry;
import com.example.bodhak.context.DependencyGraph;
import com.example.bodhak.context.AnalysisContextManager;
import com.example.bodhak.context.DefaultAnalysisContextFactory;
import com.example.bodhak.orchestration.incremental.EntityViewModelBuilder;
import com.example.bodhak.context.state.AppState;

import java.util.List;

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
        
        DependencyGraph graph = new DependencyGraph(this.registry);
        this.viewModelBuilder = new EntityViewModelBuilder(graph);
        this.analysisContextManager = new AnalysisContextManager(appState);

        com.example.bodhak.endpoint.EndpointDiscovererRegistry endpointRegistry = new com.example.bodhak.endpoint.EndpointDiscovererRegistry();
        endpointRegistry.registerAll(
                new com.example.bodhak.endpoint.discoverer.spring.SpringBootEndpointDiscoverer()
        );
        com.example.bodhak.endpoint.EndpointDiscoveryEngine endpointEngine = new com.example.bodhak.endpoint.EndpointDiscoveryEngine(endpointRegistry);

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
