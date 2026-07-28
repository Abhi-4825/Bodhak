package com.example.anuviya.analyzer.ai.platform.provider.impl;

import com.example.anuviya.analyzer.ai.client.OllamaRequest;
import com.example.anuviya.analyzer.ai.client.OllamaResponse;
import com.example.anuviya.analyzer.ai.platform.AIException;
import com.example.anuviya.analyzer.ai.platform.model.ModelCapability;
import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.model.ProviderCapability;
import com.example.anuviya.analyzer.ai.platform.model.ProviderKind;
import com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus;
import com.example.anuviya.analyzer.ai.platform.prompt.PromptPackage;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;
import com.example.anuviya.analyzer.ai.platform.provider.ProviderHealth;
import com.example.anuviya.analyzer.ai.platform.provider.ProviderInfo;
import com.example.anuviya.analyzer.ai.platform.session.AISession;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.registry.domain.PackageRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class OllamaProvider implements AIProvider {
    private static final String BASE_URL = "http://localhost:11434";
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(500))
        .build();
    private final ObjectMapper mapper = new ObjectMapper()
        .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
    private Process localProcess;
    private boolean starting = false;

    @Override
    public ProviderInfo info() {
        return new ProviderInfo(
            "ollama",
            "Ollama",
            ProviderKind.LOCAL,
            List.of(
                ProviderCapability.INFERENCE,
                ProviderCapability.MODEL_DOWNLOAD,
                ProviderCapability.STREAMING,
                ProviderCapability.KEEP_ALIVE
            )
        );
    }

    @Override
    public RuntimeStatus runtimeStatus() {
        if (pingEndpoint()) {
            starting = false;
            return RuntimeStatus.READY;
        }
        if (starting) {
            return RuntimeStatus.STARTING;
        }
        return RuntimeStatus.OFFLINE;
    }

    @Override
    public ProviderHealth health() {
        long start = System.currentTimeMillis();
        boolean ok = pingEndpoint();
        long delay = System.currentTimeMillis() - start;
        return new ProviderHealth(ok, ok ? "Ready" : "Connection failed to localhost:11434", delay);
    }

    @Override
    public boolean start() {
        if (runtimeStatus() == RuntimeStatus.READY) {
            return true;
        }
        starting = true;
        String localAppData = System.getenv("LOCALAPPDATA");
        File exePath = null;
        if (localAppData != null) {
            File testExe = new File(localAppData, "Programs/Ollama/ollama.exe");
            if (testExe.exists()) {
                exePath = testExe;
            }
        }
        
        List<String> command = new ArrayList<>();
        if (exePath != null) {
            command.add(exePath.getAbsolutePath());
        } else {
            command.add("ollama"); // assume in path
        }
        command.add("serve");

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            // Redirect output to avoid hanging
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            localProcess = pb.start();
            
            // Wait up to 5 seconds asynchronously or simply return true to indicate startup initiated
            Thread waitThread = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    if (pingEndpoint()) {
                        starting = false;
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                starting = false;
            });
            waitThread.setDaemon(true);
            waitThread.start();
            return true;
        } catch (IOException e) {
            starting = false;
            return false;
        }
    }

    @Override
    public boolean stop() {
        if (localProcess != null && localProcess.isAlive()) {
            localProcess.destroy();
            localProcess = null;
            return true;
        }
        // Force kill running tasks if started externally? No, only stop what we started.
        return false;
    }

    @Override
    public List<ModelInfo> installedModels() {
        if (runtimeStatus() != RuntimeStatus.READY) {
            return List.of();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/tags"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return List.of();
            }
            JsonNode root = mapper.readTree(response.body());
            JsonNode modelsNode = root.get("models");
            if (modelsNode == null || !modelsNode.isArray()) {
                return List.of();
            }

            List<ModelInfo> results = new ArrayList<>();
            for (JsonNode modelNode : modelsNode) {
                String name = modelNode.get("name").asText(); // e.g. "qwen2.5:3b"
                long size = modelNode.has("size") ? modelNode.get("size").asLong() : 0L;
                double sizeGb = Math.round((size / (1024.0 * 1024.0 * 1024.0)) * 10.0) / 10.0;
                
                // Try matching to registry catalog to pull capabilities
                String normalizedId = name.replace(":", "-");
                Optional<ServicePackage> catalogMatch = PackageRegistry.getInstance().get(normalizedId);

                List<ModelCapability> capabilities = new ArrayList<>();
                String displayName = name;
                if (catalogMatch.isPresent()) {
                    for (String cap : catalogMatch.get().capabilities()) {
                        try {
                            capabilities.add(ModelCapability.valueOf(cap.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            // ignore unknown capability
                        }
                    }
                    displayName = catalogMatch.get().displayName();
                } else {
                    // Default heuristics based on model tag
                    capabilities.add(ModelCapability.CODE);
                    if (name.toLowerCase().contains("qwen") || name.toLowerCase().contains("deepseek") || name.toLowerCase().contains("llama") || name.toLowerCase().contains("mistral")) {
                        capabilities.add(ModelCapability.REASONING);
                        capabilities.add(ModelCapability.JSON);
                    }
                }

                results.add(new ModelInfo(name, displayName, sizeGb, capabilities, true));
            }
            return results;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public String generate(AISession session, PromptPackage prompt) throws AIException {
        try {
            return generateInternal(session.getModel().id(), prompt.userPrompt(), prompt.systemPrompt(), null);
        } catch (Exception e) {
            throw new AIException("Inference failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(AISession session, PromptPackage prompt, Consumer<String> chunkConsumer) throws AIException {
        try {
            generateInternal(session.getModel().id(), prompt.userPrompt(), prompt.systemPrompt(), chunkConsumer);
        } catch (Exception e) {
            throw new AIException("Streaming inference failed: " + e.getMessage(), e);
        }
    }

    private String generateInternal(String model, String prompt, String system, Consumer<String> chunkConsumer) throws Exception {
        boolean useStream = (chunkConsumer != null);
        String format = (system != null) ? "json" : null;
        
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.15);
        options.put("top_p", 0.9);
        options.put("repeat_penalty", 1.1);
        options.put("num_predict", 2048);

        OllamaRequest requestBody = new OllamaRequest(model, prompt, useStream, system, format, options);
        String json = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/generate"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofMinutes(5)) // Long timeout for large local processing
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        // Use custom client configuration for long requests
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        if (useStream) {
            HttpResponse<java.util.stream.Stream<String>> response = client.send(
                request, HttpResponse.BodyHandlers.ofLines()
            );
            StringBuilder fullResponse = new StringBuilder();
            response.body().forEach(line -> {
                if (line.trim().isEmpty()) return;
                try {
                    OllamaResponse ollamaResponse = mapper.readValue(line, OllamaResponse.class);
                    String chunk = ollamaResponse.response();
                    if (chunk != null) {
                        fullResponse.append(chunk);
                        chunkConsumer.accept(chunk);
                    }
                } catch (Exception e) {
                    // Ignore parsing errors on stream termination lines
                }
            });
            return fullResponse.toString();
        } else {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Ollama HTTP Error: " + response.statusCode() + " - " + response.body());
            }
            OllamaResponse ollamaResponse = mapper.readValue(response.body(), OllamaResponse.class);
            return ollamaResponse.response();
        }
    }

    private boolean pingEndpoint() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/tags"))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
