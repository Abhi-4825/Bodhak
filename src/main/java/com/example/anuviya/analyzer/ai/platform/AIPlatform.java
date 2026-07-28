package com.example.anuviya.analyzer.ai.platform;

import com.example.anuviya.analyzer.ai.platform.model.AIProfileDef;
import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;
import com.example.anuviya.analyzer.ai.platform.provider.ProviderRegistry;
import com.example.anuviya.analyzer.ai.platform.session.AISession;
import com.example.anuviya.analyzer.ai.platform.session.SessionManager;
import com.example.anuviya.platform.PackageState;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.discovery.DiscoveryService;
import com.example.anuviya.platform.environment.EnvironmentSnapshot;
import com.example.anuviya.platform.environment.SystemEnvironmentManager;
import com.example.anuviya.platform.installation.InstallationService;
import com.example.anuviya.platform.registry.LocalPackageDb;
import com.example.anuviya.platform.registry.domain.AIProfileRegistry;
import com.example.anuviya.platform.registry.domain.PackageRegistry;
import com.example.anuviya.platform.state.PlatformState;

import java.time.Instant;
import java.util.*;

public class AIPlatform {
    private static final AIPlatform INSTANCE = new AIPlatform();
    private String activeModelName;

    private AIPlatform() {}

    public static AIPlatform getInstance() {
        return INSTANCE;
    }

    public synchronized String getActiveModelName() {
        return activeModelName;
    }

    public synchronized void setActiveModelName(String modelName) {
        this.activeModelName = modelName;
        rebuildState();
    }

    public synchronized void rebuildState() {
        EnvironmentSnapshot env = SystemEnvironmentManager.getInstance().getSnapshot();
        if (env == null) {
            SystemEnvironmentManager.getInstance().refresh();
            env = SystemEnvironmentManager.getInstance().getSnapshot();
        }

        List<ServicePackage> installed = new ArrayList<>();
        Map<String, PackageState> states = new HashMap<>();

        // Load package registry packages
        List<ServicePackage> allPackages = PackageRegistry.getInstance().all();
        for (ServicePackage pkg : allPackages) {
            boolean isInst = LocalPackageDb.isInstalled(pkg.id());
            if (isInst) {
                installed.add(pkg);
                states.put(pkg.id(), PackageState.READY);
            } else {
                PackageState activeState = InstallationService.getInstance().getPackageState(pkg.id());
                states.put(pkg.id(), activeState != null ? activeState : PackageState.NOT_INSTALLED);
            }
        }

        List<ModelInfo> installedModels = new ArrayList<>();
        List<AIProvider> activeProviders = new ArrayList<>();
        AIProvider preferred = null;

        // Discovered local providers
        List<AIProvider> discovered = DiscoveryService.getInstance().discoverInstalledProviders();
        for (AIProvider prov : discovered) {
            activeProviders.add(prov);
            installedModels.addAll(prov.installedModels());
        }

        if (ProviderRegistry.getInstance().getActive() != null) {
            preferred = ProviderRegistry.getInstance().getActive();
        }

        String currentActiveModel = "None";
        if (!installedModels.isEmpty()) {
            if (activeModelName != null) {
                Optional<ModelInfo> match = installedModels.stream()
                    .filter(m -> m.displayName().equalsIgnoreCase(activeModelName) || m.id().equalsIgnoreCase(activeModelName))
                    .findFirst();
                if (match.isPresent()) {
                    currentActiveModel = match.get().displayName();
                } else {
                    currentActiveModel = installedModels.get(0).displayName();
                    activeModelName = currentActiveModel;
                }
            } else {
                currentActiveModel = installedModels.get(0).displayName();
                activeModelName = currentActiveModel;
            }
        } else {
            activeModelName = null;
        }

        com.example.anuviya.workspace.model.PlatformStatus wsPlatformStatus = new com.example.anuviya.workspace.model.PlatformStatus(
            preferred != null ? preferred.info().displayName() : "None",
            preferred != null ? preferred.runtimeStatus() : com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.OFFLINE,
            currentActiveModel,
            true
        );

        List<com.example.anuviya.workspace.model.WorkspaceProject> recent = com.example.anuviya.workspace.WorkspaceManager.getInstance().getRecentProjects();
        List<com.example.anuviya.workspace.model.WorkspaceProject> pinned = com.example.anuviya.workspace.WorkspaceManager.getInstance().getProjectManager() != null
            ? com.example.anuviya.workspace.WorkspaceManager.getInstance().getProjectManager().getPinnedProjects()
            : List.of();

        Map<UUID, com.example.anuviya.workspace.model.ProjectIntelligence> intelMap = new HashMap<>();
        if (com.example.anuviya.workspace.WorkspaceManager.getInstance().getSessionManager() != null) {
            for (com.example.anuviya.workspace.model.WorkspaceProject p : recent) {
                com.example.anuviya.workspace.WorkspaceManager.getInstance().getSessionManager().getIntelligence(p.id()).ifPresent(intel -> intelMap.put(p.id(), intel));
            }
        }

        com.example.anuviya.workspace.model.WorkspaceState wsState = new com.example.anuviya.workspace.model.WorkspaceState(
            com.example.anuviya.workspace.WorkspaceManager.getInstance().getCurrentProject(),
            recent,
            pinned,
            intelMap,
            wsPlatformStatus
        );

        PlatformState newState = new PlatformState(
            env,
            installed,
            new ArrayList<>(), // available updates
            states,
            activeProviders,
            installedModels,
            preferred,
            wsState,
            Instant.now()
        );
        PlatformState.setCurrent(newState);
    }

    public AISession openSession(String profileId) throws AIException {
        return openSession(profileId, null);
    }

    public AISession openSession(String profileId, String modelName) throws AIException {
        Optional<AIProfileDef> profileOpt = AIProfileRegistry.getInstance().get(profileId);
        if (!profileOpt.isPresent()) {
            throw new AIException("AI Profile not registered: " + profileId);
        }

        AIProfileDef profile = profileOpt.get();
        AIProvider activeProvider = ProviderRegistry.getInstance().getActive();
        if (activeProvider == null) {
            throw new AIException("No active AI provider configured.");
        }

        if (activeProvider.runtimeStatus() != com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.READY) {
            throw new AIException("Active AI runtime provider (" + activeProvider.info().displayName() + ") is offline.");
        }

        // Resolve model matching capabilities or requested name
        List<ModelInfo> installedModels = activeProvider.installedModels();
        ModelInfo resolvedModel = null;

        if (modelName != null && !modelName.isEmpty()) {
            for (ModelInfo m : installedModels) {
                if (m.id().equalsIgnoreCase(modelName) || m.displayName().equalsIgnoreCase(modelName)) {
                    resolvedModel = m;
                    break;
                }
            }
        }

        // Fallback to first matching model matching capabilities
        if (resolvedModel == null) {
            for (ModelInfo model : installedModels) {
                if (model.capabilities().containsAll(profile.requiredCapabilities())) {
                    resolvedModel = model;
                    break;
                }
            }
        }

        if (resolvedModel == null) {
            throw new AIException("No installed model supports the required capabilities: " + profile.requiredCapabilities());
        }

        return SessionManager.getInstance().open(profile, activeProvider, resolvedModel);
    }
}
