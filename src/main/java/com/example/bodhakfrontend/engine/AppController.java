package com.example.bodhakfrontend.engine;

import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectorRegistry;
import com.example.bodhakfrontend.core.projectType.detectors.java.SpringBootDetector;
import com.example.bodhakfrontend.core.projectType.detectors.java.JavaDesktopDetector;
import com.example.bodhakfrontend.core.projectType.detectors.java.JavaCliDetector;
import com.example.bodhakfrontend.core.projectType.detectors.javascript.ReactDetector;
import com.example.bodhakfrontend.core.projectType.detectors.python.FastAPIDetector;
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

        // Wire the framework detector registry with built-in detectors.
        // To add a new framework: detectorRegistry.register(new KtorDetector()) — nothing else changes.
        FrameworkDetectorRegistry detectorRegistry = new FrameworkDetectorRegistry();
        detectorRegistry.registerAll(
                new SpringBootDetector(),
                new JavaDesktopDetector(),
                new JavaCliDetector(),
                new ReactDetector(),
                new FastAPIDetector()
        );

        // Wire Endpoint Discovery
        com.example.bodhakfrontend.core.api.engine.EndpointDiscovererRegistry endpointRegistry = new com.example.bodhakfrontend.core.api.engine.EndpointDiscovererRegistry();
        endpointRegistry.registerAll(
                new com.example.bodhakfrontend.core.api.detectors.java.SpringBootEndpointDiscoverer()
        );
        com.example.bodhakfrontend.core.api.engine.EndpointDiscoveryEngine endpointEngine = new com.example.bodhakfrontend.core.api.engine.EndpointDiscoveryEngine(endpointRegistry);

        this.analysisEngine = new AnalysisEngine(this.registry, this.viewModelBuilder, detectorRegistry, endpointEngine);
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
