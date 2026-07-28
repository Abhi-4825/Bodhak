package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;
import com.example.anuviya.platform.registry.LocalPackageDb;

public class RegistrationPass implements InstallPass {
    @Override
    public String name() {
        return "Registration Pass";
    }

    @Override
    public PassResult execute(InstallRequest request, InstallContext ctx) {
        try {
            LocalPackageDb.register(request.packageId(), request.version());
            return PassResult.SUCCESS;
        } catch (Exception e) {
            ctx.setErrorMessage("Registration failed: " + e.getMessage());
            return PassResult.FAILED;
        }
    }
}
