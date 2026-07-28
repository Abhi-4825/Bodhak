package com.example.anuviya.platform.state;

import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;
import com.example.anuviya.platform.PackageState;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.environment.EnvironmentSnapshot;
import com.example.anuviya.workspace.model.WorkspaceState;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;
import java.util.*;

public final class PlatformState {
    private static final SimpleObjectProperty<PlatformState> current = new SimpleObjectProperty<>();

    private final EnvironmentSnapshot environment;
    private final List<ServicePackage> installedPackages;
    private final List<ServicePackage> availableUpdates;
    private final Map<String, PackageState> packageStates;
    private final List<AIProvider> activeProviders;
    private final List<ModelInfo> installedModels;
    private final AIProvider preferredProvider;
    private final WorkspaceState workspaceState;
    private final Instant refreshedAt;
    private final com.example.anuviya.model.runtime.RuntimeState runtimeState;

    public PlatformState(
        EnvironmentSnapshot environment,
        List<ServicePackage> installedPackages,
        List<ServicePackage> availableUpdates,
        Map<String, PackageState> packageStates,
        List<AIProvider> activeProviders,
        List<ModelInfo> installedModels,
        AIProvider preferredProvider,
        WorkspaceState workspaceState,
        Instant refreshedAt
    ) {
        this(environment, installedPackages, availableUpdates, packageStates, activeProviders, installedModels, preferredProvider, workspaceState, refreshedAt, com.example.anuviya.model.runtime.RuntimeState.initial());
    }

    public PlatformState(
        EnvironmentSnapshot environment,
        List<ServicePackage> installedPackages,
        List<ServicePackage> availableUpdates,
        Map<String, PackageState> packageStates,
        List<AIProvider> activeProviders,
        List<ModelInfo> installedModels,
        AIProvider preferredProvider,
        WorkspaceState workspaceState,
        Instant refreshedAt,
        com.example.anuviya.model.runtime.RuntimeState runtimeState
    ) {
        this.environment = environment;
        this.installedPackages = Collections.unmodifiableList(new ArrayList<>(installedPackages));
        this.availableUpdates = Collections.unmodifiableList(new ArrayList<>(availableUpdates));
        this.packageStates = Collections.unmodifiableMap(new HashMap<>(packageStates));
        this.activeProviders = Collections.unmodifiableList(new ArrayList<>(activeProviders));
        this.installedModels = Collections.unmodifiableList(new ArrayList<>(installedModels));
        this.preferredProvider = preferredProvider;
        this.workspaceState = workspaceState;
        this.refreshedAt = refreshedAt;
        this.runtimeState = runtimeState;
    }

    public static ReadOnlyObjectProperty<PlatformState> currentProperty() {
        return current;
    }

    public static PlatformState getCurrent() {
        return current.get();
    }

    public static void setCurrent(PlatformState newState) {
        if (javafx.application.Platform.isFxApplicationThread()) {
            current.set(newState);
        } else {
            javafx.application.Platform.runLater(() -> current.set(newState));
        }
    }

    public EnvironmentSnapshot getEnvironment() {
        return environment;
    }

    public List<ServicePackage> getInstalledPackages() {
        return installedPackages;
    }

    public List<ServicePackage> getAvailableUpdates() {
        return availableUpdates;
    }

    public Map<String, PackageState> getPackageStates() {
        return packageStates;
    }

    public List<AIProvider> getActiveProviders() {
        return activeProviders;
    }

    public List<ModelInfo> getInstalledModels() {
        return installedModels;
    }

    public AIProvider getPreferredProvider() {
        return preferredProvider;
    }

    public WorkspaceState getWorkspaceState() {
        return workspaceState;
    }

    public Instant getRefreshedAt() {
        return refreshedAt;
    }

    public com.example.anuviya.model.runtime.RuntimeState getRuntimeState() {
        return runtimeState;
    }
}
