package com.example.anuviya.platform.installation.pass;

import com.example.anuviya.platform.PackageCategory;
import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.installation.InstallContext;
import com.example.anuviya.platform.installation.InstallRequest;
import com.example.anuviya.platform.registry.domain.PackageRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public class DownloadPass implements InstallPass {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String name() {
        return "Download Pass";
    }

    @Override
    public PassResult execute(InstallRequest request, InstallContext ctx) {
        Optional<ServicePackage> pkgOpt = PackageRegistry.getInstance().get(request.packageId());
        if (!pkgOpt.isPresent()) {
            ctx.setErrorMessage("Package not found in registry: " + request.packageId());
            return PassResult.FAILED;
        }

        ServicePackage pkg = pkgOpt.get();

        if (pkg.category() == PackageCategory.AI_PROVIDER) {
            return downloadProvider(pkg, ctx);
        } else if (pkg.category() == PackageCategory.AI_MODEL) {
            return downloadModel(pkg, ctx);
        }

        return PassResult.SUCCESS;
    }

    private PassResult downloadProvider(ServicePackage pkg, InstallContext ctx) {
        String osName = getNormalizedOsName();
        ServicePackage.InstallerDef instDef = pkg.installerByOs().get(osName);
        if (instDef == null) {
            ctx.setErrorMessage("No installer defined for operating system: " + osName);
            return PassResult.FAILED;
        }

        try {
            ctx.updateProgress(0.0);
            Path tempFile = Files.createTempFile("bodhak-installer-", "." + instDef.type());
            ctx.put("tempFile", tempFile.toAbsolutePath().toString());

            // Open stream connection
            URL downloadUrl = new URL(instDef.url());
            try (BufferedInputStream in = new BufferedInputStream(downloadUrl.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile())) {
                
                byte[] dataBuffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                // Estimate size if Content-Length unavailable (Ollama is usually ~250MB)
                long expectedSize = pkg.sizeGb() > 0 ? (long)(pkg.sizeGb() * 1024L * 1024 * 1024) : 250_000_000L;
                
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    double progress = (double) totalBytes / expectedSize;
                    if (progress > 0.99) progress = 0.99;
                    ctx.updateProgress(progress);
                }
            }
            ctx.updateProgress(1.0);
            return PassResult.SUCCESS;
        } catch (Exception e) {
            ctx.setErrorMessage("Download failed: " + e.getMessage());
            return PassResult.FAILED;
        }
    }

    private PassResult downloadModel(ServicePackage pkg, InstallContext ctx) {
        // Trigger Ollama Pull API
        try {
            ctx.updateProgress(0.0);
            ServicePackage.DownloadDef dlDef = pkg.downloadByRuntime().get("ollama");
            if (dlDef == null || dlDef.command() == null) {
                ctx.setErrorMessage("No download definition for Ollama runtime.");
                return PassResult.FAILED;
            }

            // Extract the model name e.g. "pull qwen2.5:3b" -> "qwen2.5:3b"
            String cmd = dlDef.command();
            String modelName = cmd.replace("pull", "").trim();

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

            String jsonPayload = MAPPER.writeValueAsString(java.util.Map.of("name", modelName));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/pull"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofHours(1))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

            HttpResponse<java.util.stream.Stream<String>> response = client.send(
                request, HttpResponse.BodyHandlers.ofLines()
            );

            if (response.statusCode() != 200) {
                ctx.setErrorMessage("Ollama Pull API returned error status: " + response.statusCode());
                return PassResult.FAILED;
            }

            response.body().forEach(line -> {
                if (line.trim().isEmpty()) return;
                try {
                    JsonNode node = MAPPER.readTree(line);
                    if (node.has("completed") && node.has("total")) {
                        long completed = node.get("completed").asLong();
                        long total = node.get("total").asLong();
                        if (total > 0) {
                            double progress = (double) completed / total;
                            ctx.updateProgress(progress);
                        }
                    }
                } catch (Exception e) {
                    // Ignore parse errors on stream tags
                }
            });

            ctx.updateProgress(1.0);
            return PassResult.SUCCESS;
        } catch (Exception e) {
            ctx.setErrorMessage("Model download failed: " + e.getMessage() + ". Make sure Ollama is running.");
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
