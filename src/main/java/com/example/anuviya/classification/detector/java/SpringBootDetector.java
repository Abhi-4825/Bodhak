package com.example.anuviya.classification.detector.java;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.classification.detection.*;

import static com.example.anuviya.classification.Capability.*;

import java.util.List;

/**
 * Detects Spring Boot framework usage via multi-source evidence.
 *
 * Evidence sources:
 *   - Dependencies: spring-boot-starter-web in pom.xml / build.gradle
 *   - AST: @SpringBootApplication, @RestController, @Controller, @Service
 *   - Tags: "spring_boot" tag in EntityContribution
 *   - Config: application.properties / application.yml
 */
public final class SpringBootDetector extends AbstractFrameworkDetector {

    @Override
    public String frameworkName() { return "spring-boot"; }

    @Override
    public String languageId() { return "java"; }

    @Override
    protected FrameworkDetectionResult doDetect(DetectionContext ctx) {
        var builder = FrameworkDetectionResult.builder(frameworkName());

        // ── 1. Dependency evidence ───────────────────────────────
        if (pomContains(ctx, "spring-boot-starter-web")) {
            builder.addEvidence(dependencyEvidence(
                    "pom.xml contains spring-boot-starter-web", 0.35));
            builder.addCapability(HTTP_ENDPOINT);
        }
        if (pomContains(ctx, "spring-boot-starter-data-jpa")) {
            builder.addEvidence(dependencyEvidence(
                    "pom.xml contains spring-boot-starter-data-jpa", 0.15));
            builder.addCapability(DATABASE_ACCESS);
            builder.addCapability(ORM);
        }
        if (pomContains(ctx, "spring-boot-starter-security")) {
            builder.addEvidence(dependencyEvidence(
                    "pom.xml contains spring-boot-starter-security", 0.10));
            builder.addCapability(SECURITY);
        }
        if (pomContains(ctx, "spring-boot-starter-websocket")) {
            builder.addEvidence(dependencyEvidence(
                    "pom.xml contains spring-boot-starter-websocket", 0.10));
            builder.addCapability(WEBSOCKET);
        }
        if (pomContains(ctx, "spring-boot-starter-amqp")
                || pomContains(ctx, "spring-kafka")) {
            builder.addEvidence(dependencyEvidence(
                    "pom.xml contains messaging dependency", 0.10));
            builder.addCapability(MESSAGE_QUEUE);
        }

        // Also check Gradle
        if (buildGradleContains(ctx, "spring-boot-starter-web")) {
            builder.addEvidence(dependencyEvidence(
                    "build.gradle contains spring-boot-starter-web", 0.35));
            builder.addCapability(HTTP_ENDPOINT);
        }

        // ── 2. AST annotation evidence ──────────────────────────
        List<EntityInfo> springBootApps = entitiesWithDecorator(ctx, "SpringBootApplication");
        if (!springBootApps.isEmpty()) {
            builder.addEvidence(astAnnotationEvidence(
                    "@SpringBootApplication found",
                    0.30,
                    springBootApps.getFirst().getEntityName()));
            builder.addCapability(DEPENDENCY_INJECTION);
        }

        List<EntityInfo> restControllers = entitiesWithDecorator(ctx, "RestController");
        if (!restControllers.isEmpty()) {
            builder.addEvidence(astAnnotationEvidence(
                    "@RestController found on " + restControllers.size() + " entities",
                    0.20,
                    restControllers.getFirst().getEntityName()));
            builder.addCapability(HTTP_ENDPOINT);
        }

        List<EntityInfo> controllers = entitiesWithDecorator(ctx, "Controller");
        if (!controllers.isEmpty()) {
            builder.addEvidence(astAnnotationEvidence(
                    "@Controller found on " + controllers.size() + " entities",
                    0.15,
                    controllers.getFirst().getEntityName()));
            builder.addCapability(HTTP_ENDPOINT);
            builder.addCapability(TEMPLATE_RENDERING);
        }

        // ── 3. EntityContribution tag evidence ──────────────────
        if (ctx.anyEntityHasTag(languageId(), "spring_boot")) {
            builder.addEvidence(entityTagEvidence(
                    "EntityContribution tag 'spring_boot' present", 0.15));
        }
        if (ctx.anyEntityHasTag(languageId(), "rest_controller")) {
            builder.addEvidence(entityTagEvidence(
                    "EntityContribution tag 'rest_controller' present", 0.10));
            builder.addCapability(HTTP_ENDPOINT);
        }

        // ── 4. Configuration evidence ───────────────────────────
        if (ctx.hasFile("application.properties") || ctx.hasFile("application.yml")) {
            builder.addEvidence(configEvidence(
                    "Spring configuration file found", 0.10));
            builder.addCapability(CONFIGURATION_MANAGEMENT);
        }

        return builder.build();
    }
}
