package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.PackageCategory;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstallationPass implements InstallPass {
    @Override
    public String name() {
        return "Installation Pass";
    }

    @Override
    public PassResult execute(InstallRequest request, InstallContext ctx) {
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(request.packageId());
        if (!pkgOpt.isPresent()) {
            ctx.setErrorMessage("Package not found in registry: " + request.packageId());
            return PassResult.FAILED;
        }

        ServicePackage pkg = pkgOpt.get();
        if (pkg.category() == PackageCategory.AI_MODEL) {
            return PassResult.SUCCESS; // model installation is a pull trigger (handled in download)
        }

        String tempFilePath = ctx.get("tempFile");
        if (tempFilePath == null) {
            ctx.setErrorMessage("Installer file path missing from context.");
            return PassResult.FAILED;
        }

        String osName = getNormalizedOsName();
        ServicePackage.InstallerDef instDef = pkg.installerByOs().get(osName);
        if (instDef == null) {
            ctx.setErrorMessage("No installer defined for operating system: " + osName);
            return PassResult.FAILED;
        }

        try {
            List<String> command = new ArrayList<>();
            command.add(tempFilePath);
            if (instDef.silentArgs() != null) {
                command.addAll(instDef.silentArgs());
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            // On Windows, silent setup might need a clean context or just run and wait
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            // Delete temp file after execution
            try {
                new File(tempFilePath).delete();
            } catch (Exception e) {
                // Ignore delete failures
            }

            if (exitCode != 0) {
                ctx.setErrorMessage("Installer process returned exit code: " + exitCode);
                return PassResult.FAILED;
            }

            return PassResult.SUCCESS;
        } catch (Exception e) {
            ctx.setErrorMessage("Installation execution failed: " + e.getMessage());
            return PassResult.FAILED;
        }
    }

    private String getNormalizedOsName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "windows";
        if (os.contains("mac")) return "mac";
        return "linux";
    }
}
