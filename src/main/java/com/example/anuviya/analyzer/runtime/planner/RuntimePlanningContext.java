package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.runtime.*;
import com.example.anuviya.model.endpoint.ServiceEndpoint;
import java.util.*;

/**
 * Transient context containing the working state of the planner pipeline.
 */
public final class RuntimePlanningContext {
    private boolean enabled = true;
    private final List<ServiceEndpoint> serviceEndpoints = new ArrayList<>();
    private AuthenticationDescriptor authenticationDescriptor = new AuthenticationDescriptor(false, null);
    private final List<ScenarioDefinition> scenarioDefinitions = new ArrayList<>();
    private final List<RuntimeDiagnostics.DiagnosticEntry> diagnostics = new ArrayList<>();
    private final Map<String, Object> attributes = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<ServiceEndpoint> getServiceEndpoints() {
        return serviceEndpoints;
    }

    public void addServiceEndpoints(Collection<ServiceEndpoint> endpoints) {
        this.serviceEndpoints.addAll(endpoints);
    }

    public AuthenticationDescriptor getAuthenticationDescriptor() {
        return authenticationDescriptor;
    }

    public void setAuthenticationDescriptor(AuthenticationDescriptor authenticationDescriptor) {
        this.authenticationDescriptor = authenticationDescriptor;
    }

    public List<ScenarioDefinition> getScenarioDefinitions() {
        return scenarioDefinitions;
    }

    public void addScenarioDefinition(ScenarioDefinition scenario) {
        this.scenarioDefinitions.add(scenario);
    }

    public List<RuntimeDiagnostics.DiagnosticEntry> getDiagnostics() {
        return diagnostics;
    }

    public void addDiagnostic(RuntimeDiagnostics.DiagnosticEntry diagnostic) {
        this.diagnostics.add(diagnostic);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
