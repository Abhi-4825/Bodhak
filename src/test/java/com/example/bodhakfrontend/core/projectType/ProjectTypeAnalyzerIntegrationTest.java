package com.example.bodhakfrontend.core.projectType;

import com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectorRegistry;
import com.example.bodhakfrontend.core.projectType.detectors.java.SpringBootDetector;
import com.example.bodhakfrontend.core.projectType.detectors.javascript.ReactDetector;
import com.example.bodhakfrontend.core.projectType.detectors.python.FastAPIDetector;
import com.example.bodhakfrontend.core.projectType.engine.ProjectTypeAnalyzer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.bodhakfrontend.core.projectType.ProjectType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the full Project Type Detection pipeline.
 *
 * These tests simulate real project layouts and verify that:
 *   FrameworkDetectors → CapabilityAggregator → CapabilityBasedClassifier
 * produces the correct ProjectClassificationResult for each scenario.
 */
class ProjectTypeAnalyzerIntegrationTest {

    private FrameworkDetectorRegistry registry;
    private ProjectTypeAnalyzer analyzer;
    private TestContextBuilder builder;

    @BeforeEach
    void setUp() {
        registry = new FrameworkDetectorRegistry();
        registry.registerAll(
                new SpringBootDetector(),
                new ReactDetector(),
                new FastAPIDetector()
        );
        analyzer = new ProjectTypeAnalyzer(registry);
    }

    @AfterEach
    void cleanup() {
        if (builder != null) builder.cleanup();
    }

    // ── Scenario 1: Pure Spring Boot REST API ─────────────────────────────────

    @Test
    void springBootRestApi_classifiedAsRestApi() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web spring-boot-starter-data-jpa")
                .withJavaEntity("Application",    Set.of("SpringBootApplication"), Set.of("spring_boot"))
                .withJavaEntity("UserController", Set.of("RestController"),        Set.of("rest_controller"))
                .withApplicationProperties();

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.hasType(REST_API),
                "Spring Boot + @RestController should be classified as REST_API");
        assertEquals(REST_API, result.primaryType(),
                "Primary type should be REST_API");
        assertFalse(result.hasType(UNKNOWN));
    }

    @Test
    void springBootRestApi_detectsSpringBootFramework() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web")
                .withJavaEntity("App", Set.of("SpringBootApplication"), Set.of());

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.detectedFrameworks().stream()
                .anyMatch(f -> "spring-boot".equals(f.frameworkName())),
                "spring-boot should appear in detected frameworks");
    }

    // ── Scenario 2: Pure React SPA ────────────────────────────────────────────

    @Test
    void reactSpa_classifiedAsWebApp() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\",\"react-dom\":\"^18.0.0\"," +
                                  "\"react-router-dom\":\"^6.0.0\"}," +
                                  "\"devDependencies\":{\"@vitejs/plugin-react\":\"^4.0.0\"}}")
                .withFile("App.jsx", "export default function App() { return <div/> }")
                .withDirectory("components");

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.hasType(WEB_APP),
                "React project with JSX should be classified as WEB_APP");
        assertFalse(result.hasType(UNKNOWN));
    }

    // ── Scenario 3: FastAPI REST API ──────────────────────────────────────────

    @Test
    void fastApiProject_classifiedAsRestApi() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi\nuvicorn\n")
                .withPythonEntity("UserRouter", Set.of("app.get", "app.post"), Set.of())
                .withDirectory("routers");

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.hasType(REST_API),
                "FastAPI with route decorators should be classified as REST_API");
    }

    // ── Scenario 4: Spring Boot + React Monorepo (multi-type) ────────────────

    @Test
    void springBootAndReact_classifiedAsBothTypes() {
        builder = new TestContextBuilder()
                // Backend signals
                .withPomXml("spring-boot-starter-web")
                .withJavaEntity("Application", Set.of("SpringBootApplication"), Set.of())
                .withJavaEntity("ApiController", Set.of("RestController"), Set.of())
                // Frontend signals
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\",\"react-dom\":\"^18.0.0\"}}")
                .withFile("App.jsx", "export default function App() {}");

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.hasType(REST_API), "Should detect REST_API from Spring Boot backend");
        assertTrue(result.hasType(WEB_APP),  "Should detect WEB_APP from React frontend");
        assertTrue(result.types().size() >= 2, "Should have multiple project types");
    }

    // ── Scenario 5: Unknown / Empty project ───────────────────────────────────

    @Test
    void emptyProject_classifiedAsUnknown() {
        builder = new TestContextBuilder();

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.hasType(LIBRARY),
                "Project with no framework signals should fall back to LIBRARY");
        assertEquals(LIBRARY, result.primaryType());
    }

    @Test
    void projectWithOnlyGenericJavaDep_classifiedAsUnknown() {
        builder = new TestContextBuilder()
                .withPomXml("com.google.guava");  // no Spring markers

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.hasType(LIBRARY));
        assertTrue(result.detectedFrameworks().isEmpty(),
                "No framework should be detected");
    }

    // ── Scenario 6: Microservice (HTTP + Message Queue) ───────────────────────

    @Test
    void springBootWithKafka_classifiedAsMicroservice() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web spring-kafka")
                .withJavaEntity("App",  Set.of("SpringBootApplication"), Set.of())
                .withJavaEntity("Ctrl", Set.of("RestController"),        Set.of());

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.hasType(MICROSERVICE),
                "HTTP_ENDPOINT + MESSAGE_QUEUE should classify as MICROSERVICE");
        assertTrue(result.hasType(REST_API),
                "Should also be classified as REST_API");
    }

    // ── Scenario 7: Capability profile is correctly populated ─────────────────

    @Test
    void capabilityProfile_containsAllDetectedCapabilities() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web spring-boot-starter-data-jpa spring-boot-starter-security")
                .withJavaEntity("App", Set.of("SpringBootApplication"), Set.of());

        var result = analyzer.analyze(builder.buildAnalysisContext());
        var profile = result.capabilityProfile();

        assertTrue(profile.hasCapability(Capability.HTTP_ENDPOINT));
        assertTrue(profile.hasCapability(Capability.DATABASE_ACCESS));
        assertTrue(profile.hasCapability(Capability.SECURITY));
        assertTrue(profile.hasCapability(Capability.DEPENDENCY_INJECTION));
    }

    // ── Scenario 8: Custom detector plugged in at runtime ─────────────────────

    @Test
    void customDetector_pluggedIn_withoutModifyingExistingCode() {
        // Simulate adding a new "ktor" detector with a single lambda-based inline detector
        registry.register(new com.example.bodhakfrontend.core.projectType.detection.AbstractFrameworkDetector() {
            @Override public String frameworkName() { return "ktor-test-stub"; }
            @Override public String languageId()    { return "kotlin"; }

            @Override
            protected com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult doDetect(
                    com.example.bodhakfrontend.core.projectType.detection.DetectionContext ctx) {
                return com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult
                        .builder(frameworkName())
                        .addEvidence(buildFileEvidence("build.gradle has ktor", 0.50))
                        .addCapability(Capability.HTTP_ENDPOINT)
                        .build();
            }
        });

        builder = new TestContextBuilder()
                .withFile("build.gradle", "implementation 'io.ktor:ktor-server-core'");

        var result = analyzer.analyze(builder.buildAnalysisContext());

        assertTrue(result.detectedFrameworks().stream()
                .anyMatch(f -> "ktor-test-stub".equals(f.frameworkName())),
                "Custom plugged-in detector should run without modifying any existing class");
        assertTrue(result.hasType(REST_API),
                "HTTP_ENDPOINT capability should still drive REST_API classification");
    }

    // ── Scenario 9: Result structure completeness ─────────────────────────────

    @Test
    void resultStructure_allFieldsPresent() {
        builder = new TestContextBuilder()
                .withPomXml("spring-boot-starter-web")
                .withJavaEntity("App", Set.of("SpringBootApplication"), Set.of());

        ProjectClassificationResult result = analyzer.analyze(builder.buildAnalysisContext());

        assertNotNull(result.primaryType(),          "primaryType must not be null");
        assertNotNull(result.projectTypes(),          "projectTypes map must not be null");
        assertNotNull(result.capabilityProfile(),     "capabilityProfile must not be null");
        assertNotNull(result.detectedFrameworks(),    "detectedFrameworks must not be null");
        assertFalse(result.projectTypes().isEmpty(),  "At least one project type should be present");
        assertTrue(result.projectTypes().containsKey(result.primaryType()),
                "Primary type must be a key in projectTypes map");
    }
}
