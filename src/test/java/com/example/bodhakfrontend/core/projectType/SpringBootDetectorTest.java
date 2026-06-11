package com.example.bodhakfrontend.core.projectType;

import com.example.bodhakfrontend.core.projectType.detection.DetectionContext;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult;
import com.example.bodhakfrontend.core.projectType.detectors.java.SpringBootDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.bodhakfrontend.core.projectType.Capability.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpringBootDetector.
 * Covers each evidence source independently, then combined.
 */
class SpringBootDetectorTest {

    private final SpringBootDetector detector = new SpringBootDetector();
    private TestContextBuilder builder;

    @AfterEach
    void cleanup() {
        if (builder != null) builder.cleanup();
    }

    // ── Metadata ──────────────────────────────────────────────────────────────

    @Test
    void frameworkName_isSpringBoot() {
        assertEquals("spring-boot", detector.frameworkName());
    }

    @Test
    void languageId_isJava() {
        assertEquals("java", detector.languageId());
    }

    // ── Dependency evidence ───────────────────────────────────────────────────

    @Test
    void pomWithStarterWeb_detected_withHttpEndpoint() {
        builder = new TestContextBuilder()
                .withPomXml("<dependency><groupId>org.springframework.boot</groupId>" +
                            "<artifactId>spring-boot-starter-web</artifactId></dependency>");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.isDetected());
        assertTrue(result.capabilities().contains(HTTP_ENDPOINT));
    }

    @Test
    void pomWithJpa_addsDbAndOrmCapabilities() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-data-jpa");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(DATABASE_ACCESS));
        assertTrue(result.capabilities().contains(ORM));
    }

    @Test
    void pomWithSecurity_addsSecurityCapability() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web spring-boot-starter-security");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(SECURITY));
    }

    @Test
    void pomWithAmqp_addsMessageQueueCapability() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web spring-boot-starter-amqp");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(MESSAGE_QUEUE));
    }

    // ── AST annotation evidence ───────────────────────────────────────────────

    @Test
    void entityWithSpringBootApplicationAnnotation_detected() {
        builder = new TestContextBuilder()
                .withJavaEntity("MyApp", Set.of("SpringBootApplication"), Set.of());
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.isDetected(), "@SpringBootApplication alone scores 0.30 ≥ threshold");
        assertTrue(result.capabilities().contains(DEPENDENCY_INJECTION));
    }

    @Test
    void entityWithRestController_addsHttpEndpoint() {
        builder = new TestContextBuilder()
                .withJavaEntity("MyController", Set.of("RestController"), Set.of());
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(HTTP_ENDPOINT));
    }

    @Test
    void entityWithControllerAnnotation_addsTemplateRendering() {
        builder = new TestContextBuilder()
                .withJavaEntity("MvcController", Set.of("Controller"), Set.of());
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(TEMPLATE_RENDERING));
    }

    // ── EntityContribution tag evidence ───────────────────────────────────────

    @Test
    void entityWithSpringBootTag_contributesToScore() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web")
                .withJavaEntity("MyApp", Set.of(), Set.of("spring_boot"));
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        // pom=0.35 + tag=0.15 = 0.50, detected
        assertTrue(result.isDetected());
        assertTrue(result.score() >= 0.50 - 0.01);
    }

    // ── Configuration evidence ────────────────────────────────────────────────

    @Test
    void applicationPropertiesPresent_addsConfigCapability() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web")
                .withApplicationProperties();
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(CONFIGURATION_MANAGEMENT));
    }

    // ── No evidence ───────────────────────────────────────────────────────────

    @Test
    void noSpringSignals_notDetected() {
        builder = new TestContextBuilder()
                .withPomXml("com.google.guava");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertFalse(result.isDetected());
    }

    // ── Combined (realistic Spring Boot REST API project) ─────────────────────

    @Test
    void fullSpringBootProject_highScoreAndAllCapabilities() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web spring-boot-starter-data-jpa spring-boot-starter-security")
                .withJavaEntity("Application",    Set.of("SpringBootApplication"), Set.of("spring_boot"))
                .withJavaEntity("UserController", Set.of("RestController"),        Set.of("rest_controller"))
                .withApplicationProperties();
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.isDetected());
        assertTrue(result.score() >= 1.0, "Rich project should have high score");
        assertTrue(result.capabilities().contains(HTTP_ENDPOINT));
        assertTrue(result.capabilities().contains(DATABASE_ACCESS));
        assertTrue(result.capabilities().contains(SECURITY));
        assertTrue(result.capabilities().contains(DEPENDENCY_INJECTION));
        assertTrue(result.capabilities().contains(CONFIGURATION_MANAGEMENT));
    }
}
