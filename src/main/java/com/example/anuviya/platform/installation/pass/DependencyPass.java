package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.io.File;
import java.util.Optional;

public class DependencyPass implements InstallPass {
    @Override
    public String name() {
        return "Dependency Pass";
    }

    @Override
    public PassResult execute(InstallRequest request, InstallContext ctx) {
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(request.packageId());
        if (!pkgOpt.isPresent()) {
            ctx.setErrorMessage("Package not found in registry: " + request.packageId());
            return PassResult.FAILED;
        }

        ServicePackage pkg = pkgOpt.get();
        for (ServicePackage.DependencyDef dep : pkg.dependencies()) {
            // Check if dependency is installed
            boolean isInstalled = checkDependencyInstalled(dep.id());
            if (!isInstalled) {
                ctx.setErrorMessage("Missing required dependency: " + dep.id() + " (" + dep.versionConstraint() + ")");
                return PassResult.FAILED;
            }
        }
        return PassResult.SUCCESS;
    }

    private boolean checkDependencyInstalled(String depId) {
        if ("ollama".equalsIgnoreCase(depId)) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null) {
                File exe = new File(localAppData, "Programs/Ollama/ollama.exe");
                if (exe.exists()) {
                    return true;
                }
            }
            try {
                Process process = Runtime.getRuntime().exec("ollama --version");
                process.waitFor();
                return process.exitValue() == 0;
            } catch (Exception e) {
                return false;
            }
        }
        // Future extensions
        return false;
    }
}
