package com.example.anuviya.analyzer.ai.platform;

import com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;
import com.example.anuviya.analyzer.ai.platform.provider.ProviderRegistry;
import com.example.anuviya.platform.environment.SystemEnvironmentManager;
import com.example.anuviya.platform.registry.RegistryManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AIBootstrapTask {
    public void execute() {
        // 1. Load Registry
        RegistryManager.getInstance().loadAll();

        // 2. Discover Providers
        ProviderRegistry.getInstance().discover();

        // 3. Check Auto Start Config
        boolean autoStart = getAutoStartPreference();

        // 4. Auto start active provider if offline
        AIProvider activeProvider = ProviderRegistry.getInstance().getActive();
        if (autoStart && activeProvider != null) {
            if (activeProvider.runtimeStatus() == RuntimeStatus.OFFLINE) {
                activeProvider.start();
            }
        }

        // 5. Initialize Live monitoring
        SystemEnvironmentManager.getInstance().startMonitoring();

        // 6. Build Initial Platform State
        AIPlatform.getInstance().rebuildState();
    }

    private boolean getAutoStartPreference() {
        String userHome = System.getProperty("user.home");
        File config = new File(userHome, ".gemini/antigravity/onboarding.properties");
        if (config.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(config)) {
                props.load(fis);
                String val = props.getProperty("ai.provider.autoStart");
                if (val != null) {
                    return Boolean.parseBoolean(val);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return true; // default true
    }
}
