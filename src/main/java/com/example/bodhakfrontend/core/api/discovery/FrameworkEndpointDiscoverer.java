package com.example.bodhakfrontend.core.api.discovery;

import com.example.bodhakfrontend.core.api.model.ServiceEndpoint;
import com.example.bodhakfrontend.core.plugin.LanguagePluginRegistry;
import com.example.bodhakfrontend.core.projectType.detection.DetectionContext;

import java.util.List;

/**
 * Extension point for framework-specific endpoint discovery.
 * E.g., SpringBootEndpointDiscoverer, FastApiEndpointDiscoverer.
 */
public interface FrameworkEndpointDiscoverer {

    /**
     * Unique name of the framework being discovered (e.g., "spring-boot").
     */
    String frameworkName();

    /**
     * Discover and extract service endpoints from the project.
     *
     * @param context  The detection context containing entities and metadata
     * @param registry The language plugin registry, useful for fetching cached ASTs
     * @return A list of endpoints found for this framework
     */
    List<ServiceEndpoint> discover(DetectionContext context, LanguagePluginRegistry registry);
}
