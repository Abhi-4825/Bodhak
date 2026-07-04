package com.example.bodhak;

import com.example.bodhak.model.endpoint.ServiceEndpoint;
import com.example.bodhak.analyzer.gatling.generator.GatlingScenarioGenerator;
import com.example.bodhak.analyzer.gatling.generator.GatlingScriptWriter;
import com.example.bodhak.analyzer.gatling.model.GatlingSimulation;
import com.example.bodhak.analyzer.gatling.model.LoadTestConfiguration;
import com.example.bodhak.analyzer.gatling.model.LoadTestRequest;
import com.example.bodhak.analyzer.gatling.model.PerformanceReport;
import com.example.bodhak.analyzer.gatling.parser.GatlingResultParser;
import com.example.bodhak.analyzer.gatling.runner.GatlingRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GatlingSmokeTest {

    public static void main(String[] args) throws Exception {

        ServiceEndpoint endpoint =
                new ServiceEndpoint(
                        "GET",
                        "/api/customers",
                        "customer"

                );

        LoadTestConfiguration config =
                new LoadTestConfiguration(
                        10,
                        5,
                        10
                );

        LoadTestRequest request =
                new LoadTestRequest(
                        endpoint,
                        config
                );

        GatlingScenarioGenerator generator =
                new GatlingScenarioGenerator();

        GatlingSimulation simulation =
                generator.generate(
                        request,
                        "http://localhost:8080"
                );

        System.out.println("=== SIMULATION ===");
        System.out.println(simulation);

        GatlingScriptWriter writer =
                new GatlingScriptWriter();

        Path scriptPath =
                writer.writeScript(
                        simulation,
                        Paths.get("temp/gatling")
                );

        System.out.println("=== SCRIPT ===");
        System.out.println(scriptPath);

        GatlingRunner runner =
                new GatlingRunner();

        Path resultPath =
                runner.run(
                        scriptPath,
                        simulation.scenarioName()
                );

        System.out.println("=== RESULT PATH ===");
        System.out.println(resultPath);

        GatlingResultParser parser =
                new GatlingResultParser();

        PerformanceReport report =
                parser.parse(resultPath,endpoint,"test");

        System.out.println("=== REPORT ===");
        System.out.println(report);
    }
}
