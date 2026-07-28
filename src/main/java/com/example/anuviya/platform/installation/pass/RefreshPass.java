package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.environment.SystemEnvironmentManager;
import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;

public class RefreshPass implements InstallPass {
    @Override
    public String name() {
        return "Refresh Pass";
    }

    @Override
    public PassResult execute(InstallRequest request, InstallContext ctx) {
        try {
            SystemEnvironmentManager.getInstance().refresh();
            // Rebuild platform state
            com.example.anuviya.analyzer.ai.platform.AIPlatform.getInstance().rebuildState();
            return PassResult.SUCCESS;
        } catch (Exception e) {
            ctx.setErrorMessage("Refresh failed: " + e.getMessage());
            return PassResult.FAILED;
        }
    }
}
