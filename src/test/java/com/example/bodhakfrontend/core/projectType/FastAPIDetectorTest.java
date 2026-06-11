package com.example.bodhakfrontend.core.projectType;

import com.example.bodhakfrontend.core.projectType.detection.DetectionContext;
import com.example.bodhakfrontend.core.projectType.detection.FrameworkDetectionResult;
import com.example.bodhakfrontend.core.projectType.detectors.python.FastAPIDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.bodhakfrontend.core.projectType.Capability.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FastAPIDetector.
 */
class FastAPIDetectorTest {

    private final FastAPIDetector detector = new FastAPIDetector();
    private TestContextBuilder builder;

    @AfterEach
    void cleanup() {
        if (builder != null) builder.cleanup();
    }

    @Test
    void frameworkName_isFastapi() {
        assertEquals("fastapi", detector.frameworkName());
    }

    @Test
    void languageId_isPython() {
        assertEquals("python", detector.languageId());
    }

    // ── Dependency evidence ───────────────────────────────────────────────────

    @Test
    void requirementsTxtWithFastapi_detected() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi==0.100.0\nuvicorn==0.23.0\n");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        // fastapi=0.40 + uvicorn=0.10 = 0.50 → detected
        assertTrue(result.isDetected());
        assertTrue(result.capabilities().contains(HTTP_ENDPOINT));
        assertTrue(result.capabilities().contains(DEPENDENCY_INJECTION));
    }

    @Test
    void pyprojectTomlWithFastapi_detected() {
        builder = new TestContextBuilder()
                .withPyprojectToml("[tool.poetry.dependencies]\nfastapi = \"^0.100\"\nuvicorn = \"*\"\n");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.isDetected());
    }

    @Test
    void requirementsWithSqlalchemy_addsDbCapabilities() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi\nsqlalchemy\n");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(DATABASE_ACCESS));
        assertTrue(result.capabilities().contains(ORM));
    }

    // ── AST decorator evidence ────────────────────────────────────────────────

    @Test
    void entityWithAppGetDecorator_detected() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi")
                .withPythonEntity("UserRouter", Set.of("app.get"), Set.of());
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        // fastapi=0.40 + uvicorn=0 + route decorator=0.25 = 0.65
        assertTrue(result.isDetected());
        assertTrue(result.capabilities().contains(HTTP_ENDPOINT));
    }

    @Test
    void entityWithRouterPostDecorator_addsRouteEvidence() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi")
                .withPythonEntity("ItemRouter", Set.of("router.post"), Set.of());
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(HTTP_ENDPOINT));
    }

    // ── File structure evidence ───────────────────────────────────────────────

    @Test
    void routersDirectory_addsEvidence() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi")
                .withDirectory("routers");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        // fastapi=0.40 + routers dir=0.05 = 0.45
        assertTrue(result.isDetected());
        assertTrue(result.score() > 0.40);
    }

    @Test
    void alembicIni_addsMigrationEvidence() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi")
                .withFile("alembic.ini", "[alembic]\n");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(DATABASE_ACCESS));
    }

    // ── No signals ────────────────────────────────────────────────────────────

    @Test
    void djangoProject_notDetected() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("Django==4.2\npsycopg2==2.9\n");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertFalse(result.isDetected());
    }

    // ── Combined (realistic FastAPI project) ──────────────────────────────────

    @Test
    void fullFastApiProject_allCapabilities() {
        builder = new TestContextBuilder()
                .withRequirementsTxt("fastapi\nuvicorn\nsqlalchemy\n")
                .withPythonEntity("UserRouter", Set.of("app.get", "router.post"), Set.of())
                .withDirectory("routers")
                .withFile("alembic.ini", "[alembic]\n");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.isDetected());
        assertTrue(result.capabilities().contains(HTTP_ENDPOINT));
        assertTrue(result.capabilities().contains(DATABASE_ACCESS));
        assertTrue(result.capabilities().contains(DEPENDENCY_INJECTION));
    }
}
