package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.PackageCategory;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Optional;

public class VerificationPass implements InstallPass {
    @Override
    public String name() {
        return "Verification Pass";
    }

    @Override
    public PassResult execute(InstallRequest request, InstallContext ctx) {
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(request.packageId());
        if (!pkgOpt.isPresent()) {
            ctx.setErrorMessage("Package not found in registry: " + request.packageId());
            return PassResult.FAILED;
        }

        ServicePackage pkg = pkgOpt.get();
        if (pkg.category() != PackageCategory.AI_PROVIDER) {
            return PassResult.SUCCESS; // model verified via runtime
        }

        String tempFilePath = ctx.get("tempFile");
        if (tempFilePath == null) {
            ctx.setErrorMessage("Downloaded file path missing from context.");
            return PassResult.FAILED;
        }

        String osName = getNormalizedOsName();
        ServicePackage.InstallerDef instDef = pkg.installerByOs().get(osName);
        if (instDef == null) {
            ctx.setErrorMessage("No installer defined for operating system: " + osName);
            return PassResult.FAILED;
        }

        String expectedSha = instDef.sha256();
        if (expectedSha == null || expectedSha.isEmpty() || expectedSha.startsWith("00000000")) {
            // Checksum missing/ignored in config
            return PassResult.SUCCESS;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(tempFilePath)) {
                byte[] byteArray = new byte[8192];
                int bytesCount;
                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            String calculatedSha = sb.toString();

            if (!calculatedSha.equalsIgnoreCase(expectedSha)) {
                ctx.setErrorMessage("Integrity check failed. Expected SHA-256: " + expectedSha + ", Calculated: " + calculatedSha);
                return PassResult.FAILED;
            }
            return PassResult.SUCCESS;
        } catch (Exception e) {
            ctx.setErrorMessage("Verification error: " + e.getMessage());
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
