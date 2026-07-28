package com.example.anuviya.platform.registry;

import com.example.anuviya.analyzer.ai.platform.model.AIProfileDef;
import com.example.anuviya.analyzer.ai.platform.model.ModelCapability;
import com.example.anuviya.analyzer.ai.platform.model.ProviderKind;
import com.example.anuviya.platform.PackageCategory;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.registry.domain.AIProfileRegistry;
import com.example.anuviya.platform.registry.domain.PackageRegistry;
import com.example.anuviya.platform.registry.domain.PromptRegistry;
import com.example.anuviya.platform.registry.domain.SchemaRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RegistryManager {
    private static final RegistryManager INSTANCE = new RegistryManager();
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private RegistryManager() {}

    public static RegistryManager getInstance() {
        return INSTANCE;
    }

    public void loadAll() {
        PackageRegistry.getInstance().clear();
        AIProfileRegistry.getInstance().clear();
        PromptRegistry.getInstance().clear();
        SchemaRegistry.getInstance().clear();

        // 1. Load Providers
        loadPackage("registry/packages/providers/ollama.yaml");

        // 2. Load Models
        loadPackage("registry/packages/models/qwen2.5-3b.yaml");

        // 3. Load Profiles
        loadProfile("registry/ai/profiles/architecture.yaml");
        loadProfile("registry/ai/profiles/scalability.yaml");

        // 4. Load Prompts
        loadPrompt("architecture", "registry/ai/prompts/architecture.md");
        loadPrompt("scalability", "registry/ai/prompts/scalability.md");

        // 5. Load Schemas
        loadSchema("architecture-analysis-v1", "registry/ai/schemas/architecture-analysis-v1.json");
        loadSchema("scalability-analysis-v1", "registry/ai/schemas/scalability-analysis-v1.json");
    }

    @SuppressWarnings("unchecked")
    private void loadPackage(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Package registry resource not found: " + resourcePath);
                return;
            }
            Map<String, Object> data = MAPPER.readValue(is, Map.class);
            
            String id = (String) data.get("id");
            String displayName = (String) data.get("displayName");
            String description = (String) data.get("description");
            PackageCategory category = PackageCategory.valueOf((String) data.get("category"));
            String version = String.valueOf(data.get("version"));
            String license = (String) data.get("license");
            
            double sizeGb = data.containsKey("sizeGb") ? ((Number) data.get("sizeGb")).doubleValue() : 0.0;
            int contextLength = data.containsKey("contextLength") ? ((Number) data.get("contextLength")).intValue() : 0;
            int requiredRamGb = data.containsKey("requiredRamGb") ? ((Number) data.get("requiredRamGb")).intValue() : 0;
            int requiredVramGb = data.containsKey("requiredVramGb") ? ((Number) data.get("requiredVramGb")).intValue() : 0;
            
            List<String> capabilities = (List<String>) data.getOrDefault("capabilities", new ArrayList<>());
            
            List<String> recommendedTasks = (List<String>) data.getOrDefault("recommendedTasks", new ArrayList<>());
            String homepage = (String) data.get("homepage");
            
            ProviderKind kind = null;
            if (data.containsKey("kind")) {
                kind = ProviderKind.valueOf((String) data.get("kind"));
            }

            Map<String, ServicePackage.InstallerDef> installerByOs = new HashMap<>();
            Map<String, ServicePackage.DownloadDef> downloadByRuntime = new HashMap<>();
            List<ServicePackage.DependencyDef> dependencies = new ArrayList<>();

            Map<String, Object> packageBlock = (Map<String, Object>) data.get("package");
            if (packageBlock != null) {
                // Parse Installer
                Map<String, Object> installerBlock = (Map<String, Object>) packageBlock.get("installer");
                if (installerBlock != null) {
                    for (Map.Entry<String, Object> entry : installerBlock.entrySet()) {
                        String osName = entry.getKey();
                        Map<String, Object> osDef = (Map<String, Object>) entry.getValue();
                        String type = (String) osDef.get("type");
                        String url = (String) osDef.get("url");
                        String sha256 = osDef.get("sha256") != null ? String.valueOf(osDef.get("sha256")) : null;
                        List<String> silentArgs = (List<String>) osDef.getOrDefault("silentArgs", new ArrayList<>());
                        String verifyExecutable = (String) osDef.get("verifyExecutable");
                        String verifyCommand = (String) osDef.get("verifyCommand");

                        installerByOs.put(osName, new ServicePackage.InstallerDef(type, url, sha256, silentArgs, verifyExecutable, verifyCommand));
                    }
                }

                // Parse Download
                Map<String, Object> downloadBlock = (Map<String, Object>) packageBlock.get("download");
                if (downloadBlock != null) {
                    for (Map.Entry<String, Object> entry : downloadBlock.entrySet()) {
                        String runtimeId = entry.getKey();
                        Map<String, Object> runtimeDef = (Map<String, Object>) entry.getValue();
                        String command = (String) runtimeDef.get("command");
                        String repository = (String) runtimeDef.get("repository");

                        downloadByRuntime.put(runtimeId, new ServicePackage.DownloadDef(command, repository));
                    }
                }

                // Parse Dependencies
                List<Map<String, Object>> depsBlock = (List<Map<String, Object>>) packageBlock.get("dependencies");
                if (depsBlock != null) {
                    for (Map<String, Object> dep : depsBlock) {
                        String depId = (String) dep.get("id");
                        String versionConstraint = (String) dep.get("versionConstraint");
                        dependencies.add(new ServicePackage.DependencyDef(depId, versionConstraint));
                    }
                }
            }

            ServicePackage pkg = new ServicePackage(
                id, displayName, description, category, version, license,
                sizeGb, contextLength, requiredRamGb, requiredVramGb,
                capabilities, recommendedTasks,
                data.containsKey("preferredTemperature") ? ((Number) data.get("preferredTemperature")).doubleValue() : 0.15,
                data.containsKey("preferredTopP") ? ((Number) data.get("preferredTopP")).doubleValue() : 0.9,
                homepage, kind, installerByOs, downloadByRuntime, dependencies
            );
            
            PackageRegistry.getInstance().register(pkg);
        } catch (Exception e) {
            System.err.println("Error loading package yaml " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadProfile(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Profile registry resource not found: " + resourcePath);
                return;
            }
            Map<String, Object> data = MAPPER.readValue(is, Map.class);

            String id = (String) data.get("id");
            String displayName = (String) data.get("displayName");
            String description = (String) data.get("description");

            List<ModelCapability> requiredCapabilities = new ArrayList<>();
            if (data.containsKey("requiredCapabilities")) {
                for (String cap : (List<String>) data.get("requiredCapabilities")) {
                    requiredCapabilities.add(ModelCapability.valueOf(cap.toUpperCase()));
                }
            }

            int minContext = data.containsKey("minContext") ? ((Number) data.get("minContext")).intValue() : 0;
            double preferredTemperature = data.containsKey("preferredTemperature") ? ((Number) data.get("preferredTemperature")).doubleValue() : 0.15;
            double preferredTopP = data.containsKey("preferredTopP") ? ((Number) data.get("preferredTopP")).doubleValue() : 0.9;
            List<String> recommendedModels = (List<String>) data.getOrDefault("recommendedModels", new ArrayList<>());

            String outputType = null;
            String outputSchema = null;
            Map<String, Object> outputBlock = (Map<String, Object>) data.get("output");
            if (outputBlock != null) {
                outputType = (String) outputBlock.get("type");
                outputSchema = (String) outputBlock.get("schema");
            }

            AIProfileDef profile = new AIProfileDef(
                id, displayName, description, requiredCapabilities,
                minContext, preferredTemperature, preferredTopP,
                recommendedModels, outputType, outputSchema
            );

            AIProfileRegistry.getInstance().register(profile);
        } catch (Exception e) {
            System.err.println("Error loading profile yaml " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPrompt(String id, String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Prompt registry resource not found: " + resourcePath);
                return;
            }
            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            PromptRegistry.getInstance().register(id, content);
        } catch (Exception e) {
            System.err.println("Error loading prompt text " + resourcePath + ": " + e.getMessage());
        }
    }

    private void loadSchema(String id, String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Schema registry resource not found: " + resourcePath);
                return;
            }
            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
            SchemaRegistry.getInstance().register(id, content);
        } catch (Exception e) {
            System.err.println("Error loading schema json " + resourcePath + ": " + e.getMessage());
        }
    }
}
