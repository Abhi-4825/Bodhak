package com.example.anuviya.endpoint;
import com.example.anuviya.frontend.FrontendRegistry;

import com.example.anuviya.context.ApiSurface;
import com.example.anuviya.model.endpoint.ServiceEndpoint;
import com.example.anuviya.classification.detection.DetectionContext;

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
    public ApiSurface analyze(DetectionContext context, FrontendRegistry pluginRegistry) {
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
