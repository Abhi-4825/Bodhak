package com.example.bodhakfrontend.core;

import com.example.bodhakfrontend.core.api.model.ServiceEndpoint;
import com.example.bodhakfrontend.core.gatling.generator.GatlingScenarioGenerator;
import com.example.bodhakfrontend.core.gatling.generator.GatlingScriptWriter;
import com.example.bodhakfrontend.core.gatling.model.GatlingSimulation;
import com.example.bodhakfrontend.core.gatling.model.LoadTestConfiguration;
import com.example.bodhakfrontend.core.gatling.model.LoadTestRequest;
import com.example.bodhakfrontend.core.gatling.model.PerformanceReport;
import com.example.bodhakfrontend.core.gatling.parser.GatlingResultParser;
import com.example.bodhakfrontend.core.gatling.runner.GatlingRunner;

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