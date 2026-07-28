package com.example.anuviya.analyzer.gatling.generator;

import com.example.anuviya.analyzer.gatling.model.GatlingSimulation;
import com.example.anuviya.analyzer.gatling.model.LoadTestConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts a GatlingSimulation model into a physical Gatling Scala script file.
 *
 * This class owns the Gatling 3.x DSL template.
 * The rest of Bodhak never sees a single line of Scala.
 *
 * V2: Extend the template to include Authorization headers.
 * V3: Extend the template to include request body DSL blocks.
 */
public class GatlingScriptWriter {

    /**
     * Writes a Gatling Simulation.scala file to the given output directory.
     *
     * @param simulation The internal simulation model to serialize
     * @param outputDir  The directory where the .scala file will be written
     * @return The path to the generated .scala file
     * @throws IOException if the file cannot be written
     */
    public Path writeScript(GatlingSimulation simulation, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);

        String script = buildScalaScript(simulation);
        Path scriptFile = outputDir.resolve(simulation.scenarioName() + ".scala");
        Files.writeString(scriptFile, script, StandardCharsets.UTF_8);

        return scriptFile;
    }

    // ── Private Template Builder ───────────────────────────────────────────────

    private String buildScalaScript(GatlingSimulation sim) {
        LoadTestConfiguration cfg = sim.configuration();

        return """
                import io.gatling.core.Predef._
                import io.gatling.http.Predef._
                import scala.concurrent.duration._

                class %s extends Simulation {

                  val httpProtocol = http
                    .baseUrl("%s")
                    .acceptHeader("application/json")
                    .userAgentHeader("Bodhak-LoadTest/1.0")

                  val scn = scenario("%s")
                    .exec(
                      http("%s %s")
                        .%s("%s")
                        .check(status.is(200))
                    )

                  setUp(
                    scn.inject(
                      rampUsers(%d) during (%d seconds)
                    ).protocols(httpProtocol)
                  ).maxDuration(%d seconds)
                }
                """.formatted(
                sim.scenarioName(),
                sim.baseUrl(),
                sim.scenarioName(),
                sim.httpMethod(), sim.fullPath(),
                sim.httpMethod().toLowerCase(),
                sim.fullPath(),
                cfg.virtualUsers(),
                cfg.rampUpSeconds(),
                cfg.durationSeconds()
        );
    }
}
