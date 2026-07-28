package com.example.anuviya.context;
import com.example.anuviya.model.endpoint.ServiceEndpoint;

import java.util.Collections;
import java.util.List;

/**
 * Represents the complete API surface of the analysed project.
 * Contains all discovered endpoints.
 */
public class ApiSurface {
    
    private final List<ServiceEndpoint> endpoints;

    public ApiSurface(List<ServiceEndpoint> endpoints) {
        this.endpoints = endpoints == null ? Collections.emptyList() : List.copyOf(endpoints);
    }

    public List<ServiceEndpoint> getEndpoints() {
        return endpoints;
    }

    public boolean isEmpty() {
        return endpoints.isEmpty();
    }
}
