package com.example.anuviya.platform.installation;

import com.example.anuviya.platform.PackageState;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.installation.pass.*;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class InstallationService {
    private static final InstallationService INSTANCE = new InstallationService();
    private final Map<String, PackageState> packageStates = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "bodhak-installer-worker");
        thread.setDaemon(true);
        return thread;
    });

    private final List<InstallPass> passes = List.of(
        new DependencyPass(),
        new CompatibilityPass(),
        new DownloadPass(),
        new VerificationPass(),
        new InstallationPass(),
        new RegistrationPass(),
        new RefreshPass()
    );

    private InstallationService() {}

    public static InstallationService getInstance() {
        return INSTANCE;
    }

    public PackageState getPackageState(String packageId) {
        return packageStates.get(packageId);
    }

    public synchronized void install(String packageId, Consumer<Double> progressConsumer, Consumer<String> onComplete, Consumer<String> onFailure) {
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(packageId);
        if (!pkgOpt.isPresent()) {
            if (onFailure != null) onFailure.accept("Package not found in registry: " + packageId);
            return;
        }

        ServicePackage pkg = pkgOpt.get();
        packageStates.put(packageId, PackageState.CHECKING);
        com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();

        executor.submit(() -> {
            InstallContext ctx = new InstallContext(progressConsumer);
            InstallRequest request = new InstallRequest(pkg.id(), pkg.category(), pkg.version());
            boolean success = true;

            for (InstallPass pass : passes) {
                // Update state machine states based on pass
                updateMachineState(packageId, pass);

                InstallPass.PassResult result = pass.execute(request, ctx);
                if (result == InstallPass.PassResult.FAILED) {
                    success = false;
                    packageStates.put(packageId, PackageState.FAILED);
                    com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
                    if (onFailure != null) {
                        javafx.application.Platform.runLater(() -> onFailure.accept(ctx.getErrorMessage()));
                    }
                    break;
                }
            }

            if (success) {
                packageStates.put(packageId, PackageState.READY);
                com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
                if (onComplete != null) {
                    javafx.application.Platform.runLater(() -> onComplete.accept(packageId));
                }
            }
        });
    }

    private void updateMachineState(String packageId, InstallPass pass) {
        if (pass instanceof CompatibilityPass) {
            packageStates.put(packageId, PackageState.CHECKING);
        } else if (pass instanceof DownloadPass) {
            packageStates.put(packageId, PackageState.DOWNLOADING);
        } else if (pass instanceof VerificationPass) {
            packageStates.put(packageId, PackageState.VERIFYING);
        } else if (pass instanceof InstallationPass) {
            packageStates.put(packageId, PackageState.INSTALLING);
        } else if (pass instanceof RegistrationPass) {
            packageStates.put(packageId, PackageState.STARTING);
        }
        com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
    }
}
