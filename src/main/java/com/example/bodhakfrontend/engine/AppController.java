package com.example.bodhakfrontend.engine;

import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;
import com.example.bodhakfrontend.engine.incremental.EntityViewModelBuilder;

/**
 * Controller linking UI / File events to the backend Engine components.
 * Equivalent to the old ProjectContext.
 */
public class AppController {

    private final LanguagePluginRegistry registry;
    private final AnalysisEngine analysisEngine;
    private final EntityViewModelBuilder viewModelBuilder;

    public AppController() {
        this.registry = new LanguagePluginRegistry();
        // The JVM entrypoint will register plugins via registry.registerPlugin()
        
        // This is the dependency graph that serves the ViewModelBuilder
        DependencyGraph graph = new DependencyGraph(this.registry);
        this.viewModelBuilder = new EntityViewModelBuilder(graph);
        this.analysisEngine = new AnalysisEngine(this.registry, this.viewModelBuilder);
    }

    public LanguagePluginRegistry getRegistry() {
        return registry;
    }

    public AnalysisEngine getEngine() {
        return analysisEngine;
    }

    public EntityViewModelBuilder getViewModelBuilder() {
        return viewModelBuilder;
    }
}
