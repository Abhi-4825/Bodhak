package com.example.bodhakfrontend.core.api.engine;

import com.example.bodhakfrontend.core.api.discovery.FrameworkEndpointDiscoverer;
import com.example.bodhakfrontend.core.api.model.ApiSurface;
import com.example.bodhakfrontend.core.api.model.ServiceEndpoint;
import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;
import com.example.bodhakfrontend.core.projectType.detection.DetectionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Engine that runs all registered Endpoint Discoverers to build the final ApiSurface.
 */
public class EndpointDiscoveryEngine {

    private final EndpointDiscovererRegistry registry;

    public EndpointDiscoveryEngine(EndpointDiscovererRegistry registry) {
        this.registry = registry;
    }

    /**
     * Executes endpoint discovery across all frameworks and aggregates the results.
     */
    public ApiSurface analyze(DetectionContext context, LanguagePluginRegistry pluginRegistry) {
        List<ServiceEndpoint> allEndpoints = new ArrayList<>();
        
        for (FrameworkEndpointDiscoverer discoverer : registry.allDiscoverers()) {
            List<ServiceEndpoint> endpoints = discoverer.discover(context, pluginRegistry);
            if (endpoints != null) {
                allEndpoints.addAll(endpoints);
            }
        }
        
        return new ApiSurface(allEndpoints);
    }
}
