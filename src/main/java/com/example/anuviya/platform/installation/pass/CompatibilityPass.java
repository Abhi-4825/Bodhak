package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.environment.SystemEnvironmentManager;
import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.util.Optional;

public class CompatibilityPass implements InstallPass {
    @Override
    public String name() {
        return "Compatibility Pass";
    }

    @Override
    public PassResult execute(InstallRequest request, InstallContext ctx) {
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(request.packageId());
        if (!pkgOpt.isPresent()) {
            ctx.setErrorMessage("Package not found in registry: " + request.packageId());
            return PassResult.FAILED;
        }

        ServicePackage pkg = pkgOpt.get();
        double freeGb = SystemEnvironmentManager.getInstance().getSnapshot().disk().freeGb();
        
        // Disk Check
        if (pkg.sizeGb() > 0 && freeGb < pkg.sizeGb()) {
            ctx.setErrorMessage("Insufficient disk space. Required: " + pkg.sizeGb() + " GB, Free: " + freeGb + " GB");
            return PassResult.FAILED;
        }

        // Hardware warnings (RAM / VRAM) are handled via warning flags/overlays, this pass returns SUCCESS.
        return PassResult.SUCCESS;
    }
}
