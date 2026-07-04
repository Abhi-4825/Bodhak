package com.example.bodhak.analyzer.gatling.runner;

import com.example.bodhak.analyzer.gatling.model.GatlingSimulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs Gatling load tests via 'mvn gatling:test' — no GATLING_HOME required.
 *
 * Strategy:
 *   1. Create a self-contained temporary Maven project under ~/.bodhak/gatling-temp/
 *   2. Write a pom.xml that pulls in the Gatling Maven plugin
 *   3. Write a Java Gatling simulation (no Scala toolchain needed)
 *   4. Execute: mvn gatling:test -Dgatling.simulationClass=GeneratedSimulation
 *   5. Stream output line-by-line via an outputListener callback (feeds the UI TextArea)
 *   6. Return the path to the generated results directory
 */
public class MavenGatlingRunner {

    private static final String SIMULATION_CLASS    = "GeneratedSimulation";
    private static final String GATLING_VERSION     = "3.9.5";   // library version
    private static final String PLUGIN_VERSION      = "4.3.7";   // maven plugin has SEPARATE versioning

    private final Path projectBase;

    public MavenGatlingRunner() {
        this(Path.of(System.getProperty("user.home"), ".bodhak", "gatling-temp"));
    }

    public MavenGatlingRunner(Path projectBase) {
        this.projectBase = projectBase;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Runs the full Maven Gatling pipeline.
     *
     * @param simulation     The simulation model to write + run
     * @param outputListener Called for each stdout/stderr line — use this to feed the UI TextArea
     * @return Path to the Gatling results directory (contains global_stats.json)
     */
    public Path run(GatlingSimulation simulation, Consumer<String> outputListener)
            throws GatlingExecutionException, IOException {

        // Step 1: Write the temp Maven project
        setupProjectStructure();
        writePom();
        writeSimulation(simulation);

        outputListener.accept("✅ Simulation project written to: " + projectBase);
        outputListener.accept("🚀 Running: mvn gatling:test ...");

        // Step 2: Execute Maven
        List<String> command = buildMavenCommand();
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(projectBase.toFile());
        pb.redirectErrorStream(true);

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new GatlingExecutionException(
                "Could not start Maven process. Is 'mvn' on your PATH? Error: " + e.getMessage(), e);
        }

        // Step 3: Stream output live
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputListener.accept(line);
            }
        }

        // Step 4: Wait for completion
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatlingExecutionException("Maven process was interrupted.", e);
        }

        if (exitCode != 0) {
            throw new GatlingExecutionException(
                "mvn gatling:test exited with code " + exitCode +
                ". Check the output above for details.");
        }

        outputListener.accept("✅ Gatling test completed.");

        // Step 5: Locate the results directory
        return findLatestResultDir(outputListener);
    }

    // ── Internal Helpers ───────────────────────────────────────────────────────

    private void setupProjectStructure() throws IOException {
        // Clean out stale target/ from any previous failed run so Maven doesn't use cached errors
        Path targetDir = projectBase.resolve("target");
        if (Files.exists(targetDir)) {
            deleteRecursively(targetDir);
        }
        Path simDir = projectBase
            .resolve("src").resolve("test").resolve("java");
        Files.createDirectories(simDir);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var entries = Files.list(path)) {
                for (Path entry : entries.toList()) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    private void writePom() throws IOException {
        String pom = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.bodhak</groupId>
              <artifactId>bodhak-gatling-temp</artifactId>
              <version>1.0-SNAPSHOT</version>
              <packaging>jar</packaging>

              <properties>
                <maven.compiler.source>17</maven.compiler.source>
                <maven.compiler.target>17</maven.compiler.target>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
              </properties>

              <!--
                Only ONE dependency needed:
                gatling-charts-highcharts bundles the Java DSL (io.gatling.javaapi.*)
                AND the HTTP module. No separate gatling-app or gatling-http-java needed.
              -->
              <dependencies>
                <dependency>
                  <groupId>io.gatling.highcharts</groupId>
                  <artifactId>gatling-charts-highcharts</artifactId>
                  <version>%s</version>
                  <scope>test</scope>
                </dependency>
              </dependencies>

              <build>
                <testSourceDirectory>src/test/java</testSourceDirectory>
                <plugins>
                  <plugin>
                    <groupId>io.gatling</groupId>
                    <artifactId>gatling-maven-plugin</artifactId>
                    <!--
                      IMPORTANT: The Maven plugin version (4.x) is completely separate
                      from the Gatling library version (3.9.5). Using 3.9.5 here causes
                      "plugin not found" because that version does NOT exist on Maven Central.
                    -->
                    <version>%s</version>
                    <configuration>
                      <simulationClass>%s</simulationClass>
                    </configuration>
                  </plugin>
                </plugins>
              </build>
            </project>
            """.formatted(GATLING_VERSION, PLUGIN_VERSION, SIMULATION_CLASS);

        Files.writeString(projectBase.resolve("pom.xml"), pom, StandardCharsets.UTF_8);
    }

    private void writeSimulation(GatlingSimulation sim) throws IOException {
        String java = """
            import io.gatling.javaapi.core.*;
            import io.gatling.javaapi.http.*;
            import static io.gatling.javaapi.core.CoreDsl.*;
            import static io.gatling.javaapi.http.HttpDsl.*;
            import java.time.Duration;

            public class GeneratedSimulation extends Simulation {

                HttpProtocolBuilder httpProtocol = http
                    .baseUrl("%s")
                    .acceptHeader("application/json")
                    .userAgentHeader("Bodhak-LoadTest/1.0");

                ScenarioBuilder scn = scenario("Bodhak Load Test")
                    .exec(
                        http("%s %s")
                            .get("%s")
                            .check(status().is(200))
                    );

                {
                    setUp(
                        scn.injectOpen(
                            rampUsers(%d).during(Duration.ofSeconds(%d))
                        )
                    )
                    .protocols(httpProtocol)
                    .maxDuration(Duration.ofSeconds(%d));
                }
            }
            """.formatted(
                sim.baseUrl(),
                sim.httpMethod(), sim.fullPath(),
                sim.fullPath(),
                sim.configuration().virtualUsers(),
                sim.configuration().rampUpSeconds(),
                sim.configuration().durationSeconds()
            );

        Path simFile = projectBase
            .resolve("src").resolve("test").resolve("java")
            .resolve(SIMULATION_CLASS + ".java");
        Files.writeString(simFile, java, StandardCharsets.UTF_8);
    }

    private List<String> buildMavenCommand() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> cmd = new ArrayList<>();
        if (isWindows) {
            // Use cmd /c so that 'mvn' resolves correctly on Windows PATH
            cmd.add("cmd");
            cmd.add("/c");
            cmd.add("mvn");
        } else {
            cmd.add("mvn");
        }
        // simulationClass is already configured in the generated pom.xml — no -D flag needed.
        // ProcessBuilder takes each token as a separate list element; never concatenate them.
        cmd.add("gatling:test");
        return cmd;
    }

    private Path findLatestResultDir(Consumer<String> log) throws GatlingExecutionException, IOException {
        Path resultsDir = projectBase.resolve("target").resolve("gatling");
        if (!Files.isDirectory(resultsDir)) {
            throw new GatlingExecutionException(
                "Gatling results directory not found at: " + resultsDir +
                ". The test may have failed before generating output.");
        }
        return Files.list(resultsDir)
            .filter(Files::isDirectory)
            .max((a, b) -> {
                try {
                    return Files.getLastModifiedTime(a).compareTo(Files.getLastModifiedTime(b));
                } catch (IOException e) { return 0; }
            })
            .orElseThrow(() ->
                new GatlingExecutionException("No result sub-directory found inside: " + resultsDir));
    }
}
