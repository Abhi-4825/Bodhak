package com.example.anuviya.analyzer.gatling.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Executes Gatling as an external process and returns the results directory path.
 *
 * Strategy:
 *   - Detects the Gatling home directory from GATLING_HOME env variable.
 *   - Copies the generated .scala file into Gatling's user-files/simulations directory.
 *   - Invokes gatling.bat (Windows) or gatling.sh (Unix) via ProcessBuilder.
 *   - Returns the path to the results directory for parsing.
 *
 * This class is intentionally decoupled from the Scala generation logic.
 * It only cares about invoking a process and finding its output.
 */
public class GatlingRunner {

    /**
     * Executes a Gatling simulation by copying the generated script into Gatling's
     * simulation directory and running the Gatling CLI.
     *
     * @param simulationScalaFile Path to the generated .scala simulation file
     * @param simulationClassName The simple class name of the simulation (no package)
     * @return Path to the Gatling results directory containing global_stats.json
     * @throws GatlingExecutionException if Gatling is not found or the run fails
     * @throws IOException               if file operations fail
     */
    public Path run(Path simulationScalaFile, String simulationClassName)
            throws GatlingExecutionException, IOException {

        Path gatlingHome = resolveGatlingHome();
        Path simulationsDir = gatlingHome.resolve("user-files").resolve("simulations");
        Files.createDirectories(simulationsDir);

        // Copy the generated script into Gatling's simulations directory
        Path targetScript = simulationsDir.resolve(simulationScalaFile.getFileName());
        Files.copy(simulationScalaFile, targetScript,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Build the Gatling CLI command
        List<String> command = buildGatlingCommand(gatlingHome, simulationClassName);

        // Execute
        System.out.println("[Bodhak Gatling] Running simulation: " + simulationClassName);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(gatlingHome.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Stream output to console in real-time
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Gatling] " + line);
            }
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatlingExecutionException("Gatling process was interrupted", e);
        }

        if (exitCode != 0) {
            throw new GatlingExecutionException("Gatling exited with code " + exitCode);
        }

        return findLatestResultDir(gatlingHome.resolve("results"), simulationClassName);
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private Path resolveGatlingHome() throws GatlingExecutionException {
        String gatlingHomeEnv = System.getenv("GATLING_HOME");
        if (gatlingHomeEnv != null && !gatlingHomeEnv.isBlank()) {
            Path p = Paths.get(gatlingHomeEnv);
            if (Files.isDirectory(p)) return p;
        }
        throw new GatlingExecutionException(
                "GATLING_HOME environment variable is not set or does not point to a valid directory. " +
                "Please install Gatling and set GATLING_HOME to its root directory.");
    }

    private List<String> buildGatlingCommand(Path gatlingHome, String simulationClass) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String scriptName = isWindows ? "gatling.bat" : "gatling.sh";
        Path script = gatlingHome.resolve("bin").resolve(scriptName);

        List<String> cmd = new ArrayList<>();
        if (isWindows) {
            cmd.add("cmd");
            cmd.add("/c");
        } else {
            cmd.add("bash");
        }
        cmd.add(script.toAbsolutePath().toString());
        cmd.add("-s");
        cmd.add(simulationClass);
        cmd.add("-nr"); // No reports — we parse raw stats ourselves
        return cmd;
    }

    /**
     * Finds the most recently modified results sub-directory matching the simulation name.
     */
    private Path findLatestResultDir(Path resultsDir, String simulationClassName) throws IOException, GatlingExecutionException {
        if (!Files.isDirectory(resultsDir)) {
            throw new GatlingExecutionException("Gatling results directory not found at: " + resultsDir);
        }

        String prefix = simulationClassName.toLowerCase();

        Optional<Path> latest = Files.list(resultsDir)
                .filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().toLowerCase().startsWith(prefix))
                .max((a, b) -> {
                    try {
                        return Files.getLastModifiedTime(a).compareTo(Files.getLastModifiedTime(b));
                    } catch (IOException e) {
                        return 0;
                    }
                });

        return latest.orElseThrow(() ->
                new GatlingExecutionException("No result directory found for simulation: " + simulationClassName));
    }
}
