package com.example.anuviya.platform.onboarding.flows.step;

import com.example.anuviya.analyzer.ai.platform.AIPlatform;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;
import com.example.anuviya.analyzer.ai.platform.provider.ProviderRegistry;
import com.example.anuviya.analyzer.ai.platform.session.AISession;
import com.example.anuviya.analyzer.ai.platform.session.SessionManager;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.environment.SystemEnvironmentManager;
import com.example.anuviya.platform.environment.model.NetworkStatus;
import com.example.anuviya.platform.installation.InstallationService;
import com.example.anuviya.platform.onboarding.OnboardingContext;
import com.example.anuviya.platform.onboarding.OnboardingStep;
import com.example.anuviya.platform.registry.domain.PackageRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class AISteps {

    public static class SystemCheckStep implements OnboardingStep {
        @Override
        public String id() { return "system-check"; }
        @Override
        public String displayName() { return "System Compatibility Check"; }
        @Override
        public StepResult execute(OnboardingContext ctx) {
            SystemEnvironmentManager.getInstance().refresh();
            var snapshot = SystemEnvironmentManager.getInstance().getSnapshot();
            
            if (snapshot.networkStatus() == NetworkStatus.OFFLINE) {
                ctx.put("error", "Your internet connection is offline. An active connection is required for setup.");
                return StepResult.FAILED;
            }
            if (snapshot.disk().freeGb() < 2.5) {
                ctx.put("error", "Insufficient disk space. At least 2.5 GB of free space is required.");
                return StepResult.FAILED;
            }
            return StepResult.SUCCESS;
        }
        @Override
        public String failureMessage(OnboardingContext ctx) {
            return ctx.get("error");
        }
    }

    public static class ProviderSelectStep implements OnboardingStep {
        @Override
        public String id() { return "provider-select"; }
        @Override
        public String displayName() { return "Select Provider"; }
        @Override
        public StepResult execute(OnboardingContext ctx) {
            // Default to ollama, can be overridden by wizard choice
            if (ctx.get("providerId") == null) {
                ctx.put("providerId", "ollama");
            }
            return StepResult.SUCCESS;
        }
        @Override
        public String failureMessage(OnboardingContext ctx) {
            return "No provider selected.";
        }
    }

    public static class ProviderInstallStep implements OnboardingStep {
        @Override
        public String id() { return "provider-install"; }
        @Override
        public String displayName() { return "Install Provider"; }
        @Override
        public StepResult execute(OnboardingContext ctx) {
            String providerId = ctx.get("providerId");
            // Check if already installed
            Optional<ServicePackage> servicePkg = PackageRegistry.getInstance().get(providerId);
            if (!servicePkg.isPresent()) {
                ctx.put("error", "Provider package metadata not found: " + providerId);
                return StepResult.FAILED;
            }

            CountDownLatch latch = new CountDownLatch(1);
            final String[] err = new String[1];
            
            // Trigger install pipeline
            InstallationService.getInstance().install(providerId, 
                progress -> {
                    ctx.put("progress", progress);
                },
                complete -> {
                    latch.countDown();
                },
                fail -> {
                    err[0] = fail;
                    latch.countDown();
                }
            );

            try {
                latch.await();
            } catch (InterruptedException e) {
                ctx.put("error", "Installation interrupted.");
                return StepResult.FAILED;
            }

            if (err[0] != null) {
                ctx.put("error", err[0]);
                return StepResult.FAILED;
            }

            // Start provider runtime
            Optional<AIProvider> provider = ProviderRegistry.getInstance().get(providerId);
            if (provider.isPresent()) {
                provider.get().start();
                // Wait for startup
                for (int i = 0; i < 20; i++) {
                    if (provider.get().runtimeStatus() == com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus.READY) {
                        break;
                    }
                    try { Thread.sleep(500); } catch (Exception e) {}
                }
            }

            return StepResult.SUCCESS;
        }
        @Override
        public String failureMessage(OnboardingContext ctx) {
            return ctx.get("error");
        }
    }

    public static class ModelSelectStep implements OnboardingStep {
        @Override
        public String id() { return "model-select"; }
        @Override
        public String displayName() { return "Select Model"; }
        @Override
        public StepResult execute(OnboardingContext ctx) {
            if (ctx.get("modelId") == null) {
                ctx.put("modelId", "qwen2.5-3b");
            }
            return StepResult.SUCCESS;
        }
        @Override
        public String failureMessage(OnboardingContext ctx) {
            return "No model selected.";
        }
    }

    public static class ModelDownloadStep implements OnboardingStep {
        @Override
        public String id() { return "model-download"; }
        @Override
        public String displayName() { return "Download Model"; }
        @Override
        public StepResult execute(OnboardingContext ctx) {
            String modelId = ctx.get("modelId");
            CountDownLatch latch = new CountDownLatch(1);
            final String[] err = new String[1];

            InstallationService.getInstance().install(modelId,
                progress -> {
                    ctx.put("progress", progress);
                },
                complete -> {
                    latch.countDown();
                },
                fail -> {
                    err[0] = fail;
                    latch.countDown();
                }
            );

            try {
                latch.await();
            } catch (InterruptedException e) {
                ctx.put("error", "Download interrupted.");
                return StepResult.FAILED;
            }

            if (err[0] != null) {
                ctx.put("error", err[0]);
                return StepResult.FAILED;
            }

            return StepResult.SUCCESS;
        }
        @Override
        public String failureMessage(OnboardingContext ctx) {
            return ctx.get("error");
        }
    }

    public static class WarmupStep implements OnboardingStep {
        @Override
        public String id() { return "warmup"; }
        @Override
        public String displayName() { return "Warming Up Model"; }
        @Override
        public StepResult execute(OnboardingContext ctx) {
            try {
                AIPlatform.getInstance().rebuildState();
                AISession session = AIPlatform.getInstance().openSession("architecture");
                SessionManager.getInstance().warmup(session);
                SessionManager.getInstance().close(session);
                return StepResult.SUCCESS;
            } catch (Exception e) {
                ctx.put("error", "Warm-up warning: " + e.getMessage());
                // Non-fatal, return success to let user proceed
                return StepResult.SUCCESS;
            }
        }
        @Override
        public String failureMessage(OnboardingContext ctx) {
            return ctx.get("error");
        }
    }

    public static class CompleteStep implements OnboardingStep {
        @Override
        public String id() { return "complete"; }
        @Override
        public String displayName() { return "Setup Complete"; }
        @Override
        public StepResult execute(OnboardingContext ctx) {
            // Write preference file indicating onboarding is done
            String userHome = System.getProperty("user.home");
            File config = new File(userHome, ".gemini/antigravity/onboarding.properties");
            Properties props = new Properties();
            props.setProperty("ai.onboarding.completed", "true");
            try (FileOutputStream fos = new FileOutputStream(config)) {
                props.store(fos, "Bodhak Onboarding Preferences");
            } catch (IOException e) {
                // non-fatal
            }
            return StepResult.SUCCESS;
        }
        @Override
        public String failureMessage(OnboardingContext ctx) {
            return "Failed to complete onboarding configuration.";
        }
    }
}
