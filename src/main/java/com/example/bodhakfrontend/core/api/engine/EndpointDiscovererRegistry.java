package com.example.bodhakfrontend.core.api.engine;

import com.example.bodhakfrontend.core.api.discovery.FrameworkEndpointDiscoverer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry holding all available framework endpoint discoverers.
 */
public class EndpointDiscovererRegistry {
    
    private final List<FrameworkEndpointDiscoverer> discoverers = new CopyOnWriteArrayList<>();

    public void register(FrameworkEndpointDiscoverer discoverer) {
        Objects.requireNonNull(discoverer, "discoverer must not be null");
        discoverers.add(discoverer);
    }

    public void registerAll(FrameworkEndpointDiscoverer... discoverers) {
        for (FrameworkEndpointDiscoverer d : discoverers) {
            register(d);
        }
    }

    public List<FrameworkEndpointDiscoverer> allDiscoverers() {
        return List.copyOf(discoverers);
    }
}
